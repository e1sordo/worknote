package es.e1sordo.worknote.dto;

import es.e1sordo.worknote.models.JiraTaskType;

import java.time.LocalDateTime;

public record TaskWithUsageDto(long entityId,
                               int id,
                               String code,
                               JiraTaskType type,
                               String title,
                               String examples,
                               boolean closed,
                               LocalDateTime latestWorklogDateTime) {
}
