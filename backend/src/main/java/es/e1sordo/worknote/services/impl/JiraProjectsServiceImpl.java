package es.e1sordo.worknote.services.impl;

import es.e1sordo.worknote.dto.UpsertProjectDto;
import es.e1sordo.worknote.models.JiraProjectEntity;
import es.e1sordo.worknote.repositories.ProjectRepository;
import es.e1sordo.worknote.services.JiraProjectsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class JiraProjectsServiceImpl implements JiraProjectsService {

    private final ProjectRepository repository;

    @Override
    public List<JiraProjectEntity> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<JiraProjectEntity> findByCode(final String code) {
        return repository.findByCode(code);
    }

    @Override
    public JiraProjectEntity create(final String code) {
        return repository.save(new JiraProjectEntity(code, code, code, true));
    }

    @Override
    public JiraProjectEntity create(final JiraProjectEntity entity) {
        return repository.save(entity);
    }

    @Override
    public JiraProjectEntity upsert(UpsertProjectDto request) {
        log.info("Upsert project");

        final JiraProjectEntity project = findByCode(request.code())
                .orElseGet(() -> create(request.code()));

        project.setActive(request.active());
        project.setName(request.name());

        final JiraProjectEntity result = repository.save(project);

        return result;
    }
}
