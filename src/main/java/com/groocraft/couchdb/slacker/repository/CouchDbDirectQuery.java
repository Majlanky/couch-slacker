package com.groocraft.couchdb.slacker.repository;

import com.groocraft.couchdb.slacker.CouchDbClient;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.RepositoryQuery;

/**
 * Implementation of {@link RepositoryQuery} used to process {@link com.groocraft.couchdb.slacker.annotation.Query} annotated method of
 * {@link org.springframework.data.repository.Repository} implementation.
 * <p>
 * Implementation support both ways of passing parameters (? with number of parameter and : with name of parameter). In the other case,
 * {@link org.springframework.data.repository.query.Param} can be used.
 *
 * @author Majlanky
 * @see RepositoryQuery
 */
public class CouchDbDirectQuery extends CouchDbQuery {

    private final String query;

    /**
     * @param query Json query created or read from method. Must not be {@literal null}.
     * @param client must not be {@literal null}.
     * @param queryMethod on which is based the query. Must not be {@literal null}.
     */
    public CouchDbDirectQuery(@NotNull String query, @NotNull CouchDbClient client, @NotNull QueryMethod queryMethod, @NotNull Class<?> entityClass) {
        super(client, queryMethod, entityClass);
        this.query = query;
    }

    @Override
    protected String getQuery(Object[] parameters) {
        return parameters.length > 0 ? specify(query, parameters) : query;
    }
}
