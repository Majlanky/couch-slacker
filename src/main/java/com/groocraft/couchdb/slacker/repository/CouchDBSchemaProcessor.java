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
import com.groocraft.couchdb.slacker.SchemaOperation;
import com.groocraft.couchdb.slacker.annotation.Document;
import com.groocraft.couchdb.slacker.configuration.CouchDbProperties;
import com.groocraft.couchdb.slacker.exception.SchemaProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.domain.EntityScanPackages;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class which providing schema processing, depending on the given {@link SchemaOperation}. In case of {@literal validate}, only check if all databases for all
 * known documents exists, in case of {@literal drop} all existing databases are deleted and created again, in case of create only missing databases are
 * created, the already existing are left untouched. If the operation is {@literal none}, no operation is done.
 *
 * @author Majlanky
 * @see com.groocraft.couchdb.slacker.configuration.CouchDbProperties
 */
@Slf4j
public class CouchDBSchemaProcessor {

    /**
     * This bean is not actually needed for anything. The main purpose is to run schema processing.
     *
     * @param properties         of Couch Slacker. Must not be {@literal null}
     * @param client             must not be {@literal null}
     * @param context            must not be {@literal null}
     * @param entityScanPackages can be {@literal null} if not {@link org.springframework.boot.autoconfigure.domain.EntityScan} used
     * @throws Exception when schema processing going wrong
     */
    public CouchDBSchemaProcessor(@NotNull CouchDbClient client,
                                  @NotNull CouchDbProperties properties,
                                  @NotNull ApplicationContext context,
                                  @Nullable @Autowired(required = false) EntityScanPackages entityScanPackages) throws Exception {
        Assert.notNull(client, "Client must not be null.");
        Assert.notNull(properties, "SchemaOperation must not be null");
        Assert.notNull(context, "Application context must not be null");
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AnnotationTypeFilter(Document.class));
        List<String> configurationPackages =
                context.getBeansWithAnnotation(Configuration.class).values().stream().map(o -> o.getClass().getPackage().getName()).collect(Collectors.toList());
        List<Class<?>> entityClasses = new LinkedList<>();
        for (String pack : entityScanPackages == null ? configurationPackages : entityScanPackages.getPackageNames()) {
            for (BeanDefinition definition : provider.findCandidateComponents(pack)) {
                entityClasses.add(Class.forName(definition.getBeanClassName()));
            }
        }
        process(entityClasses, properties.getSchemaOperation(), client);
    }

    /**
     * Methods go thru all given classes and run the operation with databases which names are obtained from class name or annotation. What operation is done
     * depends on {@link #CouchDBSchemaProcessor(CouchDbClient, CouchDbProperties, ApplicationContext, EntityScanPackages)} parameter. In case of {@literal validate}, only check if all databases for all
     * known documents exists, in case of {@literal drop} all existing databases are deleted and created again, in case of create only missing databases are
     * created, the already existing are left untouched. If the operation is {@literal none}, no operation is done.
     *
     * @param entityClasses   {@link List} of all known entities mapping classes. Must not be {@literal null}
     * @param schemaOperation must not be {@literal null}
     * @param client          must not be {@literal null}
     * @throws IOException               if some operation to database fails
     * @throws SchemaProcessingException if validation fails
     */
    public void process(@NotNull List<Class<?>> entityClasses,
                        @NotNull SchemaOperation schemaOperation,
                        @NotNull CouchDbClient client) throws Exception {
        if (entityClasses.isEmpty()) {
            log.warn("No entities mapping found");
        } else {
            entityClasses.forEach(c -> log.info("Found entity mapping class {}", c.getName()));
        }
        log.debug("Starting schema processing with operation set to {}", schemaOperation.toString().toLowerCase());
        processSchema(entityClasses, schemaOperation, client);
        log.debug("Schema processing done");
    }

    /**
     * @param entityClasses   must not be {@literal null}
     * @param schemaOperation must not be {@literal null}
     * @param client          must not be {@literal null}
     * @throws IOException               if some operation to database fails
     * @throws SchemaProcessingException if validation fails
     * @see #process(List, SchemaOperation, CouchDbClient)
     */
    private void processSchema(@NotNull List<Class<?>> entityClasses,
                               @NotNull SchemaOperation schemaOperation,
                               @NotNull CouchDbClient client) throws Exception {
        for (Class<?> clazz : entityClasses) {
            schemaOperation.accept(clazz, client);
        }
        if (schemaOperation.hasFollowing()) {
            processSchema(entityClasses, schemaOperation.getFollowing(), client);
        }
    }

}
