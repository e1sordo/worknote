package es.e1sordo.worknote.services;

import es.e1sordo.worknote.dto.TaskDto;

import java.util.List;

public interface TasksService {
    List<TaskDto> getAll(String query);
}
