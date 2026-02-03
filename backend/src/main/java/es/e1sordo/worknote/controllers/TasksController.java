package es.e1sordo.worknote.controllers;

import es.e1sordo.worknote.dto.SearchTaskResult;
import es.e1sordo.worknote.dto.TaskDto;
import es.e1sordo.worknote.dto.TaskWithUsageDto;
import es.e1sordo.worknote.dto.UpsertTaskDto;
import es.e1sordo.worknote.mapping.Mappings;
import es.e1sordo.worknote.models.JiraTaskEntity;
import es.e1sordo.worknote.services.TasksService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.apache.logging.log4j.util.Strings.isBlank;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TasksController {

    private final TasksService service;

    @GetMapping("/search")
    public ResponseEntity<List<TaskDto>> getAllByQuery(@RequestParam String query) {
        final List<SearchTaskResult> serviceResults = service.getAllByQuery(query);

        final List<TaskDto> data = serviceResults.stream().map(searchTaskResult -> {
            final JiraTaskEntity entity = searchTaskResult.entity();
            final String highlightedTitle = searchTaskResult.highlightedTitle();
            final String highlightedExamples = searchTaskResult.highlightedExamples();
            return new TaskDto(
                    entity.getId(),
                    entity.getJiraId(),
                    entity.getProject().getCode(),
                    entity.getProject().getShortCode(),
                    entity.getType(),
                    isBlank(highlightedTitle) ? entity.getTitle() : highlightedTitle,
                    entity.getDefaultValue(),
                    isBlank(highlightedExamples) ? entity.getExamples() : highlightedExamples,
                    entity.isClosed()
            );
        }).toList();

        return ResponseEntity.ok(data);
    }

    @GetMapping()
    public List<TaskWithUsageDto> getAll() {
        return service.getAllSortedByUsage().stream().map(Mappings::mapToDtoWithUsage).toList();
    }

    @GetMapping("/active")
    public List<TaskWithUsageDto> getAllActive() {
        return service.getAllSortedByUsage().stream()
                .filter(p -> !p.first().isClosed() || p.first().getProject().isActive())
                .map(Mappings::mapToDtoWithUsage).toList();
    }

    @GetMapping("/{code}")
    public ResponseEntity<TaskDto> getByCode(@PathVariable("code") String code) {
        final Optional<JiraTaskEntity> task = service.findByCode(code);
        if (task.isPresent()) {
//            final List<PredefinedWorklogEntity> predefinedWorklogs = predefinedWorklogService
//                    .getAllPredefinedWorklogsByTaskId(task.get().getId());
            final var response = Mappings.mapToDto(task.get());
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public TaskDto upsert(@RequestBody UpsertTaskDto request) {
        return Mappings.mapToDto(service.upsert(request));
    }
}
