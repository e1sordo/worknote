package es.e1sordo.worknote.models;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = VacationEntity.TABLE_NAME)
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class VacationEntity {

    static final String TABLE_NAME = "vacations";

    @Id
    @GeneratedValue
    private Long id;

    private int totalDays;
    private LocalDate startDate;
    private LocalDate endDate;

    private boolean confirmed;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "task_id")
    private JiraTaskEntity vacationTask;

    private boolean synced;
    private LocalDateTime syncedDateTime;
}
