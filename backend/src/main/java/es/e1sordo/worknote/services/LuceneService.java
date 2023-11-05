package es.e1sordo.worknote.services;

import es.e1sordo.worknote.dto.SearchTaskResult;
import es.e1sordo.worknote.models.JiraTaskEntity;
import es.e1sordo.worknote.utils.TriFunction;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.highlight.Highlighter;

import java.util.List;

public interface LuceneService {

    void addToIndex(JiraTaskEntity entity, boolean updateExisted);

    List<SearchTaskResult> search(
            String textQuery,
            TriFunction<Document, Analyzer, Highlighter, SearchTaskResult> transformer
    );
}
