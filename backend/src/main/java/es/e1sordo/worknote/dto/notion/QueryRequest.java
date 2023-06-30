package es.e1sordo.worknote.dto.notion;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QueryRequest {

    @JsonProperty
    private Object filter;

    @JsonProperty(value = "start_cursor")
    private String startCursor;

    @JsonProperty
    private List<Map<String, String>> sorts;

    public QueryRequest() {
        this.filter = new Filter();
        this.sorts = List.of(
                Map.of(
                        "property", "Date",
                        "direction", "ascending"
                )
        );
    }

    @Data
    static class Filter {

        @JsonProperty
        private List<Object> and;

        public Filter() {
            this.and = List.of(
                    Map.of(
                            "property", "In Jira?",
                            "checkbox", Map.of("equals", true)
                    ),
                    Map.of(
                            "property", "⏰ (hrs.)",
                            "number", Map.of("is_not_empty", true)
                    )
//                    ,
//                    Map.of(
//                            "property", "Jira worklog ID (debug)",
//                            "rich_text", Map.of("is_empty", true)
//                    )
            );
        }
    }
}
