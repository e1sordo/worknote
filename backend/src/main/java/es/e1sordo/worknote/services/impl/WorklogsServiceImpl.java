package es.e1sordo.worknote.services.impl;

import es.e1sordo.worknote.dto.CreateWorklogDto;
import es.e1sordo.worknote.dto.TaskDto;
import es.e1sordo.worknote.dto.WorklogDto;
import es.e1sordo.worknote.models.JiraProjectEntity;
import es.e1sordo.worknote.models.JiraTaskEntity;
import es.e1sordo.worknote.models.JiraTaskType;
import es.e1sordo.worknote.models.WorklogEntity;
import es.e1sordo.worknote.repositories.ProjectRepository;
import es.e1sordo.worknote.repositories.TaskRepository;
import es.e1sordo.worknote.repositories.WorklogRepository;
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
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    @Override
    public WorklogDto create(final CreateWorklogDto request) {
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

        return mapToDto(storedWorklog);
    }

    @Override
    public WorklogDto updateSync(final long id, final long jiraWorklogId) {
        final WorklogEntity storedWorklog = repository.findById(id).orElseThrow(RuntimeException::new);

        storedWorklog.setSynced(true);
        storedWorklog.setJiraId(jiraWorklogId);

        repository.save(storedWorklog);
        log.info("Worklog with id={} was successfully synced ({})", id, jiraWorklogId);

        return mapToDto(storedWorklog);
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

        final String projectCode = matcher.group(1);
        final int jiraTaskId = Integer.parseInt(matcher.group(2));
        final String taskTitle = matcher.group(3);

        final JiraProjectEntity project = projectRepository.findByCode(projectCode)
                .orElseGet(() -> projectRepository.save(
                        new JiraProjectEntity(projectCode, projectCode, projectCode)));

        return taskRepository.findByJiraIdAndProject(jiraTaskId, project)
                .orElseGet(() -> taskRepository.save(
                        new JiraTaskEntity(null, jiraTaskId, project, JiraTaskType.DEVELOPMENT, taskTitle, null, List.of())));
    }

    private WorklogDto mapToDto(WorklogEntity entity) {
        final JiraTaskEntity task = entity.getTask();
        return new WorklogDto(
                entity.getId(),
                entity.getStartTime(),
                entity.getDurationInMinutes(),
                entity.getSummary(),
                new TaskDto(
                        task.getId(),
                        task.getJiraId(),
                        task.getProject().getCode(),
                        task.getProject().getShortCode(),
                        task.getType(),
                        task.getTitle()
                ),
                entity.getJiraId(),
                entity.isSynced()
        );
    }
}
