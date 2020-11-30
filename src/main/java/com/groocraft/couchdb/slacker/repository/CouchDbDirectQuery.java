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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.groocraft.couchdb.slacker.CouchDbClient;
import com.groocraft.couchdb.slacker.exception.CouchDbRuntimeException;
import com.groocraft.couchdb.slacker.exception.QueryException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.repository.query.Parameter;
import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.Optional;

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
public class CouchDbDirectQuery implements RepositoryQuery {

    private final String query;
    private final CouchDbClient client;
    private final Class<?> entityClass;
    private final ObjectMapper mapper;
    private final QueryMethod queryMethod;

    /**
     * @param query       Json query created or read from method. Must not be {@literal null}.
     * @param client      must not be {@literal null}.
     * @param queryMethod on which is based the query. Must not be {@literal null}.
     * @param entityClass class of processed entities
     */
    public CouchDbDirectQuery(@NotNull String query, @NotNull CouchDbClient client, @NotNull QueryMethod queryMethod, @NotNull Class<?> entityClass) {
        Assert.hasText(query, "Query must not be null nor empty.");
        Assert.notNull(client, "Client must not be null.");
        Assert.notNull(queryMethod, "QueryMethod must not be null");
        Assert.notNull(entityClass, "EntityClass must not be null.");
        this.client = client;
        this.queryMethod = queryMethod;
        this.entityClass = entityClass;
        this.mapper = new ObjectMapper();
        this.query = query;
    }

    /**
     * Using parsed parameters from {@link QueryMethod} to find which name is which parameter and replacing all parameters found.
     *
     * @param query      must not be {@literal null}.
     * @param parameters must not be {@literal null}.
     * @return Specified query, it means all tokens are replaced by actual values of passed parameters
     */
    protected @NotNull String specify(@NotNull String query, @NotNull Object[] parameters) {
        String specified = query;
        Parameters<?, ?> methodParameters = getQueryMethod().getParameters();
        for (Parameter parameter : methodParameters) {
            if (!parameter.isDynamicProjectionParameter() && !parameter.isSpecialParameter()) {
                try {
                    specified = specified.replace("?" + (parameter.getIndex() + 1), mapper.writeValueAsString(parameters[parameter.getIndex()]));
                    Optional<String> parameterName = parameter.getName();
                    if (parameterName.isPresent()) {
                        specified = specified.replace(":" + parameterName.get(), mapper.writeValueAsString(parameters[parameter.getIndex()]));
                    }
                } catch (JsonProcessingException ex) {
                    throw new QueryException(String.format("Unable to create json representation %s. parameter", parameter.getIndex()), ex);
                }
            }
        }
        return specified;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @Nullable Object execute(@NotNull Object[] parameters) {
        String currentQuery = parameters.length > 0 ? specify(query, parameters) : query;
        try {
            return client.find(currentQuery, entityClass).getFirst();
        } catch (IOException e) {
            throw new CouchDbRuntimeException("Unable to query " + query, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull QueryMethod getQueryMethod() {
        return queryMethod;
    }

}
