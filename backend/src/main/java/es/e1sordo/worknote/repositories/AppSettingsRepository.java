package es.e1sordo.worknote.repositories;

import es.e1sordo.worknote.enums.AppSettingKeys;
import es.e1sordo.worknote.models.AppSettingEntity;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppSettingsRepository extends ListCrudRepository<AppSettingEntity, AppSettingKeys> {
}
