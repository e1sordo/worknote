package es.e1sordo.worknote.controllers;

import es.e1sordo.worknote.dto.DayDto;
import es.e1sordo.worknote.services.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

    @GetMapping("/weeks")
    public List<DayDto> getWeeks(@RequestParam(defaultValue = "4") int needsToLoad) {
        return calendarService.getWeekdays(LocalDate.now(), needsToLoad);
    }

    @GetMapping("/day")
    public DayDto getDay(@RequestParam LocalDate date) {
        return calendarService.getDay(date);
    }
}
