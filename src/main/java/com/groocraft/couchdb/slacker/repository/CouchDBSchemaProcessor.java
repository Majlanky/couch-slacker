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
import com.groocraft.couchdb.slacker.exception.SchemaProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.List;

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

    private final CouchDbClient client;
    private final SchemaOperation schemaOperation;

    /**
     * @param client          must not be {@literal null}
     * @param schemaOperation must not be {@literal null}
     */
    public CouchDBSchemaProcessor(@NotNull CouchDbClient client, @NotNull SchemaOperation schemaOperation) {
        Assert.notNull(client, "Client must not be null.");
        Assert.notNull(schemaOperation, "SchemaOperation must not be null");
        this.client = client;
        this.schemaOperation = schemaOperation;
    }

    /**
     * Methods go thru all given classes and run the operation with databases which names are obtained from class name or annotation. What operation is done
     * depends on {@link #CouchDBSchemaProcessor(CouchDbClient, SchemaOperation)} parameter. In case of {@literal validate}, only check if all databases for all
     * known documents exists, in case of {@literal drop} all existing databases are deleted and created again, in case of create only missing databases are
     * created, the already existing are left untouched. If the operation is {@literal none}, no operation is done.
     *
     * @param entityClasses {@link List} of all known entities mapping classes. Must not be {@literal null}
     * @throws IOException               if some operation to database fails
     * @throws SchemaProcessingException if validation fails
     */
    public void process(@NotNull List<Class<?>> entityClasses) throws IOException, SchemaProcessingException {
        if (entityClasses.isEmpty()) {
            log.warn("No entities mapping found");
        } else {
            entityClasses.forEach(c -> log.info("Found entity mapping class {}", c.getName()));
        }
        log.debug("Starting schema processing with operation set to {}", schemaOperation.toString().toLowerCase());
        for (Class<?> clazz : entityClasses) {
            processSchema(clazz, schemaOperation);
        }
        log.debug("Schema processing done");
    }

    /**
     * @param clazz           must not be {@literal null}
     * @param schemaOperation must not be {@literal null}
     * @throws IOException               if some operation to database fails
     * @throws SchemaProcessingException if validation fails
     * @see #process(List)
     */
    private void processSchema(@NotNull Class<?> clazz, @NotNull SchemaOperation schemaOperation) throws IOException, SchemaProcessingException {
        if (schemaOperation != SchemaOperation.NONE) {
            String databaseName = client.getDatabaseName(clazz);
            switch (schemaOperation) {
                case DROP:
                    if (client.databaseExists(clazz)) {
                        log.debug("Database {} exists and it will be deleted", databaseName);
                        client.deleteDatabase(clazz);
                    }
                case CREATE:
                    if (!client.databaseExists(clazz)) {
                        log.debug("Database {} not found and it will be created", databaseName);
                        client.createDatabase(clazz);
                    }
                case VALIDATE:
                    log.debug("Validating that database {} exists", databaseName);
                    if (!client.databaseExists(clazz)) {
                        throw new SchemaProcessingException(String.format("Database %s do not exists", databaseName));
                    }
                    log.info("Database {} exists", databaseName);
            }
        }
    }

}
