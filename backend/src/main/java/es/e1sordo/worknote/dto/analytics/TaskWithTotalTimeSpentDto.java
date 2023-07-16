package es.e1sordo.worknote.dto.analytics;

import es.e1sordo.worknote.models.JiraTaskType;

public record TaskWithTotalTimeSpentDto(long entityId,
                                        int id,
                                        String code,
                                        JiraTaskType type,
                                        String title,
                                        long totalMinutesSpent,
                                        long worklogsCount) {
}
