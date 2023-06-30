package es.e1sordo.worknote.services;

import es.e1sordo.worknote.dto.DayDto;
import es.e1sordo.worknote.models.DayEntity;
import es.e1sordo.worknote.repositories.DayRepository;
import es.e1sordo.worknote.repositories.WorklogRepository;
import es.e1sordo.worknote.services.impl.CalendarServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;

class CalendarServiceTest {

    private final DayRepository dayRepository = Mockito.mock(DayRepository.class);
    private final WorklogRepository worklogRepository = Mockito.mock(WorklogRepository.class);
    private final CalendarService service = new CalendarServiceImpl(dayRepository, worklogRepository);


    @Test
    void test() {
        final LocalDate from = LocalDate.of(2023, 5, 29);
        final LocalDate to = LocalDate.of(2023, 6, 4);

        when(dayRepository.findBetween(from, to))
                .thenReturn(List.of(
                        new DayEntity(
                                LocalDate.of(2023, 5, 29),
                                false,
                                false,
                                480,
                                null,
                                110,
                                "Что-то делал"
                        ),
                        new DayEntity(
                                LocalDate.of(2023, 5, 30),
                                false,
                                false,
                                480,
                                null,
                                111,
                                "Попытка настройки рабочего окружения"
                        ),
                        new DayEntity(
                                LocalDate.of(2023, 5, 31),
                                false,
                                false,
                                480,
                                null,
                                112,
                                "Много звонков"
                        ),
                        new DayEntity(
                                LocalDate.of(2023, 6, 1),
                                false,
                                false,
                                480,
                                null,
                                113,
                                "Применение алгоритмов при расчете векторов"
                        ),
                        new DayEntity(
                                LocalDate.of(2023, 6, 2),
                                false,
                                false,
                                480,
                                null,
                                114,
                                "Настройка Сонара"
                        )
                ));

        final List<DayDto> dayDtos = service.getWeekdays(from, , to);
        System.out.println(dayDtos);
    }
}
