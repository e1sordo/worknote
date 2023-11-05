package es.e1sordo.worknote.dto;

import es.e1sordo.worknote.models.JiraTaskEntity;

public record SearchTaskResult(JiraTaskEntity entity,
                               String highlightedTitle,
                               String highlightedExamples) {
}
