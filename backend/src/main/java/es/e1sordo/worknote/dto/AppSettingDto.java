package es.e1sordo.worknote.dto;

import es.e1sordo.worknote.enums.AppSettingKeys;

public record AppSettingDto(AppSettingKeys key, String value) {
}
