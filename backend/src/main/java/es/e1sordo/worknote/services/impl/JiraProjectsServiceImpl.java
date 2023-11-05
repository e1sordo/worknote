package es.e1sordo.worknote.services.impl;

import es.e1sordo.worknote.models.JiraProjectEntity;
import es.e1sordo.worknote.repositories.ProjectRepository;
import es.e1sordo.worknote.services.JiraProjectsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JiraProjectsServiceImpl implements JiraProjectsService {

    private final ProjectRepository repository;

    @Override
    public Optional<JiraProjectEntity> findByCode(final String code) {
        return repository.findByCode(code);
    }

    @Override
    public JiraProjectEntity create(final String code) {
        return repository.save(new JiraProjectEntity(code, code, code));
    }
}
