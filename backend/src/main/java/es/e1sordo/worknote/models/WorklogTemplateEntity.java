package es.e1sordo.worknote.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

//@Entity
//@Table(name = WorklogTemplateEntity.TABLE_NAME)
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class WorklogTemplateEntity {

    static final String TABLE_NAME = "worklog-templates";

    //    @Id
//    @SequenceGenerator(name = PK_SEQUENCE_GENERATOR_NAME, sequenceName = PK_SEQUENCE_NAME, allocationSize = 1)
//    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = PK_SEQUENCE_GENERATOR_NAME)
    private Long id;

    // https://stackoverflow.com/questions/5183630/calendar-recurring-repeating-events-best-storage-method
    // https://stackoverflow.com/questions/23835171/cron-expression-parsing-into-java-date

    private String cronExpression;
    private int usualDurationInMinutes;
}
