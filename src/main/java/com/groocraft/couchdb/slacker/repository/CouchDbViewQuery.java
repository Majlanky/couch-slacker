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
import com.groocraft.couchdb.slacker.annotation.ViewQuery;
import com.groocraft.couchdb.slacker.exception.CouchDbRuntimeException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.RepositoryQuery;

import java.io.IOException;
import java.util.Map;

/**
 * Implementation of {@link RepositoryQuery} used to process query methods of {@link org.springframework.data.repository.Repository} implementation annotated
 * by {@link com.groocraft.couchdb.slacker.annotation.ViewQuery}. Obtaining reduce result of a view is supported {@link ViewQuery#reducing()}
 *
 * @param <EntityT> type of entity returned by query if not reducing
 * @author Majlanky
 * @see RepositoryQuery
 */
public class CouchDbViewQuery<EntityT> extends PageableAndSortableQuery {

    private final CouchDbClient client;
    private final Class<EntityT> entityClass;
    private final ViewQuery viewQuery;

    /**
     * @param client      must not be {@literal null}.
     * @param viewQuery   annotation obtained from the involved query method. Must not be {@literal null}.
     * @param queryMethod on which is based the query. Must not be {@literal null}.
     * @param entityClass repository domain class. Must not be {@literal null}.
     */
    public CouchDbViewQuery(@NotNull CouchDbClient client, @NotNull ViewQuery viewQuery,
                            @NotNull QueryMethod queryMethod,
                            @NotNull Class<EntityT> entityClass) {
        super(queryMethod);
        this.client = client;
        this.entityClass = entityClass;
        this.viewQuery = viewQuery;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected @Nullable Object execute(@NotNull Object[] allParameters, @NotNull Map<String, Object> parameters, @NotNull Pageable pageable, @NotNull Sort sort) {
        try {
            if (viewQuery.reducing()) {
                return client.reduce(client.getEntityMetadata(entityClass).getDatabaseName(), viewQuery.design(), viewQuery.view(),
                        getQueryMethod().getReturnedObjectType());
            } else {
                Long skip = pageable.isPaged() ? pageable.getOffset() : null;
                Integer limit = getQueryMethod().isSliceQuery() ? pageable.getPageSize() + 1 : null;
                return client.readAll(client.readFromView(client.getEntityMetadata(entityClass).getDatabaseName(), viewQuery.design(), viewQuery.view(), skip,
                        limit, sort.and(pageable.getSort())), entityClass);
            }
        } catch (IOException e) {
            throw new CouchDbRuntimeException("Unable to run view query", e);
        }
    }
}
