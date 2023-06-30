package es.e1sordo.worknote.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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
}
