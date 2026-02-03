package es.e1sordo.worknote.controllers;

import es.e1sordo.worknote.dto.ProjectDto;
import es.e1sordo.worknote.dto.UpsertProjectDto;
import es.e1sordo.worknote.mapping.Mappings;
import es.e1sordo.worknote.models.JiraProjectEntity;
import es.e1sordo.worknote.services.JiraProjectsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectsController {

    private final JiraProjectsService service;

    @GetMapping
    public List<ProjectDto> getAll() {
        return service.findAll().stream().map(Mappings::mapToDto).toList();
    }

    @GetMapping("/active-codes")
    public List<String> getAllActiveCodes() {
        return service.findAll().stream()
                .filter(JiraProjectEntity::isActive)
                .map(JiraProjectEntity::getCode)
                .toList();
    }

    @PostMapping
    public ProjectDto upsert(@RequestBody UpsertProjectDto request) {
        return Mappings.mapToDto(service.upsert(request));
    }
}
