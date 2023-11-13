package es.e1sordo.worknote.services.impl;

import es.e1sordo.worknote.clients.XmlCalendarRuClient;
import es.e1sordo.worknote.dto.calendar.CalendarDto;
import es.e1sordo.worknote.dto.calendar.DayDto;
import es.e1sordo.worknote.dto.calendar.HolidayDto;
import es.e1sordo.worknote.models.UnusualProductionDayInfo;
import es.e1sordo.worknote.services.ProductionCalendarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static es.e1sordo.worknote.configuration.CachingConfiguration.PRODUCTION_DAYS_CACHE_NAME;

@Slf4j
@Service
@RequiredArgsConstructor
public class RussianProductionCalendarServiceImpl implements ProductionCalendarService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM.dd.yyyy");
    private static final String CACHE_TTL_IN_MILLIS = 1000 * 60 * 10 + ""; // ten minutes

    private final XmlCalendarRuClient client;

    @CacheEvict(value = PRODUCTION_DAYS_CACHE_NAME, allEntries = true)
    @Scheduled(fixedRateString = CACHE_TTL_IN_MILLIS)
    public void emptyCache() {
        log.info("Emptying {} cache", PRODUCTION_DAYS_CACHE_NAME);
    }

    @Override
    @Cacheable(PRODUCTION_DAYS_CACHE_NAME)
    public Map<LocalDate, UnusualProductionDayInfo> getUnusualDaysInfo(final LocalDate startDate, final LocalDate endDate) {
        final Map<LocalDate, UnusualProductionDayInfo> productionInfo = new HashMap<>();
        final Map<LocalDate, UnusualProductionDayInfo> specialDaysInfo = new HashMap<>();

        fillDaysInfoForYear(specialDaysInfo, startDate.getYear());
        if (startDate.getYear() != endDate.getYear()) {
            fillDaysInfoForYear(specialDaysInfo, endDate.getYear());
        }

        startDate.datesUntil(endDate).forEach(date -> {
            final UnusualProductionDayInfo info = specialDaysInfo.get(date);
            if (info != null) {
                productionInfo.put(date, info);
            }
        });

        return productionInfo;
    }

    private void fillDaysInfoForYear(final Map<LocalDate, UnusualProductionDayInfo> specialDaysInfo, final int year) {
        final CalendarDto clientResponse = client.getCalendarForYear(year);
        final List<String> holidayNames = clientResponse.getHolidays().stream()
                .map(HolidayDto::getTitle)
                .map(title -> "🇷🇺 " + title)
                .toList();
        clientResponse.getDays().forEach(day -> {
            var date = LocalDate.parse(day.getDay() + "." + year, DATE_FORMAT);
            var dayType = day.getType();
            var additionalInfo = buildAdditionalInfo(day, holidayNames);

            specialDaysInfo.put(date, new UnusualProductionDayInfo(
                    date,
                    dayType == 1,
                    dayType == 2,
                    (dayType == 1 ? 0 : dayType == 2 ? 7 : 8) * 60,
                    additionalInfo
            ));
        });
    }

    private String buildAdditionalInfo(DayDto day, List<String> holidayNames) {
        final Optional<String> holidayName = Optional.ofNullable(day.getHoliday())
                .map(holidayIndex -> holidayNames.get(holidayIndex - 1));

        if (holidayName.isPresent()) {
            return holidayName.get();
        }

        final Optional<String> movedWorkingDayInfo = Optional.ofNullable(day.getFrom())
                .map(originalDate -> "🔀 Перенос за " + changeDateFormatToRussian(originalDate));

        return movedWorkingDayInfo.orElse(null);
    }

    private String changeDateFormatToRussian(String mmDdDate) {
        final String[] split = mmDdDate.split("\\.");
        return split[1] + "." + split[0];
    }
}
