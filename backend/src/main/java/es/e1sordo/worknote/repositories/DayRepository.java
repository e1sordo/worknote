package es.e1sordo.worknote.repositories;

import es.e1sordo.worknote.models.DayEntity;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DayRepository extends ListCrudRepository<DayEntity, LocalDate> {
    List<DayEntity> findByDateBetween(final LocalDate from, final LocalDate to);

    Optional<DayEntity> findTopByOrderByDateDesc();
}
