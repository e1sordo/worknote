package es.e1sordo.worknote.services;

import es.e1sordo.worknote.dto.CreateWorklogDto;
import es.e1sordo.worknote.models.JiraTaskEntity;
import es.e1sordo.worknote.models.WorklogEntity;

import java.util.List;

public interface WorklogsService {

    List<WorklogEntity> findByTask(JiraTaskEntity task);

    WorklogEntity create(CreateWorklogDto request);

    WorklogEntity updateSync(long id, long jiraWorklogId);

    void delete(long id);
}
