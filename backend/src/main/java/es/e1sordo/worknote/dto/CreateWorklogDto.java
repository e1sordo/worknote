package es.e1sordo.worknote.dto;

import java.time.LocalDate;

public record CreateWorklogDto(LocalDate date,
                               String startTime,
                               int durationInMinutes,
                               String summary,
                               String taskLabel) {
}
