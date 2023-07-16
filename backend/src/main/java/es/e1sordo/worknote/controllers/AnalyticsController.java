package es.e1sordo.worknote.controllers;

import es.e1sordo.worknote.dto.analytics.TaskWithTotalTimeSpentDto;
import es.e1sordo.worknote.dto.analytics.TimeDistributionOverviewDto;
import es.e1sordo.worknote.services.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService service;

    @GetMapping("/tasks/most-time-consuming")
    public List<TaskWithTotalTimeSpentDto> getTheMostTimeConsumingTasks() {
        return service.getTheMostTimeConsumingTasks();
    }

    @GetMapping("/time-distribution")
    public TimeDistributionOverviewDto getTimeDistributionByTypeOfTasksPerWeeks() {
        return service.getTimeDistributionByTypeOfTasksPerWeeks();
    }
}
