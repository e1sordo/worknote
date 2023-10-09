package es.e1sordo.worknote.services;

import es.e1sordo.worknote.dto.DayDto;

import java.time.LocalDate;
import java.util.List;

public interface CalendarService {
    List<DayDto> getWeekdays(LocalDate from, int weeks);

    List<DayDto> getDays(LocalDate from, LocalDate to);

    DayDto getDay(LocalDate date);

    void updateDaySummary(LocalDate date, String newText);

    void updateWorkingMinutesCount(LocalDate date, int value);

    void updateDayNonWorkingStatus(LocalDate date, boolean value);

    void updateDayVacation(LocalDate date, boolean value);
}
