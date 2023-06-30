package es.e1sordo.worknote.repositories;

import es.e1sordo.worknote.models.JiraProjectEntity;
import es.e1sordo.worknote.models.JiraTaskEntity;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskRepository extends ListCrudRepository<JiraTaskEntity, Long> {
    Optional<JiraTaskEntity> findByJiraIdAndProject(int jiraId, JiraProjectEntity project);
}
