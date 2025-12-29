package es.e1sordo.worknote.dto.vacations;

import java.time.LocalDate;

public record UpsertVacationDto(Long id,
                                LocalDate startDate,
                                LocalDate endDate,
                                boolean confirmed) {
}
