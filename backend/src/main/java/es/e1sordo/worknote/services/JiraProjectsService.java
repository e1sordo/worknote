package es.e1sordo.worknote.services;

import es.e1sordo.worknote.models.JiraProjectEntity;

import java.util.Optional;

public interface JiraProjectsService {

    Optional<JiraProjectEntity> findByCode(String code);

    JiraProjectEntity create(String code);
}
