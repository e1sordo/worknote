package es.e1sordo.worknote.services;

import es.e1sordo.worknote.dto.SearchTaskResult;
import es.e1sordo.worknote.dto.UpsertTaskDto;
import es.e1sordo.worknote.models.JiraTaskEntity;
import es.e1sordo.worknote.utils.Pair;

import java.time.LocalDateTime;
import java.util.List;

public interface TasksService {
    List<JiraTaskEntity> getAllOpenTasks();

    List<SearchTaskResult> getAllByQuery(String query);

    List<Pair<JiraTaskEntity, LocalDateTime>> getAllSortedByUsage();

    JiraTaskEntity upsert(UpsertTaskDto request);
}
