package es.e1sordo.worknote.services;

import es.e1sordo.worknote.models.DayEntity;
import es.e1sordo.worknote.models.WorklogEntity;
import es.e1sordo.worknote.utils.Pair;

import java.time.LocalDate;
import java.util.List;

public interface CalendarService {
    List<Pair<DayEntity, List<WorklogEntity>>> getWeekdays(LocalDate from, int weeks);

    List<Pair<DayEntity, List<WorklogEntity>>> getDays(LocalDate from, LocalDate to);

    Pair<DayEntity, List<WorklogEntity>> getDay(LocalDate date);

    void updateDaySummary(LocalDate date, String newText);

    void updateWorkingMinutesCount(LocalDate date, int value);

    void updateDayNonWorkingStatus(LocalDate date, boolean value);

    void updateDayVacation(LocalDate date, boolean value);

    void updateNewFirstWorkingDay(LocalDate from);
}
