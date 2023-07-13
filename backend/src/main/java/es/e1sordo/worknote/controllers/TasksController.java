package es.e1sordo.worknote.controllers;

import es.e1sordo.worknote.dto.TaskDto;
import es.e1sordo.worknote.dto.TaskWithUsageDto;
import es.e1sordo.worknote.dto.UpsertTaskDto;
import es.e1sordo.worknote.services.TasksService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TasksController {

    private final TasksService service;

    @GetMapping("/search")
    public List<TaskDto> getAllByQuery(@RequestParam String query) {
        return service.getAllByQuery(query);
    }

    @GetMapping
    public List<TaskWithUsageDto> getAll() {
        return service.getAllSortedByUsage();
    }

    @PostMapping
    public TaskDto upsert(@RequestBody UpsertTaskDto request) {
        return service.upsert(request);
    }
}
