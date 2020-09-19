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
import com.groocraft.couchdb.slacker.configuration.CouchDbProperties;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.util.Assert;

import java.io.Serializable;

/**
 * Implementation of {@link RepositoryFactoryBeanSupport}. The implementation registers {@link CouchDbRepositoryFactory}
 *
 * @author Majlanky
 * @see RepositoryFactoryBeanSupport
 */
public class CouchDbRepositoryFactoryBean<T extends Repository<S, ID>, S, ID extends Serializable>
        extends RepositoryFactoryBeanSupport<T, S, ID> {

    private final CouchDbClient client;
    private final CouchDbProperties properties;

    /**
     * @param repositoryInterface must not be {@literal null}.
     * @param client              must not be {@literal null}.
     * @param properties          must not be {@literal null}.
     */
    @SuppressWarnings("SameParameterValue")
    protected CouchDbRepositoryFactoryBean(@NotNull Class<? extends T> repositoryInterface, @NotNull CouchDbClient client, @NotNull CouchDbProperties properties) {
        super(repositoryInterface);
        Assert.notNull(client, "Client must not be null.");
        Assert.notNull(properties, "Properties must not be null.");
        this.client = client;
        this.properties = properties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected RepositoryFactorySupport createRepositoryFactory() {
        return new CouchDbRepositoryFactory(client, properties);
    }
}
