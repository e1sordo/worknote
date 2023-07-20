package es.e1sordo.worknote.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Entity
@Table(name = JiraTaskEntity.TABLE_NAME)
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class JiraTaskEntity {

    static final String TABLE_NAME = "tasks";

    @Id
    @GeneratedValue
    private Long id;

    private int jiraId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_code")
    private JiraProjectEntity project;

    @Enumerated(value = EnumType.STRING)
    private JiraTaskType type;

    @Column(columnDefinition = "boolean default false")
    private boolean closed;

    private String title;
    private String examples;

    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<WorklogEntity> worklogs;
}
