package es.e1sordo.worknote.dto;

import es.e1sordo.worknote.models.JiraTaskType;

public record TaskDto(long entityId,
                      int id,
                      String code,
                      String shortCode,
                      JiraTaskType type,
                      String title,
                      String examples,
                      boolean closed) {
}
