package es.e1sordo.worknote.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = JiraProjectEntity.TABLE_NAME)
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class JiraProjectEntity {

    static final String TABLE_NAME = "projects";

    @Id
    private String code;
    private String name;
    private String shortCode;
    private boolean active = true;
}
