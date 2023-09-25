package es.e1sordo.worknote.dto;

import java.util.List;

public record DayDto(String date,
                     boolean nonWorkingDay,
                     boolean vacation,
                     boolean reducedWorkingDay,
                     int workingMinutes, // out of 8 hours, ex.
                     String additionalInfo,

                     Integer sequenceNumber,
                     String summary,
                     List<WorklogDto> worklogs) {
}
