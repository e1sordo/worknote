package es.e1sordo.worknote.repositories;

import es.e1sordo.worknote.models.JiraProjectEntity;
import es.e1sordo.worknote.models.JiraTaskEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends ListCrudRepository<JiraTaskEntity, Long> {
    Optional<JiraTaskEntity> findByJiraIdAndProject(int jiraId, JiraProjectEntity project);

    @Query("""
            SELECT t, w.date, w.startTime
            FROM JiraTaskEntity t
            JOIN t.worklogs w
            WHERE CONCAT(w.date, ' ', w.startTime) =
                (SELECT CONCAT(wl.date, ' ', wl.startTime)
                FROM WorklogEntity wl
                WHERE wl.task = t
                ORDER BY wl.date DESC, wl.startTime DESC LIMIT 1)
            ORDER BY w.date DESC, w.startTime DESC
            """)
    List<Object[]> findTasksWithLatestWorklogs();

    List<JiraTaskEntity> findByWorklogsEmpty();

    @Query("""
            SELECT t, SUM(w.durationInMinutes) AS totalDuration, COUNT(w) AS worklogsCount
            FROM JiraTaskEntity t
            JOIN t.worklogs w
            GROUP BY t
            ORDER BY totalDuration DESC
            LIMIT 15
            """)
    List<Object[]> findTop15TasksWithTotalDuration();
}
