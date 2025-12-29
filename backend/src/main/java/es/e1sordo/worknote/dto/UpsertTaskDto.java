package es.e1sordo.worknote.dto;

import es.e1sordo.worknote.models.JiraTaskType;

public record UpsertTaskDto(int id, // jiraId
                            String code,
                            JiraTaskType type,
                            String title,
                            String defaultValue,
                            String examples,
                            boolean closed) {
}
