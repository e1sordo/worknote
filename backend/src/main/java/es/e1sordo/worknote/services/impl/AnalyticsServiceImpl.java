package es.e1sordo.worknote.services.impl;

import es.e1sordo.worknote.dto.analytics.TaskWithTotalTimeSpentDto;
import es.e1sordo.worknote.dto.analytics.TimeDistributionDto;
import es.e1sordo.worknote.dto.analytics.TimeDistributionOverviewDto;
import es.e1sordo.worknote.models.JiraTaskEntity;
import es.e1sordo.worknote.models.JiraTaskType;
import es.e1sordo.worknote.models.WorklogEntity;
import es.e1sordo.worknote.repositories.TaskRepository;
import es.e1sordo.worknote.repositories.WorklogRepository;
import es.e1sordo.worknote.services.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final TaskRepository taskRepository;
    private final WorklogRepository worklogRepository;

    @Override
    public List<TaskWithTotalTimeSpentDto> getTheMostTimeConsumingTasks() {
        log.info("Find top 15 tasks with total duration");

        List<TaskWithTotalTimeSpentDto> result = new ArrayList<>();

        List<Object[]> top15TasksWithTotalDuration = taskRepository.findTop15TasksWithTotalDuration();
        for (Object[] task : top15TasksWithTotalDuration) {
            JiraTaskEntity jiraTask = (JiraTaskEntity) task[0];
            long totalDuration = (Long) task[1];
            long worklogsCount = (Long) task[2];

            result.add(mapToDtoWithTotalDuration(jiraTask, totalDuration, worklogsCount));
        }

        return result;
    }

    @Override
    public TimeDistributionOverviewDto getTimeDistributionByTypeOfTasksPerWeeks() {
        log.info("Find time distribution by task type");

        final var endOfCurrentWeekExclusively = LocalDate.now().with(DayOfWeek.SUNDAY).plusDays(1);
        final var begin = endOfCurrentWeekExclusively.minusWeeks(10).with(DayOfWeek.MONDAY);
        final List<WorklogEntity> worklogs = worklogRepository.findByDateBetween(begin, endOfCurrentWeekExclusively);
        worklogs.sort(comparing(WorklogEntity::getDate));

        List<TimeDistributionDto> result = new ArrayList<>();

        final Map<LocalDate, List<WorklogEntity>> worklogsGroupedByDate = worklogs.stream()
                .collect(groupingBy(WorklogEntity::getDate));

        Map<JiraTaskType, Integer> currentDistributionMap = prepareTimeDistributionMap();
        for (LocalDate date : begin.datesUntil(endOfCurrentWeekExclusively).toList()) {
            final List<WorklogEntity> dateWorklogs = worklogsGroupedByDate.getOrDefault(date, emptyList());
            for (WorklogEntity dateWorklog : dateWorklogs) {
                currentDistributionMap.computeIfPresent(
                        dateWorklog.getTask().getType(),
                        (type, value) -> value + dateWorklog.getDurationInMinutes()
                );
            }

            if (date.equals(date.with(DayOfWeek.SUNDAY))) {
                result.add(new TimeDistributionDto(currentDistributionMap, date));
                currentDistributionMap = prepareTimeDistributionMap();
            }
        }

        return new TimeDistributionOverviewDto(result);
    }

    private Map<JiraTaskType, Integer> prepareTimeDistributionMap() {
        return Arrays.stream(JiraTaskType.values())
                .collect(toMap(Function.identity(), type -> 0, (x, y) -> y, LinkedHashMap::new));
    }

    protected static int getWeekOfYear(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.WEEK_OF_YEAR);
    }

//    private static LocalDate getWeekEndDate(MyObject object) {
//        return object.getDate().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
//    }

    private TaskWithTotalTimeSpentDto mapToDtoWithTotalDuration(JiraTaskEntity entity,
                                                                long totalDuration,
                                                                long worklogsCount) {
        return new TaskWithTotalTimeSpentDto(
                entity.getId(),
                entity.getJiraId(),
                entity.getProject().getCode(),
                entity.getType(),
                entity.getTitle(),
                totalDuration,
                worklogsCount
        );
    }
}
