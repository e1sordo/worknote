package es.e1sordo.worknote.services.impl;

import es.e1sordo.worknote.enums.AppSettingKeys;
import es.e1sordo.worknote.models.DayEntity;
import es.e1sordo.worknote.models.WorklogEntity;
import es.e1sordo.worknote.repositories.DayRepository;
import es.e1sordo.worknote.repositories.WorklogRepository;
import es.e1sordo.worknote.services.AppSettingsService;
import es.e1sordo.worknote.services.CalendarService;
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
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
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
    private final AppSettingsService appSettingsService;

    @Override
    public List<Pair<DayEntity, List<WorklogEntity>>> getWeekdays(final LocalDate from, final int weeks) {
        final var endOfCurrentWeek = from.with(DayOfWeek.SUNDAY);
        var endOfWeekExclusively = endOfCurrentWeek.plusDays(1);

        if (endOfCurrentWeek.equals(from)) {
            endOfWeekExclusively = endOfWeekExclusively.plusWeeks(1);
        }

        final var begin = from.minusWeeks(weeks).with(DayOfWeek.MONDAY);

        // todo sync holidays to next month if needed (using syncTimesheetRepo)

        return getDays(begin, endOfWeekExclusively);
    }

    @Override
    public List<Pair<DayEntity, List<WorklogEntity>>> getDays(final LocalDate from, final LocalDate to) {
        final Map<LocalDate, DayEntity> days = dayRepository.findByDateBetween(from, to).stream()
                .collect(toMap(DayEntity::getDate, identity(), (d1, d2) -> d1));

        final Map<LocalDate, List<WorklogEntity>> worklogs = worklogRepository.findByDateBetween(from, to).stream()
                .collect(groupingBy(WorklogEntity::getDate));

        final AtomicInteger workedSequenceNumber = new AtomicInteger(0);
        return from.datesUntil(to)
                .map(day -> {
                    var dayInfo = ofNullable(days.get(day)).orElseGet(() -> {
                        var weekday = day.getDayOfWeek() == DayOfWeek.SATURDAY || day.getDayOfWeek() == DayOfWeek.SUNDAY;

                        final var entity = new DayEntity(
                                day,
                                weekday,
                                false,
                                false,
                                getMinutesByDayStatus(!weekday),
                                null,
                                weekday ? null : workedSequenceNumber.incrementAndGet(),
                                null
                        );

                        return dayRepository.save(entity);
                    });

                    ofNullable(dayInfo.getWorkedSequenceNumber()).ifPresent(workedSequenceNumber::set);

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

            updateNewFirstWorkingDay(date);
        });
    }

    @Override
    public void updateDayVacation(final LocalDate date, final boolean value) {
        dayRepository.findById(date).ifPresent(day -> {
            log.info("Try to update day {} vacation status from '{}' to '{}'", date, day.isVacation(), value);
            day.setVacation(value);
            dayRepository.save(day);

            updateNewFirstWorkingDay(date);
        });
    }

    @Override
    public void updateNewFirstWorkingDay(LocalDate from) {
        final String value = appSettingsService.get(AppSettingKeys.WORK_SINCE).getValue();
        if (value == null) {
            return;
        }
        final var newFirstWorkingDay = LocalDate.parse(value);

        final var today = LocalDate.now();
        if (newFirstWorkingDay.isAfter(today)) {
            return;
        }

        final List<DayEntity> allDays = dayRepository.findAll()
                .stream()
                .sorted(comparing(DayEntity::getDate))
                .filter(entity -> from == null || !entity.getDate().isBefore(from))
                .toList();

        var counter = 1;
        if (from != null) {
            var previousCounter = dayRepository.findMaxWorkedSequenceNumberBeforeDate(from);
            if (previousCounter != null) {
                counter = previousCounter + 1;
            }
        }

        for (DayEntity day : allDays) {
            final LocalDate dayDate = day.getDate();
            if (dayDate.isBefore(newFirstWorkingDay) || day.isNonWorkingDay() || day.isVacation()) {
                day.setWorkedSequenceNumber(null);
                continue;
            }

            day.setWorkedSequenceNumber(counter++);
        }

        dayRepository.saveAll(allDays);
    }

    private int getMinutesByDayStatus(boolean isWorkingDay) {
        return isWorkingDay ? MAX_WORKING_MINUTES : 0;
    }
}
