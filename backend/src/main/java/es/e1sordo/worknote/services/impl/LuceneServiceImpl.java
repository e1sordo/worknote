package es.e1sordo.worknote.services.impl;

import es.e1sordo.worknote.dto.SearchTaskResult;
import es.e1sordo.worknote.models.JiraTaskEntity;
import es.e1sordo.worknote.services.LuceneService;
import es.e1sordo.worknote.services.TasksService;
import es.e1sordo.worknote.utils.TriFunction;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
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
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;

@Slf4j
@Service
@RequiredArgsConstructor
public class LuceneServiceImpl implements LuceneService {

    private final Environment environment;
    private final TasksService tasksService;

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
    public void postConstruct() throws IOException {
        if (environment.matchesProfiles("test")) {
            return;
        }
        fillUpLucene();
    }

    private void fillUpLucene() throws IOException {
        final List<JiraTaskEntity> data = tasksService.getAllOpenTasks();
        if (!data.isEmpty()) {
            final List<Document> documents = new ArrayList<>();
            data.forEach(item -> documents.add(wrapDocument(item)));
            final var config = new IndexWriterConfig(ANALYZER);
            writer = new IndexWriter(DIRECTORY, config);
            writer.addDocuments(documents);
        }
        commitAndCloseWriter();
    }

    @Override
    public void addToIndex(JiraTaskEntity entity, boolean updateExisted) {
        try {
            IndexWriterConfig config = new IndexWriterConfig();
            writer = new IndexWriter(DIRECTORY, config);

            if (updateExisted) {
                Term term = new Term("jiraId", String.valueOf(entity.getJiraId()));
                writer.deleteDocuments(term);
            }

            if (!entity.isClosed()) {
                writer.addDocument(wrapDocument(entity));
            }

            commitAndCloseWriter();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<SearchTaskResult> search(
            final String textQuery,
            final TriFunction<Document, Analyzer, Highlighter, SearchTaskResult> transformer
    ) {
        final String[] columns = { "jiraId", "project", "type", "title", "examples" };
        final var queryParser = new MultiFieldQueryParser(columns, ANALYZER);
        final var htmlFormatter = new SimpleHTMLFormatter("<mark>", "</mark>");

        try {
            final IndexReader reader = DirectoryReader.open(DIRECTORY);
            final IndexSearcher searcher = new IndexSearcher(reader);
            final Query query = queryParser.parse(textQuery);
            final var highlighter = new Highlighter(htmlFormatter, new QueryScorer(query));

            final TopDocs docs = searcher.search(query, 50);
            final StoredFields storedFields = searcher.storedFields();

            final List<SearchTaskResult> results = new ArrayList<>();
            for (ScoreDoc scoreDoc : docs.scoreDocs) {
                final Document document = storedFields.document(scoreDoc.doc);
                results.add(transformer.apply(document, ANALYZER, highlighter));
            }
            return results;
        } catch (ParseException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private Document wrapDocument(JiraTaskEntity entity) {
        final var document = new Document();
        List.of(
                new StoredField("id", entity.getId()),
                new StringField("jiraId", String.valueOf(entity.getJiraId()), Field.Store.YES),
                new StringField("project", entity.getProject().getCode(), Field.Store.YES),
                new StringField("type", entity.getType().name(), Field.Store.YES),
                new TextField("title", entity.getTitle(), Field.Store.YES),
                new TextField("examples", ofNullable(entity.getExamples()).orElse(""), Field.Store.YES)
        ).forEach(document::add);

        return document;
    }

    private void commitAndCloseWriter() throws IOException {
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
}
