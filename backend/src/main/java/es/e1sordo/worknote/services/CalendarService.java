package es.e1sordo.worknote.services;

import es.e1sordo.worknote.dto.DayDto;

import java.time.LocalDate;
import java.util.List;

public interface CalendarService {
    List<DayDto> getWeekdays(LocalDate from, int weeks);

    List<DayDto> getDays(LocalDate from, LocalDate to);

    DayDto getDay(LocalDate date);
}
