package es.e1sordo.worknote.dto;

public record UpsertProjectDto(String code,
                               String name,
                               boolean active) {
}
