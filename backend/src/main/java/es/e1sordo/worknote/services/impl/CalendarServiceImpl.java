package es.e1sordo.worknote.services.impl;

import es.e1sordo.worknote.models.DayEntity;
import es.e1sordo.worknote.models.UnusualProductionDayInfo;
import es.e1sordo.worknote.models.WorklogEntity;
import es.e1sordo.worknote.repositories.DayRepository;
import es.e1sordo.worknote.repositories.WorklogRepository;
import es.e1sordo.worknote.services.CalendarService;
import es.e1sordo.worknote.services.ProductionCalendarService;
import es.e1sordo.worknote.utils.Pair;
import es.e1sordo.worknote.utils.SanitizingUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

@Slf4j
@Service
@Profile("!demo")
@RequiredArgsConstructor
public class CalendarServiceImpl implements CalendarService {

    private static final int MAX_WORKING_MINUTES = 480;

    private final DayRepository dayRepository;
    private final WorklogRepository worklogRepository;
    private final ProductionCalendarService productionCalendarService;

    @Override
    public List<Pair<DayEntity, List<WorklogEntity>>> getWeekdays(final LocalDate from, final int weeks) {
        final var endOfCurrentWeek = from.with(DayOfWeek.SUNDAY);
        var endOfWeekExclusively = endOfCurrentWeek.plusDays(1);

        if (endOfCurrentWeek.equals(from)) {
            endOfWeekExclusively = endOfWeekExclusively.plusWeeks(1);
        }

        final var begin = from.minusWeeks(weeks).with(DayOfWeek.MONDAY);
        return getDays(begin, endOfWeekExclusively);
    }

    @Override
    public void createDaysIfDoesNotExist(final LocalDate tillDate) {
        final LocalDate latestCreatedDate = dayRepository.findTopByOrderByDateDesc()
                .map(DayEntity::getDate)
                .map(d -> d.plusDays(1))
                .orElseGet(LocalDate::now);

        if (!latestCreatedDate.isBefore(tillDate)) {
            return;
        }

        latestCreatedDate.datesUntil(tillDate).forEach(day -> {
                    final var entity = getDayEntity(day);

                    final Map<LocalDate, UnusualProductionDayInfo> daysInfo
                            = productionCalendarService.getUnusualDaysInfo(latestCreatedDate, tillDate);

                    daysInfo.computeIfPresent(day, (localDate, productionInfo) -> {
                        entity.setNonWorkingDay(productionInfo.isNonWorkingDay());
                        entity.setReducedWorkingDay(productionInfo.isReducedWorkingDay());
                        entity.setWorkingMinutes(productionInfo.getWorkingMinutes());
                        entity.setAdditionalInfo(productionInfo.getAdditionalInfo());
                        return productionInfo;
                    });

                    log.info("Day {} is absent in DB, so creating it: {}", day, entity);

                    dayRepository.save(entity);
                }
        );
    }

    private DayEntity getDayEntity(LocalDate day) {
        var weekday = day.getDayOfWeek() == DayOfWeek.SATURDAY || day.getDayOfWeek() == DayOfWeek.SUNDAY;
        return new DayEntity(
                day,
                weekday,
                false,
                false,
                getMinutesByDayStatus(!weekday),
                null,
                null
        );
    }

    @Override
    public List<Pair<DayEntity, List<WorklogEntity>>> getDays(final LocalDate from, final LocalDate to) {
        createDaysIfDoesNotExist(to);

        final Map<LocalDate, DayEntity> days = dayRepository.findByDateBetween(from, to).stream()
                .collect(toMap(DayEntity::getDate, identity(), (d1, d2) -> d1));

        final Map<LocalDate, List<WorklogEntity>> worklogs = worklogRepository.findByDateBetween(from, to).stream()
                .collect(groupingBy(WorklogEntity::getDate));

        return from.datesUntil(to)
                .map(day -> {
                    var dayInfo = days.get(day);

                    final List<WorklogEntity> dayWorklogs = worklogs.getOrDefault(day, emptyList());
                    dayWorklogs.sort(comparing(WorklogEntity::getStartTime));

                    return Pair.of(dayInfo, dayWorklogs);
                })
                .toList();
    }

    @Override
    public Pair<DayEntity, List<WorklogEntity>> getDay(final LocalDate date) {
        return getDays(date, date.plusDays(1)).get(0);
    }

    @Override
    public void updateDaySummary(final LocalDate date, final String newText) {
        dayRepository.findById(date).ifPresent(day -> {
            var newValue = SanitizingUtil.sanitize(newText);
            log.info("Try to update day {} summary from '{}' to '{}'", date, day.getSummary(), newValue);
            if (day.isNonWorkingDay()) {
                day.setSummary(null);
                day.setAdditionalInfo(newValue);
            } else {
                day.setSummary(newValue);
                day.setAdditionalInfo(null);
            }
            dayRepository.save(day);
        });
    }

    @Override
    public void updateWorkingMinutesCount(final LocalDate date, final int value) {
        dayRepository.findById(date).ifPresent(day -> {
            log.info("Try to update day {} working minutes from '{}' to '{}'", date, day.getWorkingMinutes(), value);
            day.setWorkingMinutes(value);
            day.setReducedWorkingDay(value < MAX_WORKING_MINUTES);
            dayRepository.save(day);
        });
    }

    @Override
    public void updateDayNonWorkingStatus(final LocalDate date, final boolean value) {
        dayRepository.findById(date).ifPresent(day -> {
            log.info("Try to update day {} non-working status from '{}' to '{}'", date, day.isNonWorkingDay(), value);
            day.setNonWorkingDay(value);
            day.setWorkingMinutes(getMinutesByDayStatus(!value));
            dayRepository.save(day);
        });
    }

    @Override
    public void updateDayVacation(final LocalDate date, final boolean value) {
        dayRepository.findById(date).ifPresent(day -> {
            log.info("Try to update day {} vacation status from '{}' to '{}'", date, day.isVacation(), value);
            day.setVacation(value);
            dayRepository.save(day);
        });
    }

    private int getMinutesByDayStatus(boolean isWorkingDay) {
        return isWorkingDay ? MAX_WORKING_MINUTES : 0;
    }
}
