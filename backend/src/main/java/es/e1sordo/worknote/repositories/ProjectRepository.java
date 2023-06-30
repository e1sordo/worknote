package es.e1sordo.worknote.repositories;

import es.e1sordo.worknote.models.JiraProjectEntity;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectRepository extends ListCrudRepository<JiraProjectEntity, Long> {
    Optional<JiraProjectEntity> findByCode(String code);
}
