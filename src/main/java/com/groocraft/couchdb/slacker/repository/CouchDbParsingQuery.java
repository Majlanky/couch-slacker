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
import com.groocraft.couchdb.slacker.annotation.Index;
import com.groocraft.couchdb.slacker.exception.CouchDbRuntimeException;
import com.groocraft.couchdb.slacker.exception.QueryException;
import com.groocraft.couchdb.slacker.structure.DocumentFindRequest;
import com.groocraft.couchdb.slacker.structure.FindResult;
import com.groocraft.couchdb.slacker.utils.FindContext;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Parameter;
import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.parser.PartTree;
import org.springframework.util.Assert;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * Implementation of {@link RepositoryQuery} used to process query methods of {@link org.springframework.data.repository.Repository} implementation.
 * <p>
 * Implementation support named parameters with {@link org.springframework.data.repository.query.Param} or name of a parameter is used to specify parsed query.
 * <p>
 * Implementation supports all find queries.
 *
 * @param <EntityT> type of entity returned by query
 * @author Majlanky
 * @see RepositoryQuery
 */
public class CouchDbParsingQuery<EntityT> implements RepositoryQuery {

    private final QueryMethod queryMethod;
    private final CouchDbClient client;
    private final Class<EntityT> entityClass;
    private final BiFunction<FindResult<EntityT>, Object[], Object> postProcessor;
    private final PartTree partTree;
    private final Index index;
    private final boolean returnExecutionStats;

    /**
     * @param client               must not be {@literal null}.
     * @param returnExecutionStats flag which can turn on/off execution stats in result of every query.
     * @param method               must not be {@literal null}.
     * @param queryMethod          on which is based the query. Must not be {@literal null}.
     * @param entityClass          repository domain class. Must not be {@literal null}.
     */
    public CouchDbParsingQuery(@NotNull CouchDbClient client, boolean returnExecutionStats, @NotNull Method method,
                               @NotNull QueryMethod queryMethod,
                               @NotNull Class<EntityT> entityClass) {
        Assert.notNull(client, "Client must not be null.");
        Assert.notNull(method, "Method must not be null.");
        Assert.notNull(queryMethod, "QueryMethod must not be null.");
        Assert.notNull(entityClass, "EntityClass must not be null.");
        this.client = client;
        this.returnExecutionStats = returnExecutionStats;
        this.queryMethod = queryMethod;
        this.entityClass = entityClass;
        partTree = new PartTree(queryMethod.getName(), queryMethod.getResultProcessor().getReturnedType().getDomainType());
        index = method.getAnnotation(Index.class);
        this.postProcessor = getPostProcessor(partTree, queryMethod, entityClass);
    }

    /**
     * Method to obtain sorting information from parameters if present. If there is no parameter with sorting information
     * {@link Sort#unsorted()} is used.
     *
     * @param parameters Actual parameter of a call. Must not be {@literal null}
     * @return {@link Sort} from parameters if present or {@link Sort#unsorted()}. Can not be {@literal null}
     */
    private @NotNull Sort getSortFrom(@NotNull Object[] parameters) {
        int sortIndex = getQueryMethod().getParameters().getSortIndex();
        if (sortIndex != -1) {
            return (Sort) parameters[sortIndex];
        }
        return Sort.unsorted();
    }

