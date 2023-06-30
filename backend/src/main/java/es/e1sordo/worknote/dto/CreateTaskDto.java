package es.e1sordo.worknote.dto;

public record CreateTaskDto(Integer id,
                            String projectCode,
                            String title) {
}
