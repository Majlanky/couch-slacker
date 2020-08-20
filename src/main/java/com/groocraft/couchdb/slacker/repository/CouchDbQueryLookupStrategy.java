package com.groocraft.couchdb.slacker.repository;

import com.groocraft.couchdb.slacker.CouchDbClient;
import com.groocraft.couchdb.slacker.annotation.Query;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.RepositoryQuery;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Implementation of {@link QueryLookupStrategy} to process (generic) method in standard Spring Data way.
 *
 * @author Majlanky
 * @see QueryLookupStrategy
 */
public class CouchDbQueryLookupStrategy implements QueryLookupStrategy {

    private final CouchDbClient client;

    /**
     * @param client must not be {@literal null}.
     */
    public CouchDbQueryLookupStrategy(CouchDbClient client) {
        this.client = client;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata, ProjectionFactory factory, NamedQueries namedQueries) {
        String namedQueryName = String.format("%s.%s", metadata.getDomainType().getSimpleName(), method.getName());
        QueryMethod queryMethod = new QueryMethod(method, metadata, factory);
        Optional<String> query;
        if (namedQueries.hasQuery(namedQueryName)) {
            query = Optional.of(namedQueries.getQuery(namedQueryName));
        } else {
            Optional<Query> queryAnnotation = Optional.ofNullable(method.getAnnotation(Query.class));
            query = queryAnnotation.map(Query::value);
        }
        return query.map(s -> (RepositoryQuery) new CouchDbDirectQuery(s, client, queryMethod, metadata.getDomainType())).orElseGet(() -> new CouchDbParsingQuery(client,
                method, queryMethod, metadata.getDomainType()));
    }
}