    /**
     * Method to obtain pagination information from parameters if present. If there is no parameter with pagination information
     * {@link Pageable#unpaged()} is used.
     *
     * @param parameters Actual parameter of a call. Must not be {@literal null}
     * @return {@link Pageable} from parameters if present or {@link Pageable#unpaged()}. Can not be {@literal null}
     */
    private @NotNull Pageable getPageableFrom(@NotNull Object[] parameters) {
        int pageableIndex = getQueryMethod().getParameters().getPageableIndex();
        if (pageableIndex != -1) {
            return (Pageable) parameters[pageableIndex];
        }
        return Pageable.unpaged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(Object[] parameters) {
        Pageable pageable = getPageableFrom(parameters);
        Sort sortParameter = getSortFrom(parameters);
        List<Sort.Order> sort = null;
        try {
            Long skip = pageable.isPaged() ? pageable.getOffset() : null;
            //if there is hard max result in query method, than the max, if not it depends if slice is returned. If so, we need only find out if there is next
            // slice. In case of page, we need get everything to count total pages.
            Integer limit = partTree.getMaxResults() != null ? partTree.getMaxResults() : queryMethod.isSliceQuery() ? pageable.getPageSize() + 1 :
                    null;
            DocumentFindRequest request = new DocumentFindRequest(new FindContext(partTree, initializeParameters(parameters),
                    client.getEntityMetadata(entityClass)), skip, limit, index != null ? index.value() : null,
                    sortParameter.and(partTree.getSort()).and(pageable.getSort()), returnExecutionStats);

            return postProcessor.apply(
                    client.find(request, entityClass),
                    parameters);
        } catch (IOException e) {
            throw new CouchDbRuntimeException("Unable to run parsing query", e);
        }
    }

    /**
     * Method to create map of named parameters with actual value of a call. Actual implementation works only with named parameters, so
     * {@link QueryException} can be thrown if one or more parameters are not named.
     *
     * @param parameters Actual parameter of a call. Must not be {@literal null}
     * @return {@link Map} of named parameters of a call. Can not be {@literal null}
     */
    private @NotNull Map<String, Object> initializeParameters(@NotNull Object[] parameters) {
        Map<String, Object> initialized = new HashMap<>();
        Parameters<?, ?> methodParameters = getQueryMethod().getParameters();
        for (Parameter parameter : methodParameters) {
            if (!parameter.isDynamicProjectionParameter() && !parameter.isSpecialParameter()) {
                initialized.put(parameter.getName().orElseThrow(() -> new QueryException("Dynamic query can work only with named parameters")),
                        parameters[parameter.getIndex()]);
            }
        }
        return initialized;
    }

    /**
     * Method to create post processor for find result. Spring data provides delete, count, exists and distinct operation above result of find query.
     *
     * @param partTree    {@link PartTree} created from generic query method.  Must not be {@literal null}
     * @param queryMethod Must not be {@literal null}
     * @param entityClass Must not be {@literal null}
     * @return {@link Function} processing result of find query in the requested way. Can not be {@literal null}
     */
    private @NotNull BiFunction<FindResult<EntityT>, Object[], Object> getPostProcessor(@NotNull PartTree partTree,
                                                                                        @NotNull QueryMethod queryMethod,
                                                                                        @NotNull Class<EntityT> entityClass) {
        if (partTree.isDelete()) {
            return (i, p) -> delete(i.getEntities(), entityClass);
        }
        if (partTree.isCountProjection()) {
            return (i, p) -> i.getEntities().size();
        }
        if (partTree.isDistinct()) {
            return (i, p) -> {
                throw new QueryException("Distinct is not implemented yet");
            };
        }
        if (partTree.isExistsProjection()) {
            return (i, p) -> i.getEntities().size() > 0;
        }
        if (queryMethod.isPageQuery()) {
            return this::wrapAsPage;
        }
        if (queryMethod.isSliceQuery()) {
            return this::wrapAsSlice;
        }
        return (i, p) -> i.getEntities();
    }

    /**
     * Wrapper for calling delete. Method deletes all given entities from DB.
     *
     * @param entities which should be erased.  Must not be {@literal null}
     * @param clazz    of entities.  Must not be {@literal null}
     * @return {@link Iterable} of deleted entities. Can not be {@literal null}
     */
    private @NotNull Iterable<EntityT> delete(@NotNull List<EntityT> entities, @NotNull Class<EntityT> clazz) {
        try {
            return client.deleteAll(entities, clazz);
        } catch (IOException e) {
            throw new CouchDbRuntimeException("Unable to query post process (delete) query ", e);
        }
    }

    private @NotNull Page<EntityT> wrapAsPage(@NotNull FindResult<EntityT> findResult, Object[] parameters) {
        Pageable pageable = getPageableFrom(parameters);
        List<EntityT> paged = new LinkedList<>();
        IntStream.range(0, pageable.getPageSize()).forEach(i -> paged.add(findResult.getEntities().get(i)));
        return new PageImpl<>(paged, pageable, pageable.getOffset() + findResult.getEntities().size());
    }

    private @NotNull Slice<EntityT> wrapAsSlice(@NotNull FindResult<EntityT> findResult, Object[] parameters) {
        Pageable pageable = getPageableFrom(parameters);
        boolean hasNext = findResult.getEntities().size() > pageable.getPageSize();
        if (hasNext) {
            findResult.getEntities().remove(findResult.getEntities().size() - 1);
        }
        return new SliceImpl<>(findResult.getEntities(), pageable, hasNext);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueryMethod getQueryMethod() {
        return queryMethod;
    }

}
