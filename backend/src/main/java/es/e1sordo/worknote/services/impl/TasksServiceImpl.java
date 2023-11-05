package es.e1sordo.worknote.services.impl;

import es.e1sordo.worknote.dto.SearchTaskResult;
import es.e1sordo.worknote.dto.UpsertTaskDto;
import es.e1sordo.worknote.models.JiraProjectEntity;
import es.e1sordo.worknote.models.JiraTaskEntity;
import es.e1sordo.worknote.repositories.TaskRepository;
import es.e1sordo.worknote.services.JiraProjectsService;
import es.e1sordo.worknote.services.LuceneService;
import es.e1sordo.worknote.services.TasksService;
import es.e1sordo.worknote.services.WorklogsService;
import es.e1sordo.worknote.utils.Pair;
import es.e1sordo.worknote.utils.SanitizingUtil;
import es.e1sordo.worknote.utils.TriFunction;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.TokenSources;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static es.e1sordo.worknote.utils.KeyboardSwitchUtil.enToRu;
import static es.e1sordo.worknote.utils.KeyboardSwitchUtil.ruToEn;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;

@Slf4j
@Service
@RequiredArgsConstructor
public class TasksServiceImpl implements TasksService {

    private final @Lazy LuceneService luceneService;
    private final JiraProjectsService projectsService;
    private final @Lazy WorklogsService worklogsService;
    private final TaskRepository repository;

    @PostConstruct
    public void postConstruct() {
        removeDuplicates();
    }

    private void removeDuplicates() {
        final List<JiraTaskEntity> allTasks = repository.findAll();
        var duplicateIds = allTasks.stream()
                .collect(groupingBy(entity -> entity.getProject().getCode() + "-" + entity.getJiraId(), counting()))
                .entrySet()
                .stream()
                .filter(m -> m.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(toSet());

        if (duplicateIds.isEmpty()) {
            log.info("No duplicated tasks were found");
            return;
        }

        log.info("Found {} task duplicates: {}", duplicateIds.size(), duplicateIds);

        var duplicates = allTasks.stream()
                .filter(entity -> duplicateIds.contains(entity.getProject().getCode() + "-" + entity.getJiraId()))
                .toList();

        for (JiraTaskEntity duplicate : duplicates) {
            if (worklogsService.findByTask(duplicate).isEmpty()) {
                log.info("Duplicate {} were deleted", duplicate);
                repository.delete(duplicate);
            }
        }
    }

    @Override
    public List<SearchTaskResult> getAllByQuery(final String searchString) {
        final String sanitizedSearchString = SanitizingUtil.sanitize(searchString);
        log.info("Find tasks by query: {}", sanitizedSearchString);

        final String fullSearchString = ruToEn(sanitizedSearchString) + " " + enToRu(sanitizedSearchString);
        log.debug("Full string to search tasks by: {}", fullSearchString);

        final String searchQuery = stream(fullSearchString.trim().split(" "))
                .map(s -> s + " " + s + "*") // tasks -> "tasks tasks*"
                .collect(joining(" "));

        if (searchQuery.startsWith("*")) {
            return emptyList();
        }

        final TriFunction<Document, Analyzer, Highlighter, SearchTaskResult> function = ((document, analyzer, highlighter) -> {
            final long documentId = Long.parseLong(document.get("id"));
            final JiraTaskEntity task = repository.findById(documentId).orElseThrow();

            String highlightedTitle = extractHighlights("title", task::getTitle, analyzer, highlighter);
            String highlightedExamples = extractHighlights("examples", task::getExamples, analyzer, highlighter);

            return new SearchTaskResult(task, highlightedTitle, highlightedExamples);
        });

        return luceneService.search(searchQuery, function);
    }

    @Override
    public List<JiraTaskEntity> getAllOpenTasks() {
        log.info("Find all open tasks");
        return repository.findByClosedFalse();
    }

    @Override
    public List<Pair<JiraTaskEntity, LocalDateTime>> getAllSortedByUsage() {
        log.info("Find all tasks sorted by usage");

        List<Pair<JiraTaskEntity, LocalDateTime>> result = new ArrayList<>();

        List<Object[]> tasksWithLatestWorklogs = repository.findTasksWithLatestWorklogs();
        for (Object[] task : tasksWithLatestWorklogs) {
            final var jiraTask = (JiraTaskEntity) task[0];
            final var latestDate = ofNullable((LocalDate) task[1]);
            final var latestTime = ofNullable((LocalTime) task[2])
                    .orElseGet(() -> LocalTime.of(8, 0));

            result.add(Pair.of(
                    jiraTask,
                    latestDate.map(date -> date.atTime(latestTime)).orElse(null)
            ));
        }

        repository.findByWorklogsEmpty().forEach(jiraTask ->
                result.add(Pair.of(jiraTask, null)));

        result.sort(Comparator.comparing(pair -> pair.first().isClosed()));

        return result;
    }

    @Override
    public JiraTaskEntity upsert(final UpsertTaskDto request) {
        log.info("Upsert task by its Project ({}) and ID ({})", request.code(), request.id());

        final JiraProjectEntity project = projectsService.findByCode(request.code())
                .orElseGet(() -> projectsService.create(request.code()));

        final Optional<JiraTaskEntity> task = repository.findByJiraIdAndProject(request.id(), project);

        final JiraTaskEntity result;
        if (task.isPresent()) {
            final var taskEntity = task.get();
            log.info("Task was found, so try to update it ({})", taskEntity.getTitle());
            taskEntity.setJiraId(request.id());
            taskEntity.setTitle(request.title());
            taskEntity.setType(request.type());
            taskEntity.setExamples(request.examples());
            taskEntity.setClosed(request.closed());
            luceneService.addToIndex(taskEntity, true);
            result = repository.save(taskEntity);
        } else {
            log.info("Task was not found, so try to create. Title: {}", request.title());
            final JiraTaskEntity taskEntity = repository.save(createEntity(request, project));
            luceneService.addToIndex(taskEntity, false);
            result = taskEntity;
        }

        return result;
    }

    @SneakyThrows
    private String extractHighlights(String field, Supplier<String> getter, Analyzer analyzer, Highlighter highlighter) {
        final String originalValue = ofNullable(getter.get()).orElse("");
        final TokenStream tokenStream = TokenSources.getTokenStream(field, originalValue, analyzer);
        return highlighter.getBestFragments(tokenStream, originalValue, 10, "...");
    }

    private JiraTaskEntity createEntity(final UpsertTaskDto request, final JiraProjectEntity project) {
        return new JiraTaskEntity(
                null,
                request.id(),
                project,
                request.type(),
                false,
                request.title(),
                request.examples(),
                null
        );
    }
}
