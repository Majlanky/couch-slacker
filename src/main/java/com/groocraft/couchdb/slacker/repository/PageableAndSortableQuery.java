/*
 * Copyright 2020-2022 the original author or authors.
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

import com.groocraft.couchdb.slacker.exception.QueryException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Parameter;
import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * Ancestor of all {@link RepositoryQuery} implementations which supports {@link Pageable} and {@link Sort} parameters.
 *
 * @author Majlanky
 */
public abstract class PageableAndSortableQuery implements RepositoryQuery {

    private final QueryMethod queryMethod;

    /**
     * @param queryMethod must not be {@literal null}
     */
    protected PageableAndSortableQuery(@NotNull QueryMethod queryMethod) {
        Assert.notNull(queryMethod, "QueryMethod must not be null");
        this.queryMethod = queryMethod;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(Object[] parameters) {
        return execute(parameters, initializeParameters(parameters), getPageableFrom(parameters), getSortFrom(parameters));
    }

    /**
     * Main method of the class executed when overlaying {@link org.springframework.data.repository.Repository} method is called.
     *
     * @param allParameters originally passed to the overlaying method. Must not be {@literal null}
     * @param parameters    indexed parameters without the {@link Pageable} and {@link Sort}. Must not be {@literal null}
     * @param pageable      must not be {@literal null}
     * @param sort          must not be {@literal null}
     * @return Result of the view call mapped to an instance of the relevant class
     */
    protected abstract @Nullable Object execute(@NotNull Object[] allParameters,
                                                @NotNull Map<String, Object> parameters,
                                                @NotNull Pageable pageable,
                                                @NotNull Sort sort);

    /**
     * Method to obtain sorting information from parameters if present. If there is no parameter with sorting information
     * {@link Sort#unsorted()} is used.
     *
     * @param parameters Actual parameter of a call. Must not be {@literal null}
     * @return {@link Sort} from parameters if present or {@link Sort#unsorted()}. Can not be {@literal null}
     */
    protected @NotNull Sort getSortFrom(@NotNull Object[] parameters) {
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
    protected @NotNull Pageable getPageableFrom(@NotNull Object[] parameters) {
        int pageableIndex = getQueryMethod().getParameters().getPageableIndex();
        if (pageableIndex != -1) {
            return (Pageable) parameters[pageableIndex];
        }
        return Pageable.unpaged();
    }

    /**
     * Method to create map of named parameters with actual value of a call. Actual implementation works only with named parameters, so
     * {@link QueryException} can be thrown if one or more parameters are not named.
     *
     * @param parameters Actual parameter of a call. Must not be {@literal null}
     * @return {@link Map} of named parameters of a call. Can not be {@literal null}
     */
    protected @NotNull Map<String, Object> initializeParameters(@NotNull Object[] parameters) {
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
     * {@inheritDoc}
     */
    @Override
    public QueryMethod getQueryMethod() {
        return queryMethod;
    }
}
