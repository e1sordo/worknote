package es.e1sordo.worknote.services.impl;

import es.e1sordo.worknote.dto.CreateWorklogDto;
import es.e1sordo.worknote.dto.UpsertTaskDto;
import es.e1sordo.worknote.models.JiraProjectEntity;
import es.e1sordo.worknote.models.JiraTaskEntity;
import es.e1sordo.worknote.models.JiraTaskType;
import es.e1sordo.worknote.models.WorklogEntity;
import es.e1sordo.worknote.repositories.WorklogRepository;
import es.e1sordo.worknote.services.JiraProjectsService;
import es.e1sordo.worknote.services.TasksService;
import es.e1sordo.worknote.services.WorklogsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorklogsServiceImpl implements WorklogsService {

    private final WorklogRepository repository;
    private final JiraProjectsService projectsService;
    private final TasksService tasksService;

    @Override
    public List<WorklogEntity> findByTask(final JiraTaskEntity task) {
        return repository.findByTask(task);
    }

    @Override
    public WorklogEntity create(final CreateWorklogDto request) {
        final JiraTaskEntity task = resolveTask(request.taskLabel());
        log.info("Task: {}", task);

        final WorklogEntity storedWorklog = repository.save(new WorklogEntity(
                null,
                request.date(),
                LocalTime.parse(request.startTime()),
                request.durationInMinutes(),
                request.summary(),
                task,
                null,
                false
        ));
        log.info("Worklog successfully created!");

        return storedWorklog;
    }

    @Override
    public WorklogEntity updateSync(final long id, final long jiraWorklogId) {
        final WorklogEntity storedWorklog = repository.findById(id).orElseThrow(RuntimeException::new);

        storedWorklog.setSynced(true);
        storedWorklog.setJiraId(jiraWorklogId);

        repository.save(storedWorklog);
        log.info("Worklog with id={} was successfully synced ({})", id, jiraWorklogId);

        return storedWorklog;
    }

    @Override
    public void delete(final long id) {
        repository.deleteById(id);
        log.info("Worklog with id={} was successfully deleted", id);
    }

    private JiraTaskEntity resolveTask(String taskLabel) {
        String pattern = "\\((\\w+)-(\\d+)\\) (.*)";

        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(taskLabel);

        if (!matcher.matches()) {
            log.error("Invalid input string: {}", taskLabel);
            throw new RuntimeException("Failed to parse task. Wrong input format");
        }

        final String projectCode = matcher.group(1).toUpperCase();
        final int jiraTaskId = Integer.parseInt(matcher.group(2));
        final String taskTitle = matcher.group(3);

        final JiraProjectEntity project = projectsService.findByCode(projectCode)
                .orElseGet(() -> projectsService.create(
                        new JiraProjectEntity(projectCode, projectCode, projectCode, true)));

        return tasksService.findByTaskIdAndProject(jiraTaskId, project)
                .orElseGet(() -> {
                    tasksService.upsert(new UpsertTaskDto(
                            jiraTaskId, projectCode, JiraTaskType.DEVELOPMENT, taskTitle, null, null, false
                    ));
                    return tasksService.findByTaskIdAndProject(jiraTaskId, project).orElseThrow();
                });
    }
}
