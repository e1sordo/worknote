package es.e1sordo.worknote.mapping;

import es.e1sordo.worknote.dto.AppSettingDto;
import es.e1sordo.worknote.dto.DayDto;
import es.e1sordo.worknote.dto.TaskDto;
import es.e1sordo.worknote.dto.TaskWithUsageDto;
import es.e1sordo.worknote.dto.WorklogDto;
import es.e1sordo.worknote.models.AppSettingEntity;
import es.e1sordo.worknote.models.DayEntity;
import es.e1sordo.worknote.models.JiraTaskEntity;
import es.e1sordo.worknote.models.WorklogEntity;
import es.e1sordo.worknote.utils.Pair;

import java.time.LocalDateTime;
import java.util.List;

public final class Mappings {

    public static AppSettingDto mapToDto(final AppSettingEntity entity) {
        return new AppSettingDto(
                entity.getKey(),
                entity.getValue()
        );
    }

    public static DayDto mapToDto(final Pair<DayEntity, List<WorklogEntity>> pair) {
        final DayEntity entity = pair.first();
        final List<WorklogEntity> worklogs = pair.second();
        return new DayDto(
                entity.getDate().toString(),
                entity.isNonWorkingDay(),
                entity.isVacation(),
                entity.isReducedWorkingDay(),
                entity.getWorkingMinutes(),
                entity.getAdditionalInfo(),
                entity.getWorkedSequenceNumber(),
                entity.getSummary(),
                worklogs.stream().map(Mappings::mapToDto).toList()
        );
    }

    public static TaskDto mapToDto(final JiraTaskEntity entity) {
        return new TaskDto(
                entity.getId(),
                entity.getJiraId(),
                entity.getProject().getCode(),
                entity.getProject().getShortCode(),
                entity.getType(),
                entity.getTitle(),
                entity.getExamples(),
                entity.isClosed()
        );
    }

    public static TaskWithUsageDto mapToDtoWithUsage(final Pair<JiraTaskEntity, LocalDateTime> pair) {
        final var entity = pair.first();
        final var latestWorklogTime = pair.second();
        return new TaskWithUsageDto(
                entity.getId(),
                entity.getJiraId(),
                entity.getProject().getCode(),
                entity.getType(),
                entity.getTitle(),
                entity.getExamples(),
                entity.isClosed(),
                latestWorklogTime
        );
    }

    public static WorklogDto mapToDto(WorklogEntity entity) {
        return new WorklogDto(
                entity.getId(),
                entity.getStartTime(),
                entity.getDurationInMinutes(),
                entity.getSummary(),
                mapToDto(entity.getTask()),
                entity.getJiraId(),
                entity.isSynced()
        );
    }
}
