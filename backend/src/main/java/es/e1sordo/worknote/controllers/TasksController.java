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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
                    isBlank(highlightedExamples) ? entity.getExamples() : highlightedExamples,
                    entity.isClosed()
            );
        }).toList();

        return ResponseEntity.ok(data);
    }

    @GetMapping
    public List<TaskWithUsageDto> getAll() {
        return service.getAllSortedByUsage().stream().map(Mappings::mapToDtoWithUsage).toList();
    }

    @PostMapping
    public TaskDto upsert(@RequestBody UpsertTaskDto request) {
        return Mappings.mapToDto(service.upsert(request));
    }
}
