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

import com.groocraft.couchdb.slacker.CouchDbClient;
import com.groocraft.couchdb.slacker.configuration.CouchDbProperties;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.Optional;

/**
 * Implementation of factory for CouchDB repositories. CouchDB repositories are based on {@link SimpleCouchDbRepository}. Factory provides
 * {@link QueryLookupStrategy} to enable (generic) query methods in CouchDB repositories (managed by {@link CouchDbQueryLookupStrategy}).
 *
 * @author Majlanky
 * @see RepositoryFactorySupport
 * @see SimpleCouchDbRepository
 * @see CouchDbQueryLookupStrategy
 */
public class CouchDbRepositoryFactory extends RepositoryFactorySupport {

    private final CouchDbClient client;
    private final CouchDbProperties properties;

    /**
     * @param client     must not be {@literal null}
     * @param properties must not be {@literal null}
     */
    public CouchDbRepositoryFactory(@NotNull CouchDbClient client, @NotNull CouchDbProperties properties) {
        Assert.notNull(client, "Client must not be null.");
        Assert.notNull(properties, "Properties must not be null.");
        this.client = client;
        this.properties = properties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T, IdT> EntityInformation<T, IdT> getEntityInformation(Class<T> domainClass) {
        return client.getEntityInformation(domainClass);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object getTargetRepository(RepositoryInformation metadata) {
        return getTargetRepositoryViaReflection(metadata, client, metadata.getDomainType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected @NotNull Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
        return SimpleCouchDbRepository.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Optional<QueryLookupStrategy> getQueryLookupStrategy(@Nullable QueryLookupStrategy.Key key,
                                                                   QueryMethodEvaluationContextProvider evaluationContextProvider) {
        return Optional.of(new CouchDbQueryLookupStrategy(client, properties));
    }
}
