package es.e1sordo.worknote.controllers;

import es.e1sordo.worknote.dto.CreateWorklogDto;
import es.e1sordo.worknote.dto.UpdateWorklogSyncDto;
import es.e1sordo.worknote.dto.WorklogDto;
import es.e1sordo.worknote.services.WorklogsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/worklogs")
@RequiredArgsConstructor
public class WorklogController {

    private final WorklogsService service;

    @PostMapping
    public WorklogDto create(@RequestBody CreateWorklogDto request) {
        log.info("Create Worklog Request");
        return service.create(request);
    }

    @PutMapping("/{id}")
    public WorklogDto updateSync(@PathVariable long id, @RequestBody UpdateWorklogSyncDto request) {
        log.info("Delete Worklog {} Request", id);
        return service.updateSync(id, request.jiraWorklogId());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable long id) {
        log.info("Delete Worklog {} Request", id);
        service.delete(id);
    }
}
