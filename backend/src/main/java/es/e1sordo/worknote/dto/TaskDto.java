package es.e1sordo.worknote.dto;

import es.e1sordo.worknote.models.JiraTaskType;

import java.util.List;

public record TaskDto(long entityId,
                      int id,
                      String code,
                      String shortCode,
                      JiraTaskType type,
                      String title,
                      String defaultValue,
                      String examples,
                      boolean closed,
                      List<PredefinedWorklogDto> predefinedWorklogs) {
}
