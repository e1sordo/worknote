package es.e1sordo.worknote.services.impl;

import es.e1sordo.worknote.dto.TaskDto;
import es.e1sordo.worknote.repositories.TaskRepository;
import es.e1sordo.worknote.services.TasksService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TasksServiceImpl implements TasksService {

    private final TaskRepository repository;

    @Override
    public List<TaskDto> getAll(final String query) {
        log.info("Find tasks by query: {}", query);

        return repository.findAll()
                .stream()
                .filter(entity -> (entity.getTitle() + entity.getJiraId() + entity.getExamples()).toLowerCase()
                        .contains(query.toLowerCase()))
                .map(entity -> new TaskDto(
                        entity.getJiraId(),
                        entity.getProject().getCode(),
                        entity.getProject().getShortCode(),
                        entity.getType(),
                        entity.getTitle()))
                .toList();
    }
}
