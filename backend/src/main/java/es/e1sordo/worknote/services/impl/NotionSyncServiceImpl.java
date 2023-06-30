package es.e1sordo.worknote.services.impl;

import es.e1sordo.worknote.clients.NotionClient;
import es.e1sordo.worknote.dto.notion.ObjectResponse;
import es.e1sordo.worknote.dto.notion.ObjectsPageResponse;
import es.e1sordo.worknote.dto.notion.QueryRequest;
import es.e1sordo.worknote.models.JiraProjectEntity;
import es.e1sordo.worknote.models.JiraTaskEntity;
import es.e1sordo.worknote.models.JiraTaskType;
import es.e1sordo.worknote.models.WorklogEntity;
import es.e1sordo.worknote.repositories.ProjectRepository;
import es.e1sordo.worknote.repositories.TaskRepository;
import es.e1sordo.worknote.repositories.WorklogRepository;
import es.e1sordo.worknote.services.NotionSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.function.UnaryOperator.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotionSyncServiceImpl implements NotionSyncService {

    private final NotionClient client;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final WorklogRepository worklogRepository;

    @Value("${notion.timesheetDb}")
    private String notionDb;

    @Override
    public void sync() {
        final List<ObjectResponse> allData = new ArrayList<>();

        String startCursor = null;
        ObjectsPageResponse response;
        do {
            var filterQuery = new QueryRequest();
            filterQuery.setStartCursor(startCursor);
            response = client.getAll(notionDb, filterQuery);
            allData.addAll(response.getResults());
            startCursor = response.getNextCursor();
        } while (response.isHasMore());

        final Map<String, List<ObjectResponse>> groupedWorklogs = allData.stream()
                .collect(groupingBy(result -> result.getJiraProject() + "-" + result.getJiraTaskId()));

        final Map<String, JiraProjectEntity> projects = projectRepository.findAll().stream()
                .collect(toMap(JiraProjectEntity::getCode, identity()));

        final List<JiraTaskEntity> tasksToCreate = groupedWorklogs.entrySet().stream()
                .map(entry -> {
                    final String[] projectAndTaskId = entry.getKey()
                            .replace("null", "PRODGATE")
                            .split("-");
                    final int jiraId = Integer.parseInt(projectAndTaskId[1]);
                    return new JiraTaskEntity(
                            null,
                            jiraId,
                            projects.get(projectAndTaskId[0]),
                            jiraId < 30 ? JiraTaskType.PROCESS_MAINTENANCE : JiraTaskType.DEVELOPMENT,
                            Optional.ofNullable(entry.getValue().get(0).getJiraTaskName()).orElse("[UNNAMED]"),
                            ""
                    );
                }).toList();

        tasksToCreate.forEach(entity -> {
            log.info("Save task {}", entity.getTitle());
            taskRepository.save(entity);
        });

        final Map<Pair<String, Integer>, JiraTaskEntity> storedTasks = taskRepository.findAll().stream()
                .collect(toMap(v -> Pair.of(v.getProject().getCode(), v.getJiraId()), identity(), (t1, t2) -> t1));

        allData.stream().map(worklog -> new WorklogEntity(
                null,
                LocalDate.parse(worklog.getDate()),
                Optional.ofNullable(worklog.getStartTime()).map(LocalTime::parse).orElse(LocalTime.of(12, 0)),
                (int) (worklog.getHoursSpent() * 60),
                worklog.getComment(),
                storedTasks.get(Pair.of(
                        Optional.ofNullable(worklog.getJiraProject()).orElse("PRODGATE"),
                        worklog.getJiraTaskId()
                )),
                Optional.ofNullable(worklog.getJiraId()).map(Long::parseLong).orElse(0L),
                true
        )).forEach(entity -> {
            log.info("Save worklog {}", entity);
            worklogRepository.save(entity);
        });

        final var storedWorklogs = worklogRepository.findAll();

        log.info("Stored worklogs. Size: {}", storedWorklogs.size());
    }
}
