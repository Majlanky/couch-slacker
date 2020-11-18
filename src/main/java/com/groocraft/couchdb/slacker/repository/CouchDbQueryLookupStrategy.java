/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.groocraft.couchdb.slacker.repository;

import com.groocraft.couchdb.slacker.CouchDbClient;
import com.groocraft.couchdb.slacker.annotation.Query;
import com.groocraft.couchdb.slacker.configuration.CouchDbProperties;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.util.Assert;

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
    private final CouchDbProperties properties;

    /**
     * @param client     must not be {@literal null}.
     * @param properties must not be {@literal null}.
     */
    public CouchDbQueryLookupStrategy(@NotNull CouchDbClient client, @NotNull CouchDbProperties properties) {
        Assert.notNull(client, "Client must not be null.");
        Assert.notNull(properties, "Properties must not be null");
        this.client = client;
        this.properties = properties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull RepositoryQuery resolveQuery(@NotNull Method method, @NotNull RepositoryMetadata metadata, @NotNull ProjectionFactory factory,
                                                 @NotNull NamedQueries namedQueries) {
        String namedQueryName = String.format("%s.%s", metadata.getDomainType().getSimpleName(), method.getName());
        QueryMethod queryMethod = new QueryMethod(method, metadata, factory);
        Optional<String> query;
        if (namedQueries.hasQuery(namedQueryName)) {
            query = Optional.of(namedQueries.getQuery(namedQueryName));
        } else {
            Optional<Query> queryAnnotation = Optional.ofNullable(method.getAnnotation(Query.class));
            query = queryAnnotation.map(Query::value);
        }
        return query.map(s -> (RepositoryQuery) new CouchDbDirectQuery(s, client, queryMethod, metadata.getDomainType()))
                .orElseGet(() -> new CouchDbParsingQuery<>(client, properties.isFindExecutionStats(), method, queryMethod,
                        metadata.getDomainType()));
    }
}
