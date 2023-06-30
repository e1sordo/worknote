package es.e1sordo.worknote.services;

import es.e1sordo.worknote.dto.CreateWorklogDto;
import es.e1sordo.worknote.dto.WorklogDto;

public interface WorklogsService {
    WorklogDto create(CreateWorklogDto request);
    WorklogDto updateSync(long id, long jiraWorklogId);
    void delete(long id);
}
