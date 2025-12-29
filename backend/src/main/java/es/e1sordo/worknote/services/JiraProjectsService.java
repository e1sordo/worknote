package es.e1sordo.worknote.services;

import es.e1sordo.worknote.dto.UpsertProjectDto;
import es.e1sordo.worknote.models.AppSettingEntity;
import es.e1sordo.worknote.models.JiraProjectEntity;

import java.util.List;
import java.util.Optional;

public interface JiraProjectsService {

    List<JiraProjectEntity> findAll();

    Optional<JiraProjectEntity> findByCode(String code);

    JiraProjectEntity create(String code);

    JiraProjectEntity create(JiraProjectEntity entity);

    JiraProjectEntity upsert(UpsertProjectDto request);
}
