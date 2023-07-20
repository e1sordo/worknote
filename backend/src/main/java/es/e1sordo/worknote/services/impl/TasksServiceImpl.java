package es.e1sordo.worknote.services.impl;

import es.e1sordo.worknote.dto.TaskDto;
import es.e1sordo.worknote.dto.TaskWithUsageDto;
import es.e1sordo.worknote.dto.UpsertTaskDto;
import es.e1sordo.worknote.models.JiraProjectEntity;
import es.e1sordo.worknote.models.JiraTaskEntity;
import es.e1sordo.worknote.repositories.ProjectRepository;
import es.e1sordo.worknote.repositories.TaskRepository;
import es.e1sordo.worknote.services.TasksService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@Slf4j
@Service
@RequiredArgsConstructor
public class TasksServiceImpl implements TasksService {

    private final ProjectRepository projectRepository;
    private final TaskRepository repository;

    @Override
    public List<TaskDto> getAllByQuery(final String query) {
        log.info("Find tasks by query: {}", query);

        return repository.findByClosedFalse()
                .stream()
                .filter(entity -> (entity.getTitle() + entity.getJiraId() + entity.getExamples()).toLowerCase()
                        .contains(query.toLowerCase()))
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public List<TaskWithUsageDto> getAllSortedByUsage() {
        log.info("Find all tasks sorted by usage");

        List<TaskWithUsageDto> result = new ArrayList<>();

        List<Object[]> tasksWithLatestWorklogs = repository.findTasksWithLatestWorklogs();
        for (Object[] task : tasksWithLatestWorklogs) {
            JiraTaskEntity jiraTask = (JiraTaskEntity) task[0];
            var latestDate = ofNullable((LocalDate) task[1]);
            LocalTime latestTime = ofNullable((LocalTime) task[2])
                    .orElseGet(() -> LocalTime.of(8, 0));

            result.add(mapToDtoWithUsage(
                    jiraTask,
                    latestDate.map(date -> date.atTime(latestTime)).orElse(null)));
        }

        repository.findByWorklogsEmpty()
                .forEach(jiraTask -> result.add(mapToDtoWithUsage(jiraTask, null)));

        result.sort(Comparator.comparing(TaskWithUsageDto::closed));

        return result;
    }

    @Override
    public TaskDto upsert(final UpsertTaskDto request) {
        log.info("Upsert task by its Project ({}) and ID ({})", request.code(), request.id());

        final JiraProjectEntity project = projectRepository.findByCode(request.code()).orElseGet(() ->
                projectRepository.save(new JiraProjectEntity(request.code(), request.code(), request.code())));

        final Optional<JiraTaskEntity> task = repository.findByJiraIdAndProject(request.id(), project);

        if (task.isPresent()) {
            final var taskEntity = task.get();
            log.info("Task was found, so try to update it ({})", taskEntity.getTitle());
            taskEntity.setJiraId(request.id());
            taskEntity.setTitle(request.title());
            taskEntity.setType(request.type());
            taskEntity.setExamples(request.examples());
            taskEntity.setClosed(request.closed());
            return mapToDto(repository.save(taskEntity));
        }

        log.info("Task was not found, so try to create. Title: {}", request.title());
        return mapToDto(repository.save(new JiraTaskEntity(
                null, request.id(), project, request.type(), false, request.title(), request.examples(), null
        )));
    }

    private TaskDto mapToDto(final JiraTaskEntity entity) {
        return new TaskDto(
                entity.getId(),
                entity.getJiraId(),
                entity.getProject().getCode(),
                entity.getProject().getShortCode(),
                entity.getType(),
                entity.getTitle(),
                entity.isClosed()
        );
    }

    private TaskWithUsageDto mapToDtoWithUsage(final JiraTaskEntity entity, final LocalDateTime latestWorklogTime) {
        return new TaskWithUsageDto(
                entity.getId(),
                entity.getJiraId(),
                entity.getProject().getCode(),
                entity.getType(),
                entity.getTitle(),
                entity.getExamples(),
                entity.isClosed(),
                latestWorklogTime
        );
    }
}
