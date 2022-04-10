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
import com.groocraft.couchdb.slacker.CouchDbContext;
import com.groocraft.couchdb.slacker.EntityMetadata;
import com.groocraft.couchdb.slacker.SchemaOperation;
import com.groocraft.couchdb.slacker.configuration.CouchDbProperties;
import com.groocraft.couchdb.slacker.exception.SchemaProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScanPackages;
import org.springframework.util.Assert;

import java.io.IOException;
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

    private final CouchDbClient client;

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
                                  @NotNull CouchDbContext context,
                                  @Nullable @Autowired(required = false) EntityScanPackages entityScanPackages) throws Exception {
        Assert.notNull(client, "Client must not be null.");
        Assert.notNull(properties, "SchemaOperation must not be null");
        Assert.notNull(context, "Context must not be null");
        this.client = client;
        process(context, properties.getSchemaOperation());
    }

    /**
     * Methods go thru all given classes and run the operation with databases which names are obtained from class name or annotation. What operation is done
     * depends on {@link #CouchDBSchemaProcessor(CouchDbClient, CouchDbProperties, CouchDbContext, EntityScanPackages)} parameter. In case of {@literal validate}, only check if all databases for all
     * known documents exists, in case of {@literal drop} all existing databases are deleted and created again, in case of create only missing databases are
     * created, the already existing are left untouched. If the operation is {@literal none}, no operation is done.
     *
     * @param context         must not be {@literal null}
     * @param schemaOperation must not be {@literal null}
     * @throws IOException               if some operation to database fails
     * @throws SchemaProcessingException if validation fails
     */
    public void process(@NotNull CouchDbContext context,
                        @NotNull SchemaOperation schemaOperation) throws Exception {
        List<EntityMetadata> allMetadata = context.getAll().values().stream().flatMap(m -> m.values().stream()).collect(Collectors.toList());
        log.debug("Starting schema processing with operation set to {}", schemaOperation.toString().toLowerCase());
        processSchema(allMetadata, schemaOperation);
        log.debug("Schema processing done");
    }

    /**
     * @param allMetadata     must not be {@literal null}
     * @param schemaOperation must not be {@literal null}
     * @throws IOException               if some operation to database fails
     * @throws SchemaProcessingException if validation fails
     * @see #process(CouchDbContext, SchemaOperation)
     */
    public void processSchema(@NotNull List<EntityMetadata> allMetadata,
                              @NotNull SchemaOperation schemaOperation) throws Exception {
        for (EntityMetadata metadata : allMetadata) {
            schemaOperation.accept(metadata, client);
        }
        if (schemaOperation.hasFollowing()) {
            processSchema(allMetadata, schemaOperation.getFollowing());
        }
    }

}
