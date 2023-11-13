package es.e1sordo.worknote.clients;

import es.e1sordo.worknote.dto.calendar.CalendarDto;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.stereotype.Component;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

@Slf4j
@Component
@RequiredArgsConstructor
public class XmlCalendarRuClient {

    private static final String API_URL = "https://xmlcalendar.ru/data/ru/%d/calendar.xml";

    private final MappingJackson2XmlHttpMessageConverter converter;

    @SneakyThrows
    public CalendarDto getCalendarForYear(int year) {
        var apiUrl = API_URL.formatted(year);
        log.info("Send GET request to {}", apiUrl);

        final var url = new URL(apiUrl);
        final var connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        final var response = new StringBuilder();
        try (final var scanner = new Scanner(connection.getInputStream())) {
            while (scanner.hasNextLine()) {
                response.append(scanner.nextLine());
            }
        }
        final String xmlData = response.toString();

        log.info("Client response {}", xmlData);

        return converter.getObjectMapper().readValue(xmlData, CalendarDto.class);
    }
}
