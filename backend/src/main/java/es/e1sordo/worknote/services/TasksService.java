package es.e1sordo.worknote.services;

import es.e1sordo.worknote.dto.SearchTaskResult;
import es.e1sordo.worknote.dto.UpsertTaskDto;
import es.e1sordo.worknote.models.JiraProjectEntity;
import es.e1sordo.worknote.models.JiraTaskEntity;
import es.e1sordo.worknote.utils.Pair;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TasksService {
    List<JiraTaskEntity> getAllOpenTasks();

    List<SearchTaskResult> getAllByQuery(String query);

    List<Pair<JiraTaskEntity, LocalDateTime>> getAllSortedByUsage();

    Optional<JiraTaskEntity> findByTaskIdAndProject(int taskId, JiraProjectEntity project);

    Optional<JiraTaskEntity> findByCode(String code);

    JiraTaskEntity upsert(UpsertTaskDto request);

    Optional<JiraProjectEntity> findActiveProject();
}
