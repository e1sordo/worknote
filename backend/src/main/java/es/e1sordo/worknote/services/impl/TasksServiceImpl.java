package es.e1sordo.worknote.services.impl;

import es.e1sordo.worknote.dto.TaskDto;
import es.e1sordo.worknote.dto.TaskWithUsageDto;
import es.e1sordo.worknote.dto.UpsertTaskDto;
import es.e1sordo.worknote.models.JiraProjectEntity;
import es.e1sordo.worknote.models.JiraTaskEntity;
import es.e1sordo.worknote.repositories.ProjectRepository;
import es.e1sordo.worknote.repositories.TaskRepository;
import es.e1sordo.worknote.services.TasksService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;

@Slf4j
@Service
@RequiredArgsConstructor
public class TasksServiceImpl implements TasksService {

    private final ProjectRepository projectRepository;
    private final TaskRepository repository;

    private static final Analyzer ANALYZER;
    private static final Directory DIRECTORY;

    private IndexWriter writer = null;

    static {
        ANALYZER = new PerFieldAnalyzerWrapper(new RussianAnalyzer(), Map.of(
                "jiraId", new StandardAnalyzer()
        ));
        DIRECTORY = new ByteBuffersDirectory();
    }

    @PostConstruct
    public void fillUpLucene() throws IOException {
        final List<JiraTaskEntity> data = repository.findByClosedFalse();
        IndexWriterConfig config;
        if (!data.isEmpty()) {
            ArrayList<Document> documents = new ArrayList<>();
            data.forEach(item -> documents.add(wrapDocument(item)));
            config = new IndexWriterConfig(ANALYZER);
            writer = new IndexWriter(DIRECTORY, config);
            writer.addDocuments(documents);
        }
        try {
            writer.commit();
        } catch (Exception e) {
            e.printStackTrace();
            writer.rollback();
        } finally {
            writer.close();
            writer = null;
        }
    }

    @Override
    public List<TaskDto> getAllByQuery(final String searchString) {
        log.info("Find tasks by query: {}", searchString);

        final String searchQuery = stream(searchString.split(" "))
                .map(s -> s + "*")
                .collect(joining(" "));

        String[] columns = { "jiraId", "project", "type", "title", "examples" };
        MultiFieldQueryParser queryParser = new MultiFieldQueryParser(columns, ANALYZER);
        SimpleHTMLFormatter htmlFormatter = new SimpleHTMLFormatter("<mark>", "</mark>");

        List<TaskDto> results = new ArrayList<>();

        try {
            IndexReader reader = DirectoryReader.open(DIRECTORY);
            IndexSearcher searcher = new IndexSearcher(reader);

            Query query = queryParser.parse(searchQuery);

            Highlighter highlighter = new Highlighter(htmlFormatter, new QueryScorer(query));

            TopDocs docs = searcher.search(query, 50);
            StoredFields storedFields = searcher.storedFields();
            for (ScoreDoc scoreDoc : docs.scoreDocs) {
                Document document = storedFields.document(scoreDoc.doc);

                final long documentId = Long.parseLong(document.get("id"));
                final JiraTaskEntity task = repository.findById(documentId).get();

                TokenStream titleTokenStream = TokenSources.getTokenStream("title", task.getTitle(), ANALYZER);
                String highlightedTitle = highlighter.getBestFragments(titleTokenStream, task.getTitle(), 10, "...");

                final var examplesText = ofNullable(task.getExamples()).orElse("");
                TokenStream examplesTokenStream = TokenSources.getTokenStream("examples", examplesText, ANALYZER);
                String highlightedExamples = highlighter.getBestFragments(examplesTokenStream, examplesText, 10, "...");

                final var taskDto = new TaskDto(
                        task.getId(),
                        task.getJiraId(),
                        task.getProject().getCode(),
                        task.getProject().getShortCode(),
                        task.getType(),
                        highlightedTitle.isBlank() ? task.getTitle() : highlightedTitle,
                        highlightedExamples.isBlank() ? task.getExamples() : highlightedExamples,
                        task.isClosed()
                );

                results.add(taskDto);
            }
        } catch (ParseException | InvalidTokenOffsetsException | IOException ex) {
            throw new RuntimeException(ex);
        }

        return results;
    }

