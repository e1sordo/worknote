package es.e1sordo.worknote.services.impl;

import es.e1sordo.worknote.models.VacationEntity;
import es.e1sordo.worknote.repositories.VacationRepository;
import es.e1sordo.worknote.services.CalendarService;
import es.e1sordo.worknote.services.JiraProjectsService;
import es.e1sordo.worknote.services.VacationsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class VacationsServiceImpl implements VacationsService {

    private final VacationRepository repository;
    private final CalendarService calendarService;
    private final JiraProjectsService jiraProjectsService;

    @Override
    public Optional<VacationEntity> findNextVacation() {
        val today = LocalDate.now();
        return repository.findFirstByStartDateGreaterThanEqualOrderByStartDateAsc(today);
    }

    @Override
    public List<VacationEntity> findAll() {
        return repository.findAll();
    }

    @Override
    public VacationEntity create(LocalDate startDate, LocalDate endDate) {
//        val activeProjects = jiraProjectsService.findAll()
//                .stream().filter(JiraProjectEntity::isActive)
//                .toList();
//        val activeProject = activeProjects.get(activeProjects.size() - 1);
        val entity = new VacationEntity(
                null,
                (int) ChronoUnit.DAYS.between(startDate, endDate) + 1,
                startDate,
                endDate,
                false,
                null,
                false,
                null
        );
        return repository.save(entity);
    }

    @Override
    public VacationEntity update(long id, LocalDate startDate, LocalDate endDate, boolean confirmed) {
        log.info("Try to update existed vacation with id={}", id);

        val entity = repository.findById(id);
        if (entity.isEmpty()) {
            return create(startDate, endDate);
        }
        val entityObj = entity.get();

        if (entityObj.isSynced()) {
            throw new RuntimeException("Unable to update already synced vacation entity");
        }

        if (!entityObj.getStartDate().isEqual(startDate) || !entityObj.getEndDate().isEqual(endDate)) {
            entityObj.setStartDate(startDate);
            entityObj.setEndDate(endDate);
            entityObj.setTotalDays((int) ChronoUnit.DAYS.between(startDate, endDate) + 1);

            calendarService.createDaysIfDoesNotExist(endDate);

            // remove vacation marking from previous days
            entityObj.getStartDate().datesUntil(entityObj.getEndDate())
                    .forEach(date -> calendarService.updateDayVacation(date, false));

            // mark new dates as vacation
            startDate.datesUntil(endDate)
                    .forEach(date -> calendarService.updateDayVacation(date, confirmed));
        }

        entityObj.setConfirmed(confirmed);

        return repository.save(entityObj);
    }

    @Override
    public void delete(long id) {
        log.info("Try to delete vacation with id={}", id);

        val entity = repository.findById(id);
        if (entity.isEmpty()) {
            return;
        }

        if (entity.get().isSynced()) {
            throw new RuntimeException("Unable to delete already synced vacation entity");
        }

        repository.deleteById(id);
        log.info("Vacation with id={} was successfully deleted", id);
    }
}
