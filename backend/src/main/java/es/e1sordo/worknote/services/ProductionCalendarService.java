package es.e1sordo.worknote.services;

import es.e1sordo.worknote.models.UnusualProductionDayInfo;

import java.time.LocalDate;
import java.util.Map;

public interface ProductionCalendarService {

    Map<LocalDate, UnusualProductionDayInfo> getUnusualDaysInfo(LocalDate startDate, LocalDate endDate);
}