    @Override
    public List<TaskWithUsageDto> getAllSortedByUsage() {
        log.info("Find all tasks sorted by usage");

        List<TaskWithUsageDto> result = new ArrayList<>();

        List<Object[]> tasksWithLatestWorklogs = repository.findTasksWithLatestWorklogs();
        for (Object[] task : tasksWithLatestWorklogs) {
            JiraTaskEntity jiraTask = (JiraTaskEntity) task[0];
            var latestDate = ofNullable((LocalDate) task[1]);
            LocalTime latestTime = ofNullable((LocalTime) task[2])
                    .orElseGet(() -> LocalTime.of(8, 0));

            result.add(mapToDtoWithUsage(
                    jiraTask,
                    latestDate.map(date -> date.atTime(latestTime)).orElse(null)));
        }

        repository.findByWorklogsEmpty().forEach(jiraTask ->
                result.add(mapToDtoWithUsage(jiraTask, null)));

        result.sort(Comparator.comparing(TaskWithUsageDto::closed));

        return result;
    }

    @Override
    public TaskDto upsert(final UpsertTaskDto request) {
        log.info("Upsert task by its Project ({}) and ID ({})", request.code(), request.id());

        final JiraProjectEntity project = projectRepository.findByCode(request.code()).orElseGet(() ->
                projectRepository.save(new JiraProjectEntity(request.code(), request.code(), request.code())));

        final Optional<JiraTaskEntity> task = repository.findByJiraIdAndProject(request.id(), project);

        final TaskDto result;
        if (task.isPresent()) {
            final var taskEntity = task.get();
            log.info("Task was found, so try to update it ({})", taskEntity.getTitle());
            taskEntity.setJiraId(request.id());
            taskEntity.setTitle(request.title());
            taskEntity.setType(request.type());
            taskEntity.setExamples(request.examples());
            taskEntity.setClosed(request.closed());
            result = mapToDto(repository.save(taskEntity));
        } else {
            log.info("Task was not found, so try to create. Title: {}", request.title());
            result = mapToDto(repository.save(createEntity(request, project)));
        }

        try {
            fillUpLucene();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return result;
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

    private TaskDto mapToDto(final JiraTaskEntity entity) {
        return new TaskDto(
                entity.getId(),
                entity.getJiraId(),
                entity.getProject().getCode(),
                entity.getProject().getShortCode(),
                entity.getType(),
                entity.getTitle(),
                entity.getExamples(),
                entity.isClosed()
        );
    }

    private TaskWithUsageDto mapToDtoWithUsage(final JiraTaskEntity entity, final LocalDateTime latestWorklogTime) {
        return new TaskWithUsageDto(
                entity.getId(),
                entity.getJiraId(),
                entity.getProject().getCode(),
                entity.getType(),
                entity.getTitle(),
                entity.getExamples(),
                entity.isClosed(),
                latestWorklogTime
        );
    }

    private Document wrapDocument(JiraTaskEntity entity) {
        final var document = new Document();
        document.add(new StoredField("id", entity.getId()));
        document.add(new StringField("jiraId", String.valueOf(entity.getJiraId()), Field.Store.YES));
        document.add(new StringField("project", entity.getProject().getCode(), Field.Store.YES));
        document.add(new StringField("type", entity.getType().name(), Field.Store.YES));
        document.add(new TextField("title", entity.getTitle(), Field.Store.YES));
        document.add(new TextField("examples", ofNullable(entity.getExamples()).orElse(""), Field.Store.YES));
        return document;
    }
}
