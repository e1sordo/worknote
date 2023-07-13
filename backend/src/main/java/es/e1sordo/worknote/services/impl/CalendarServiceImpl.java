package es.e1sordo.worknote.services.impl;

import es.e1sordo.worknote.dto.DayDto;
import es.e1sordo.worknote.dto.TaskDto;
import es.e1sordo.worknote.dto.WorklogDto;
import es.e1sordo.worknote.models.DayEntity;
import es.e1sordo.worknote.models.JiraTaskEntity;
import es.e1sordo.worknote.models.WorklogEntity;
import es.e1sordo.worknote.repositories.DayRepository;
import es.e1sordo.worknote.repositories.WorklogRepository;
import es.e1sordo.worknote.services.CalendarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarServiceImpl implements CalendarService {

    private final DayRepository dayRepository;
    private final WorklogRepository worklogRepository;

    @Override
    public List<DayDto> getWeekdays(final LocalDate from, final int weeks) {
        final var endOfWeekExclusively = from.with(DayOfWeek.SUNDAY).plusDays(1);
        final var begin = from.minusWeeks(weeks).with(DayOfWeek.MONDAY);

        // todo sync holidays to next month if needed (using syncTimesheetRepo)

        return getDays(begin, endOfWeekExclusively);
    }

    @Override
    public List<DayDto> getDays(final LocalDate from, final LocalDate to) {
        final Map<LocalDate, DayEntity> days = dayRepository.findByDateBetween(from, to).stream()
                .collect(toMap(DayEntity::getDate, identity(), (d1, d2) -> d1));

        final Map<LocalDate, List<WorklogEntity>> worklogs = worklogRepository.findByDateBetween(from, to).stream()
                .collect(groupingBy(WorklogEntity::getDate));

        return from.datesUntil(to)
                .map(day -> {
                    var dayInfo = ofNullable(days.get(day)).orElseGet(() -> {
                        var weekday = day.getDayOfWeek() == DayOfWeek.SATURDAY || day.getDayOfWeek() == DayOfWeek.SUNDAY;
                        return dayRepository.save(
                                new DayEntity(
                                        day,
                                        weekday,
                                        false,
                                        weekday ? 0 : 480,
                                        null,
                                        null,
                                        null));
                    });

                    final List<WorklogEntity> dayWorklogs = worklogs.getOrDefault(day, emptyList());
                    dayWorklogs.sort(comparing(WorklogEntity::getStartTime));

                    return new DayDto(
                            day.toString(),
                            dayInfo.isNonWorkingDay(),
                            dayInfo.isReducedWorkingDay(),
                            dayInfo.getWorkingMinutes(),
                            dayInfo.getAdditionalInfo(),
                            dayInfo.getWorkedSequenceNumber(),
                            dayInfo.getSummary(),
                            dayWorklogs.stream().map(this::mapToDto).toList()
                    );
                })
                .toList();
    }

    @Override
    public DayDto getDay(final LocalDate date) {
        return getDays(date, date.plusDays(1)).get(0);
    }

    @Override
    public void updateDaySummary(final LocalDate date, final String newText) {
        dayRepository.findById(date).ifPresent(day -> {
            log.info("Try to update day {} summary from '{}' to '{}'", date, day.getSummary(), newText);
            day.setSummary(newText);
            dayRepository.save(day);
        });
    }

    private WorklogDto mapToDto(WorklogEntity entity) {
        final JiraTaskEntity task = entity.getTask();
        return new WorklogDto(
                entity.getId(),
                entity.getStartTime(),
                entity.getDurationInMinutes(),
                entity.getSummary(),
                new TaskDto(
                        task.getId(),
                        task.getJiraId(),
                        task.getProject().getCode(),
                        task.getProject().getShortCode(),
                        task.getType(),
                        task.getTitle()
                ),
                entity.getJiraId(),
                entity.isSynced()
        );
    }
}
