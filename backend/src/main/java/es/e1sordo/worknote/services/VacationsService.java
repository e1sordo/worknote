package es.e1sordo.worknote.services;

import es.e1sordo.worknote.models.VacationEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface VacationsService {
    Optional<VacationEntity> findNextVacation();

    List<VacationEntity> findAll();

    VacationEntity create(LocalDate startDate, LocalDate endDate);

    VacationEntity update(long id, LocalDate startDate, LocalDate endDate, boolean confirmed);

    void delete(long id);
}
