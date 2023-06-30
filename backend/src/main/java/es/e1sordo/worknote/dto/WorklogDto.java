package es.e1sordo.worknote.dto;

import java.time.LocalTime;

public record WorklogDto(long id,
                         LocalTime startTime,
                         int durationInMinutes,
                         String summary,
                         TaskDto task,
                         Long jiraId,
                         boolean synced) {
}
