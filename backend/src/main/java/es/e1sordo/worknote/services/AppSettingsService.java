package es.e1sordo.worknote.services;

import es.e1sordo.worknote.enums.AppSettingKeys;
import es.e1sordo.worknote.models.AppSettingEntity;

public interface AppSettingsService {

    void set(AppSettingKeys key, String value);

    AppSettingEntity get(AppSettingKeys key);
}
