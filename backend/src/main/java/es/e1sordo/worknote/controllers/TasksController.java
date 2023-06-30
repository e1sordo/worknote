package es.e1sordo.worknote.controllers;

import es.e1sordo.worknote.dto.TaskDto;
import es.e1sordo.worknote.services.TasksService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TasksController {

    private final TasksService service;

    @GetMapping
    public List<TaskDto> getAll(@RequestParam String query) {
        return service.getAll(query);
    }
}
