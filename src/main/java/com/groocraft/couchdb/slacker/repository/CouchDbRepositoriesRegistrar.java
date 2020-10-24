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

import com.groocraft.couchdb.slacker.annotation.EnableCouchDbRepositories;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

import java.lang.annotation.Annotation;

/**
 * Implementation of {@link RepositoryBeanDefinitionRegistrarSupport} to register {@link CouchDbRepositoryConfigurationExtension}. The implementation is
 * imported by {@link EnableCouchDbRepositories} annotation on a configuration bean.
 *
 * @author Majlanky
 * @see RepositoryBeanDefinitionRegistrarSupport
 */
public class CouchDbRepositoriesRegistrar extends RepositoryBeanDefinitionRegistrarSupport {

    /**
     * {@inheritDoc}
     */
    @Override
    protected @NotNull Class<? extends Annotation> getAnnotation() {
        return EnableCouchDbRepositories.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected @NotNull RepositoryConfigurationExtension getExtension() {
        return new CouchDbRepositoryConfigurationExtension();
    }
}
