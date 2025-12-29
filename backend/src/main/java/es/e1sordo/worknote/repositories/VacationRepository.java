package es.e1sordo.worknote.repositories;

import es.e1sordo.worknote.models.VacationEntity;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface VacationRepository extends ListCrudRepository<VacationEntity, Long> {

    Optional<VacationEntity> findFirstByStartDateGreaterThanEqualOrderByStartDateAsc(LocalDate date);
}
