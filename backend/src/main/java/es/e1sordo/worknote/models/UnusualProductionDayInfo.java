package es.e1sordo.worknote.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class UnusualProductionDayInfo {

    private LocalDate date;
    private boolean nonWorkingDay;

    private boolean reducedWorkingDay;
    private int workingMinutes;

    private String additionalInfo;
}
