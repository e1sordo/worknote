package es.e1sordo.worknote.services;

import es.e1sordo.worknote.dto.analytics.TaskWithTotalTimeSpentDto;
import es.e1sordo.worknote.dto.analytics.TimeDistributionOverviewDto;

import java.util.List;

public interface AnalyticsService {
    List<TaskWithTotalTimeSpentDto> getTheMostTimeConsumingTasks();

    TimeDistributionOverviewDto getTimeDistributionByTypeOfTasksPerWeeks();
}
