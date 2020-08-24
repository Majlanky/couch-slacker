/*
 * Copyright 2014-2020 the original author or authors.
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

import org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport;

/**
 * Implementation of {@link RepositoryConfigurationExtensionSupport} to inject named queries from {@literal couchDb-named-queries.properties} placed in
 * META-INF (happens in {@link RepositoryConfigurationExtensionSupport#getDefaultNamedQueryLocation()}) and register {@link CouchDbRepositoryFactoryBean}
 * which is providing factory of CouchDB repositories.
 *
 * @author Majlanky
 * @see RepositoryConfigurationExtensionSupport
 */
public class CouchDbRepositoryConfigurationExtension extends RepositoryConfigurationExtensionSupport {

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getModulePrefix() {
        return "couchDb";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRepositoryFactoryBeanClassName() {
        return CouchDbRepositoryFactoryBean.class.getName();
    }
}
