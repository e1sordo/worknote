package es.e1sordo.worknote.services.impl;

import es.e1sordo.worknote.models.DayEntity;
import es.e1sordo.worknote.models.JiraProjectEntity;
import es.e1sordo.worknote.models.JiraTaskEntity;
import es.e1sordo.worknote.models.JiraTaskType;
import es.e1sordo.worknote.models.WorklogEntity;
import es.e1sordo.worknote.services.CalendarService;
import es.e1sordo.worknote.utils.Pair;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;

@Service
@Profile("demo")
public class DemoCalendarServiceImpl implements CalendarService {

    private static final JiraProjectEntity ABCDEF_PROJECT = new JiraProjectEntity("ABCDEF", "", "ABC");
    private static final JiraProjectEntity XYZ_PROJECT = new JiraProjectEntity("XYZ", "", "XYZ");

    private static final JiraTaskEntity TASK_DAILY = new JiraTaskEntity(
            77L, 586, ABCDEF_PROJECT, JiraTaskType.PROCESS_MAINTENANCE, false, "Daily", "Daily, DSM", emptyList()
    );
    private static final JiraTaskEntity TASK_DEVELOPMENT_1 = new JiraTaskEntity(
            78L, 1987, ABCDEF_PROJECT, JiraTaskType.DEVELOPMENT, false, "Feature A", "Feature A", emptyList()
    );
    private static final JiraTaskEntity TASK_STUDY = new JiraTaskEntity(
            79L, 35, XYZ_PROJECT, JiraTaskType.TRAINING_AND_DEVELOPMENT, false, "Trainings", "Trainings", emptyList()
    );
    private static final JiraTaskEntity TASK_MEETINGS = new JiraTaskEntity(
            80L, 511, ABCDEF_PROJECT, JiraTaskType.ORGANIZING_ACTIVITIES, false, "Activities", "Activities", emptyList()
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
                            new WorklogEntity(1L, null, LocalTime.of(8, 30), 15, "Stand-up meeting", TASK_DAILY, null, false)
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
                            new WorklogEntity(1L, null, LocalTime.of(8, 30), 15, "Stand-up meeting", TASK_DAILY, 77877L, true),
                            new WorklogEntity(2L, null, LocalTime.of(9, 0), 300, "Code Review, Bug Fix", TASK_DEVELOPMENT_1, 454565L, true),
                            new WorklogEntity(3L, null, LocalTime.of(17, 0), 105, "Team Retro", TASK_DAILY, null, false)
                    )
            ),
            3, new DemoDay(
                    false,
                    false,
                    480,
                    null,
                    75,
                    "Jonathan went on vacation for 2 weeks 🏖️",
                    List.of(
                            new WorklogEntity(1L, null, LocalTime.of(8, 30), 15, "Stand-up meeting", TASK_DAILY, 77877L, true),
                            new WorklogEntity(2L, null, LocalTime.of(9, 0), 465, "Task investigation", TASK_DEVELOPMENT_1, 454565L, true)
                    )
            ),
            4, new DemoDay(
                    false,
                    false,
                    480,
                    null,
                    74,
                    null,
                    List.of(
                            new WorklogEntity(2L, null, LocalTime.of(10, 0), 480, "Finish online course on information security", TASK_STUDY, null, false)
                    )
            ),
            5, new DemoDay(
                    false,
                    false,
                    480,
                    null,
                    73,
                    "I got a 2-day corporate training appt 🥲",
                    List.of(
                            new WorklogEntity(2L, null, LocalTime.of(10, 0), 480, "Online course on information security", TASK_STUDY, 454565L, true)
                    )
            ),
            6, new DemoDay(
                    false,
                    false,
                    480,
                    null,
                    72,
                    null,
                    List.of(
                            new WorklogEntity(1L, null, LocalTime.of(8, 30), 30, "Stand-up meeting", TASK_DAILY, 77877L, true),
                            new WorklogEntity(2L, null, LocalTime.of(10, 0), 120, "Meeting with Sally on microservice architecture", TASK_MEETINGS, 454565L, true),
                            new WorklogEntity(3L, null, LocalTime.of(13, 0), 330, "Testing", TASK_DEVELOPMENT_1, 454565L, true)
                    )
            )
    );

    record DemoDay(boolean nonWorkingDay,
                   boolean reducedWorkingDay,
                   int workingMinutes,
                   String additionalInfo,
                   Integer sequenceNumber,
                   String summary,
                   List<WorklogEntity> worklogs) {
        public Pair<DayEntity, List<WorklogEntity>> forDay(LocalDate day) {
            return Pair.of(
                    new DayEntity(day, nonWorkingDay, false, reducedWorkingDay, workingMinutes, additionalInfo, sequenceNumber, summary),
                    worklogs
            );
        }
    }

    @Override
    public List<Pair<DayEntity, List<WorklogEntity>>> getWeekdays(final LocalDate from, final int weeks) {
        final var endOfCurrentWeek = from.with(DayOfWeek.SUNDAY);
        var endOfWeekExclusively = endOfCurrentWeek.plusDays(1);

        if (endOfCurrentWeek.equals(from)) {
            endOfWeekExclusively = endOfWeekExclusively.plusWeeks(1);
        }

        final var begin = from.minusWeeks(weeks).with(DayOfWeek.MONDAY);
        return getDays(begin, endOfWeekExclusively);
    }

    @Override
    public List<Pair<DayEntity, List<WorklogEntity>>> getDays(final LocalDate from, final LocalDate to) {
        final var list = new LinkedList<Pair<DayEntity, List<WorklogEntity>>>();

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

    private Pair<DayEntity, List<WorklogEntity>> generateEmptyDay(LocalDate date) {
        return Pair.of(
                new DayEntity(
                        date,
                        List.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY).contains(date.getDayOfWeek()),
                        false,
                        false,
                        List.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY).contains(date.getDayOfWeek()) ? 0 : 480,
                        null,
                        null,
                        null
                ),
                emptyList()
        );
    }

    @Override
    public Pair<DayEntity, List<WorklogEntity>> getDay(final LocalDate date) {
        return null;
    }

    @Override
    public void updateWorkingMinutesCount(final LocalDate date, final int value) {
    }

    @Override
    public void updateDayNonWorkingStatus(final LocalDate date, final boolean value) {
    }

    @Override
    public void updateDaySummary(final LocalDate date, final String newText) {
    }

    @Override
    public void updateDayVacation(final LocalDate date, final boolean value) {
    }

    @Override
    public void updateNewFirstWorkingDay(final LocalDate from) {
    }
}
