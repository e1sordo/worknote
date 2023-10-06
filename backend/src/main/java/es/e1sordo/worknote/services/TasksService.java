package es.e1sordo.worknote.services;

import es.e1sordo.worknote.dto.TaskDto;
import es.e1sordo.worknote.dto.TaskWithUsageDto;
import es.e1sordo.worknote.dto.UpsertTaskDto;

import java.io.IOException;
import java.util.List;

public interface TasksService {
    List<TaskDto> getAllByQuery(String query);

    List<TaskWithUsageDto> getAllSortedByUsage();

    TaskDto upsert(UpsertTaskDto request);

    void fillUpLucene() throws IOException;
}
