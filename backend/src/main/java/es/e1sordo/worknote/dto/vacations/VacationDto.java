package es.e1sordo.worknote.dto.vacations;

import java.time.LocalDate;

public record VacationDto(long id,
                          int totalDays,
                          LocalDate startDate,
                          LocalDate endDate,
                          boolean confirmed,
                          boolean synced) {
}
