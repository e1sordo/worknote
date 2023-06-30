package es.e1sordo.worknote.repositories;

import es.e1sordo.worknote.models.WorklogEntity;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WorklogRepository extends ListCrudRepository<WorklogEntity, Long> {
    List<WorklogEntity> findByDateBetween(final LocalDate from, final LocalDate to);
}
