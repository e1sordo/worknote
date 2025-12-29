package es.e1sordo.worknote.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Entity
@Table(name = DayEntity.TABLE_NAME)
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DayEntity {

    static final String TABLE_NAME = "days";

    @Id
    private LocalDate date;
    private boolean nonWorkingDay;
    private boolean vacation;

    private boolean reducedWorkingDay;
    private int workingMinutes; // out of 8 hours, ex.....   0 if weekday
    private String additionalInfo;
    private String summary;
}
