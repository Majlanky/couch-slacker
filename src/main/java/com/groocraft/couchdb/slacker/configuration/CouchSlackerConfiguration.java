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

package com.groocraft.couchdb.slacker.configuration;

import com.groocraft.couchdb.slacker.CouchDbClient;
import com.groocraft.couchdb.slacker.IdGenerator;
import com.groocraft.couchdb.slacker.annotation.Document;
import com.groocraft.couchdb.slacker.exception.SchemaProcessingException;
import com.groocraft.couchdb.slacker.repository.CouchDBSchemaProcessor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.domain.EntityScanPackages;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Basic class which can be used as extension of {@link org.springframework.context.annotation.Configuration} class and enable reading Couch Slacker
 * configuration from yaml or properties configuration files.
 *
 * @author Majlanky
 */
@EnableConfigurationProperties(CouchDbProperties.class)
public class CouchSlackerConfiguration {

    /**
     * @param properties   of Couch Slacker. Must not be {@literal null}
     * @param idGenerators all configured beans of {@link IdGenerator} class. Can be {@literal null}
     * @return {@link CouchDbClient} with the given properties
     */
    @Bean(destroyMethod = "close")
    public CouchDbClient dbClient(@NotNull CouchDbProperties properties,
                                  @Autowired(required = false) List<IdGenerator<?>> idGenerators) {
        Assert.notNull(properties, "Properties must not be null.");
        return CouchDbClient.builder().properties(properties).idGenerators(idGenerators).build();
    }

    /**
     * This bean is not actually needed for anything. The main purpose is to run schema processing.
     *
     * @param properties         of Couch Slacker. Must not be {@literal null}
     * @param client             must not be {@literal null}
     * @param entityScanPackages can be {@literal null} if not {@link org.springframework.boot.autoconfigure.domain.EntityScan} used
     * @return {@link CouchDBSchemaProcessor} as mock bean
     * @throws ClassNotFoundException    if scanning returns name of class which is not loadable
     * @throws IOException               if some database operation fails during schema processing
     * @throws SchemaProcessingException if schema processing fails on a rule
     * @see CouchDBSchemaProcessor
     */
    @Bean
    public CouchDBSchemaProcessor schemaProcessor(
            @NotNull CouchDbProperties properties,
            @NotNull CouchDbClient client,
            @Autowired(required = false) EntityScanPackages entityScanPackages) throws ClassNotFoundException, IOException, SchemaProcessingException {
        Assert.notNull(properties, "Properties must not be null.");
        Assert.notNull(client, "Client must not be null.");
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AnnotationTypeFilter(Document.class));
        List<Class<?>> entityClasses = new LinkedList<>();
        for (String pack : entityScanPackages == null ? List.of(getClass().getPackage().getName()) : entityScanPackages.getPackageNames()) {
            for (BeanDefinition definition : provider.findCandidateComponents(pack)) {
                entityClasses.add(Class.forName(definition.getBeanClassName()));
            }
        }
        CouchDBSchemaProcessor processor = new CouchDBSchemaProcessor(client, properties.getSchemaOperation());
        processor.process(entityClasses);
        return processor;
    }

}
