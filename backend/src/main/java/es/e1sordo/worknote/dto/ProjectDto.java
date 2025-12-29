package es.e1sordo.worknote.dto;

public record ProjectDto(String code,
                         String shortCode,
                         String name,
                         boolean active) {
}
