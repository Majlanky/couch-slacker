package com.groocraft.couchdb.slacker.structure;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Pojo class to ease reading responses to database _find request.
 *
 * @param <EntityT> Type of result entity
 * @author Majlanky
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DocumentFindResponse<EntityT> {

    @JsonProperty("docs")
    private List<EntityT> documents;

    @JsonProperty("execution_stats")
    private ExecutionStats executionStats;

    @JsonProperty("error")
    private String error;

    @JsonProperty("bookmark")
    private String bookmark;

    @JsonProperty("warning")
    private String warning;

    @JsonProperty("reason")
    private String reason;

}
