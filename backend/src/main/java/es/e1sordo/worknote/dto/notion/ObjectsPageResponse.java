package es.e1sordo.worknote.dto.notion;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ObjectsPageResponse {

    @JsonProperty
    private List<ObjectResponse> results = new ArrayList<>();

    @JsonProperty(value = "has_more")
    private boolean hasMore;

    @JsonProperty(value = "next_cursor")
    private String nextCursor;
}
