package com.groocraft.couchdb.slacker.structure;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Pojo class to ease reading part of {@link DocumentFindResponse}.
 *
 * @author Majlanky
 * @see DocumentFindResponse
 */
@SuppressWarnings("unused")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ExecutionStats {

    private int totalKeysExamined;
    private int totalDocsExamined;
    private int totalQuorumDocsExamined;
    private int resultsReturned;
    private int executionTimeMs;

}
