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

package com.groocraft.couchdb.slacker;

import com.groocraft.couchdb.slacker.annotation.Document;
import com.groocraft.couchdb.slacker.configuration.CouchDbProperties;
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

import javax.annotation.concurrent.ThreadSafe;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Class which provides feature of database name context switching. There is {@link CouchDbContext#DEFAULT} context in which entities are mapped by annotations. Other
 * contexts can be added by configuration ({@link CouchDbProperties#setMapping(Map)}), {@link #add(DocumentDescriptor)} or
 * {@link #add(String, DocumentDescriptor)}. If a context added during a runtime, schema must be prepared or created by
 * {@link com.groocraft.couchdb.slacker.repository.CouchDBSchemaProcessor#processSchema(List, SchemaOperation)}.
 *
 * @author Majlanky
 * @see CouchDbProperties
 */
@Slf4j
@ThreadSafe
public class SpringCouchDbContext extends CouchDbContext {

    /**
     * @param properties         must not be {@literal null}
     * @param context            must not be {@literal null}
     * @param entityScanPackages can be {@literal null} when no {@link org.springframework.boot.autoconfigure.domain.EntityScan} used.
     * @throws ClassNotFoundException in case configuration {@link CouchDbProperties#getMapping()} contains non-existing class
     */
    public SpringCouchDbContext(@NotNull CouchDbProperties properties,
                                @NotNull ApplicationContext context,
                                @Nullable @Autowired(required = false) EntityScanPackages entityScanPackages) throws ClassNotFoundException {
        super(properties);
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

        register(entityClasses, properties);
    }

    /**
     * Method created {@link EntityMetadata} for all given {@code entityClass} and link it to the {@link CouchDbContext#DEFAULT} context. From the given {@code
     * properties} the {@link CouchDbProperties#getMapping()} if not empty and creates configured context with the given configuration.
     *
     * @param entityClasses all known classes annotated by {@link Document}. Must not be {@literal null}
     * @param properties    must not be {@literal null}
     */
    private void register(@NotNull List<Class<?>> entityClasses, @NotNull CouchDbProperties properties) {
        if (entityClasses.isEmpty()) {
            log.warn("No entities mapping found");
        } else {
            entityClasses.forEach(c -> log.info("Found entity mapping class {}", c.getName()));
        }

        Map<String, Class<?>> mapped = new HashMap<>();
        for (Class<?> clazz : entityClasses) {
            log.info("Registering class {} into {} context", clazz.getName(), CouchDbContext.DEFAULT);
            mapped.put(clazz.getName(), clazz);
            add(DocumentDescriptor.of(clazz));
        }

        log.info("{} database contexts found in configuration", properties.getMapping().size());

        for (String name : properties.getMapping().keySet()) {
            log.info("Registering entity mapping for context {}", name);
            for (CouchDbProperties.Document d : properties.getMapping().get(name)) {
                Class<?> entityClass = mapped.get(d.getEntityClass());
                Assert.notNull(entityClass, "Configured class " + d.getEntityClass() + " does not exist or is not annotated by Document");
                add(name, DocumentDescriptor.of(entityClass, d.getDatabase()));
                log.info("Class {} will be stored in {} database in {} context", d.getEntityClass(), d.getDatabase(), name);
            }
        }
    }

}
