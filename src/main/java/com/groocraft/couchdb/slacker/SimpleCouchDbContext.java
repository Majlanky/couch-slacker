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

import com.groocraft.couchdb.slacker.configuration.CouchDbProperties;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.ThreadSafe;
import java.util.List;
import java.util.Map;

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
public class SimpleCouchDbContext extends CouchDbContext {

    /**
     * @param properties must not be {@literal null}
     */
    public SimpleCouchDbContext(@NotNull CouchDbProperties properties) {
        super(properties);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized @NotNull EntityMetadata get(@NotNull Class<?> clazz) {
        if (!contains(clazz)) {
            log.info("Registering class {} into {} context", clazz.getName(), CouchDbContext.DEFAULT);
            add(DocumentDescriptor.of(clazz));
            for (String name : getCouchDbProperties().getMapping().keySet()) {
                for (CouchDbProperties.Document d : getCouchDbProperties().getMapping().get(name)) {
                    if (clazz.getName().equals(d.getEntityClass())) {
                        add(name, DocumentDescriptor.of(clazz, d.getDatabase()));
                        log.info("Class {} will be stored in {} database in {} context", d.getEntityClass(), d.getDatabase(), name);
                    }
                }
            }
        }
        return super.get(clazz);
    }

}