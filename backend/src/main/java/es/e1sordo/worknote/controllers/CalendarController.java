package es.e1sordo.worknote.controllers;

import es.e1sordo.worknote.dto.DayDto;
import es.e1sordo.worknote.dto.UpdateDaySummaryDto;
import es.e1sordo.worknote.dto.UpdateDayVacationDto;
import es.e1sordo.worknote.services.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
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

    @PatchMapping("/day/{date}/summary")
    public void updateDaySummary(@PathVariable LocalDate date, @RequestBody UpdateDaySummaryDto request) {
        calendarService.updateDaySummary(date, request.newText());
    }

    @PatchMapping("/day/{date}/vacation")
    public void updateDayVacation(@PathVariable LocalDate date, @RequestBody UpdateDayVacationDto request) {
        calendarService.updateDayVacation(date, request.value());
    }
}
