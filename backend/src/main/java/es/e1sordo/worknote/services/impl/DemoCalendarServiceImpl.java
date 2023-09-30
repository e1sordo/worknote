package es.e1sordo.worknote.services.impl;

import es.e1sordo.worknote.dto.DayDto;
import es.e1sordo.worknote.dto.TaskDto;
import es.e1sordo.worknote.dto.WorklogDto;
import es.e1sordo.worknote.models.JiraTaskType;
import es.e1sordo.worknote.services.CalendarService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
@Profile("demo")
public class DemoCalendarServiceImpl implements CalendarService {

    private static final TaskDto TASK_DAILY = new TaskDto(
            77L, 586, "ABCDEF", "ABC", JiraTaskType.PROCESS_MAINTENANCE, "Daily", "Daily, DSM", false
    );
    private static final TaskDto TASK_DEVELOPMENT_1 = new TaskDto(
            78L, 1987, "ABCDEF", "ABC", JiraTaskType.DEVELOPMENT, "Feature A", "Feature A", false
    );
    private static final TaskDto TASK_STUDY = new TaskDto(
            79L, 35, "XYZ", "XYZ", JiraTaskType.TRAINING_AND_DEVELOPMENT, "Trainings", "Trainings", false
    );
    private static final TaskDto TASK_MEETINGS = new TaskDto(
            80L, 511, "ABCDEF", "ABC", JiraTaskType.ORGANIZING_ACTIVITIES, "Activities", "Activities", false
    );

    private static final Map<Integer, DemoDay> DEMO_DATA = Map.of(
            0, new DemoDay(
                    false,
                    false,
                    480,
                    null,
                    77,
                    null,
                    List.of(
                            new WorklogDto(1L, LocalTime.of(8, 30), 15, "Stand-up meeting", TASK_DAILY, null, false)
                    )
            ),
            1, new DemoDay(
                    true,
                    false,
                    0,
                    "🏴󠁧󠁢󠁥󠁮󠁧󠁿 Late Summer Bank Holiday",
                    null,
                    null,
                    List.of()
            ),
            2, new DemoDay(
                    false,
                    false,
                    480,
                    null,
                    76,
                    null,
                    List.of(
                            new WorklogDto(1L, LocalTime.of(8, 30), 15, "Stand-up meeting", TASK_DAILY, 77877L, true),
                            new WorklogDto(2L, LocalTime.of(9, 0), 300, "Code Review, Bug Fix", TASK_DEVELOPMENT_1, 454565L, true),
                            new WorklogDto(3L, LocalTime.of(17, 0), 105, "Team Retro", TASK_DAILY, null, false)
                    )
            ),
            3, new DemoDay(
                    false,
                    false,
                    480,
                    null,
                    76,
                    "Jonathan went on vacation for 2 weeks 🏖️",
                    List.of(
                            new WorklogDto(1L, LocalTime.of(8, 30), 15, "Stand-up meeting", TASK_DAILY, 77877L, true),
                            new WorklogDto(2L, LocalTime.of(9, 0), 465, "Task investigation", TASK_DEVELOPMENT_1, 454565L, true)
                    )
            ),
            4, new DemoDay(
                    false,
                    false,
                    480,
                    null,
                    76,
                    null,
                    List.of(
                            new WorklogDto(2L, LocalTime.of(10, 0), 480, "Finish online course on information security", TASK_STUDY, null, false)
                    )
            ),
            5, new DemoDay(
                    false,
                    false,
                    480,
                    null,
                    75,
                    "I got a 2-day corporate training appt 🥲",
                    List.of(
                            new WorklogDto(2L, LocalTime.of(10, 0), 480, "Online course on information security", TASK_STUDY, 454565L, true)
                    )
            ),
            6, new DemoDay(
                    false,
                    false,
                    480,
                    null,
                    75,
                    null,
                    List.of(
                            new WorklogDto(1L, LocalTime.of(8, 30), 30, "Stand-up meeting", TASK_DAILY, 77877L, true),
                            new WorklogDto(2L, LocalTime.of(10, 0), 120, "Meeting with Sally on microservice architecture", TASK_MEETINGS, 454565L, true),
                            new WorklogDto(3L, LocalTime.of(13, 0), 330, "Testing", TASK_DEVELOPMENT_1, 454565L, true)
                    )
            )
    );

    record DemoDay(boolean nonWorkingDay,
                   boolean reducedWorkingDay,
                   int workingMinutes,
                   String additionalInfo,
                   Integer sequenceNumber,
                   String summary,
                   List<WorklogDto> worklogs) {
        public DayDto forDay(LocalDate day) {
            return new DayDto(day.toString(), nonWorkingDay, false, reducedWorkingDay, workingMinutes, additionalInfo, sequenceNumber, summary, worklogs);
        }
    }

    @Override
    public List<DayDto> getWeekdays(final LocalDate from, final int weeks) {
        final var endOfCurrentWeek = from.with(DayOfWeek.SUNDAY);
        var endOfWeekExclusively = endOfCurrentWeek.plusDays(1);

        if (endOfCurrentWeek.equals(from)) {
            endOfWeekExclusively = endOfWeekExclusively.plusWeeks(1);
        }

        final var begin = from.minusWeeks(weeks).with(DayOfWeek.MONDAY);
        return getDays(begin, endOfWeekExclusively);
    }

    @Override
    public List<DayDto> getDays(final LocalDate from, final LocalDate to) {
        final var list = new LinkedList<DayDto>();

        int testDayCounter = 0;

        var currentDay = to;

        while (currentDay.isAfter(from)) {
            currentDay = currentDay.minusDays(1);

            if (currentDay.isAfter(LocalDate.now())) {
                list.addFirst(generateEmptyDay(currentDay));
                continue;
            }
            DemoDay dayTemplate = DEMO_DATA.get(testDayCounter);
            if (dayTemplate == null || List.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY).contains(currentDay.getDayOfWeek())) {
                list.addFirst(generateEmptyDay(currentDay));
            } else {
                list.addFirst(dayTemplate.forDay(currentDay));
                testDayCounter++;
            }
        }

        return list;
    }

    private DayDto generateEmptyDay(LocalDate date) {
        return new DayDto(
                date.toString(),
                List.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY).contains(date.getDayOfWeek()),
                false,
                false,
                List.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY).contains(date.getDayOfWeek()) ? 0 : 480,
                null,
                null,
                null,
                Collections.emptyList()
        );
    }

    @Override
    public DayDto getDay(final LocalDate date) {
        return null;
    }

    @Override
    public void updateDaySummary(final LocalDate date, final String newText) {
    }

    @Override
    public void updateDayVacation(final LocalDate date, final boolean value) {
    }
}
