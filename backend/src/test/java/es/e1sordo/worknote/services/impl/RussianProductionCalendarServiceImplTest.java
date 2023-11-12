package es.e1sordo.worknote.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import es.e1sordo.worknote.clients.XmlCalendarRuClient;
import es.e1sordo.worknote.dto.calendar.CalendarDto;
import es.e1sordo.worknote.models.UnusualProductionDayInfo;
import es.e1sordo.worknote.services.ProductionCalendarService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;
import static java.util.function.Function.identity;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RussianProductionCalendarServiceImplTest {

    private final XmlMapper mapper = new XmlMapper();
    private final XmlCalendarRuClient client = mock();
    private final ProductionCalendarService service = new RussianProductionCalendarServiceImpl(client);

    private static final String XML_DATA_2023 = """
            <calendar year="2023" lang="en" date="2022.08.07" country="ru">
                 <holidays>
                 </holidays>
                 <days>
                     <day d="12.28" t="3"/>
                     <day d="12.30" t="1" f="12.28"/>
                     <day d="12.31" t="1" f="01.07"/>
                 </days>
             </calendar>
             """;
    private static final String XML_DATA_2024 = """
            <calendar year="2024" lang="en" date="2023.09.30">
                <holidays>
                    <holiday id="1" title="New Year's Day" />
                    <holiday id="2" title="Orthodox Christmas Day" />
                    <holiday id="3" title="Defender of the Fatherland Day" />
                    <holiday id="4" title="International Women's Day" />
                    <holiday id="5" title="Spring and Labor Day" />
                    <holiday id="6" title="Victory Day" />
                    <holiday id="7" title="Russia Day" />
                    <holiday id="8" title="Unity Day" />
                </holidays>
                <days>
                    <day d="01.01" t="1" h="1"/>
                    <day d="01.02" t="1" h="1"/>
                    <day d="01.03" t="1" h="1"/>
                    <day d="01.04" t="1" h="1"/>
                    <day d="01.05" t="1" h="1"/>
                    <day d="01.06" t="1" h="1"/>
                    <day d="01.07" t="1" h="2"/>
                    <day d="01.08" t="1" h="1"/>
                    <day d="02.22" t="2"/>
                    <day d="02.23" t="1" h="3"/>
                    <day d="03.07" t="2"/>
                    <day d="03.08" t="1" h="4"/>
                    <day d="04.27" t="3"/>
                    <day d="04.29" t="1" f="04.27"/>
                    <day d="04.30" t="1" f="11.02"/>
                    <day d="05.01" t="1" h="5"/>
                    <day d="05.08" t="2"/>
                    <day d="05.09" t="1" h="6"/>
                    <day d="05.10" t="1" f="01.06"/>
                    <day d="06.11" t="2"/>
                    <day d="06.12" t="1" h="7"/>
                    <day d="11.02" t="2"/>
                    <day d="11.04" t="1" h="8"/>
                    <day d="12.28" t="3"/>
                    <day d="12.30" t="1" f="12.28"/>
                    <day d="12.31" t="1" f="01.07"/>
                </days>
            </calendar>
            """;

    @Test
    void testEmptyCase() throws JsonProcessingException {
        // given
        when(client.getCalendarForYear(2024)).thenReturn(mapper.readValue(XML_DATA_2024, CalendarDto.class));

        // when
        final Map<LocalDate, UnusualProductionDayInfo> actualResult = service.getUnusualDaysInfo(
                LocalDate.of(2024, 7, 5),
                LocalDate.of(2024, 7, 12)
        );

        // then
        assertEquals(emptyMap(), actualResult);
    }

    @Test
    void testNormalCase() throws JsonProcessingException {
        // given
        when(client.getCalendarForYear(2024)).thenReturn(mapper.readValue(XML_DATA_2024, CalendarDto.class));

        // when
        final Map<LocalDate, UnusualProductionDayInfo> actualResult = service.getUnusualDaysInfo(
                LocalDate.of(2024, 4, 26),
                LocalDate.of(2024, 5, 3)
        );

        // then
        assertEquals(
                List.of(
                        new UnusualProductionDayInfo(
                                LocalDate.of(2024, 4, 27), false, false, 480, null
                        ),

                        new UnusualProductionDayInfo(
                                LocalDate.of(2024, 4, 29), true, false, 0, "🔀 Перенос за 27.04"
                        ),

                        new UnusualProductionDayInfo(
                                LocalDate.of(2024, 4, 30), true, false, 0, "🔀 Перенос за 02.11"
                        ),

                        new UnusualProductionDayInfo(
                                LocalDate.of(2024, 5, 1), true, false, 0, "🇷🇺 Spring and Labor Day"
                        )
                ).stream().collect(Collectors.toMap(UnusualProductionDayInfo::getDate, identity())),
                actualResult
        );
    }

    @Test
    void testReducesWorkingDaysCase() throws JsonProcessingException {
        // given
        when(client.getCalendarForYear(2024)).thenReturn(mapper.readValue(XML_DATA_2024, CalendarDto.class));

        // when
        final Map<LocalDate, UnusualProductionDayInfo> actualResult = service.getUnusualDaysInfo(
                LocalDate.of(2024, 5, 6),
                LocalDate.of(2024, 5, 13)
        );

        // then
        assertEquals(
                List.of(
                        new UnusualProductionDayInfo(
                                LocalDate.of(2024, 5, 8), false, true, 420, null
                        ),

                        new UnusualProductionDayInfo(
                                LocalDate.of(2024, 5, 9), true, false, 0, "🇷🇺 Victory Day"
                        ),

                        new UnusualProductionDayInfo(
                                LocalDate.of(2024, 5, 10), true, false, 0, "🔀 Перенос за 06.01"
                        )
                ).stream().collect(Collectors.toMap(UnusualProductionDayInfo::getDate, identity())),
                actualResult
        );
    }

    @Test
    void testTwoYearsCase() throws JsonProcessingException {
        // given
        when(client.getCalendarForYear(2023)).thenReturn(mapper.readValue(XML_DATA_2023, CalendarDto.class));
        when(client.getCalendarForYear(2024)).thenReturn(mapper.readValue(XML_DATA_2024, CalendarDto.class));

        // when
        final Map<LocalDate, UnusualProductionDayInfo> actualResult = service.getUnusualDaysInfo(
                LocalDate.of(2023, 12, 29),
                LocalDate.of(2024, 1, 4)
        );

        // then
        assertEquals(
                List.of(
                        new UnusualProductionDayInfo(
                                LocalDate.of(2023, 12, 30), true, false, 0, "🔀 Перенос за 28.12"
                        ),

                        new UnusualProductionDayInfo(
                                LocalDate.of(2023, 12, 31), true, false, 0, "🔀 Перенос за 07.01"
                        ),

                        new UnusualProductionDayInfo(
                                LocalDate.of(2024, 1, 1), true, false, 0, "🇷🇺 New Year's Day"
                        ),

                        new UnusualProductionDayInfo(
                                LocalDate.of(2024, 1, 2), true, false, 0, "🇷🇺 New Year's Day"
                        ),

                        new UnusualProductionDayInfo(
                                LocalDate.of(2024, 1, 3), true, false, 0, "🇷🇺 New Year's Day"
                        )
                ).stream().collect(Collectors.toMap(UnusualProductionDayInfo::getDate, identity())),
                actualResult
        );
    }
}
