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

import com.groocraft.couchdb.slacker.exception.SchemaProcessingException;
import com.groocraft.couchdb.slacker.structure.DesignDocument;
import com.groocraft.couchdb.slacker.structure.View;
import com.groocraft.couchdb.slacker.utils.ThrowingBiConsumer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;

@Slf4j
public enum SchemaOperation {

    /**
     * No validation is done
     */
    NONE(null, null),
    /**
     * The check that all databases, design documents and views configured in all {@link com.groocraft.couchdb.slacker.annotation.Document} in scanned
     * packages exists.
     */
    VALIDATE(null, SchemaOperation::validate),
    /**
     * Creates all non-existing databases, design documents and views configured in all {@link com.groocraft.couchdb.slacker.annotation.Document} in scanned
     * packages. Validation is executed after the creation.
     */
    CREATE(VALIDATE, SchemaOperation::create),
    /**
     * Deletes all existing databases, design documents and views configured in all {@link com.groocraft.couchdb.slacker.annotation.Document} in scanned
     * packages. Create and validate operations are executed after the dropping. Can be dangerous if databases contains data!
     */
    DROP(CREATE, SchemaOperation::drop);

    private final SchemaOperation following;
    private final ThrowingBiConsumer<EntityMetadata, CouchDbClient, Exception> action;

    SchemaOperation(SchemaOperation following, ThrowingBiConsumer<EntityMetadata, CouchDbClient, Exception> action) {
        this.following = following;
        this.action = action;
    }

    public boolean hasFollowing() {
        return following != null;
    }

    public SchemaOperation getFollowing() {
        return following;
    }

    public void accept(EntityMetadata metadata, CouchDbClient client) throws Exception {
        if (action != null) {
            action.accept(metadata, client);
        }
    }

    private static void drop(EntityMetadata metadata, CouchDbClient client) throws IOException {
        if (client.databaseExists(metadata.getDatabaseName())) {
            log.info("Database {} exists and it will be deleted", metadata.getDatabaseName());
            client.deleteDatabase(metadata.getDatabaseName());
        }
    }

    private static void create(EntityMetadata metadata, CouchDbClient client) throws IOException {
        if (!client.databaseExists(metadata.getDatabaseName())) {
            log.info("Database {} not found and it will be created", metadata.getDatabaseName());
            client.createDatabase(metadata.getDatabaseName());
        }
        log.info("Checking design {} for {} database", CouchDbClient.ALL_DESIGN, metadata.getDatabaseName());
        DesignDocument allDesign = client.readDesignSafely(CouchDbClient.ALL_DESIGN, metadata.getDatabaseName()).
                orElseGet(() -> {
                    log.info("Design {} not found in {} database, creating new", CouchDbClient.ALL_DESIGN, metadata.getDatabaseName());
                    return new DesignDocument(CouchDbClient.ALL_DESIGN, new HashSet<>());
                });
        View dataView = allDesign.getViews().get(CouchDbClient.ALL_DATA_VIEW);
        if (dataView == null) {
            log.info("View {} not found in design {}, creating new", CouchDbClient.ALL_DATA_VIEW, CouchDbClient.ALL_DESIGN);
            dataView = new View(CouchDbClient.ALL_DATA_VIEW, CouchDbClient.ALL_DATA_MAP, CouchDbClient.COUNT_REDUCE);
            allDesign.addView(dataView);
            client.saveDesign(allDesign, metadata.getDatabaseName());
        } else {
            log.info("View {} exists, checking that mapping and reduce functions matches", CouchDbClient.ALL_DATA_VIEW);
            if (!CouchDbClient.ALL_DATA_MAP.equals(dataView.getMapFunction()) || !CouchDbClient.COUNT_REDUCE.equals(dataView.getReduceFunction())) {
                log.info("View functions do not match expectation, view will be altered");
                dataView.setMapFunction(CouchDbClient.ALL_DATA_MAP);
                dataView.setReduceFunction(CouchDbClient.COUNT_REDUCE);
                client.saveDesign(allDesign, metadata.getDatabaseName());
            }
        }
        if (metadata.isViewed()) {
            log.info("Entities in database {} should be accessed by views and types, going to create design and views if necessary", metadata.getDatabaseName());
            DesignDocument design = client.readDesignSafely(metadata.getDesign(), metadata.getDatabaseName()).
                    orElseGet(() -> {
                        log.info("Design {} not found, creating new", metadata.getDesign());
                        return new DesignDocument(metadata.getDesign(), new HashSet<>());
                    });
            View view = design.getViews().get(metadata.getView());
            if (view == null) {
                log.info("View {} not found in design {}, creating new", metadata.getView(), metadata.getDesign());
                view = new View(metadata.getView(), String.format(CouchDbClient.VIEW_MAP, metadata.getTypeField(), metadata.getType()), CouchDbClient.COUNT_REDUCE);
                design.addView(view);
                client.saveDesign(design, metadata.getDatabaseName());
            } else {
                log.info("View {} exists, checking that mapping and reduce functions matches", metadata.getView());
                String wantedMapping = String.format(CouchDbClient.VIEW_MAP, metadata.getTypeField(), metadata.getType());
                if (!wantedMapping.equals(view.getMapFunction()) || !CouchDbClient.COUNT_REDUCE.equals(view.getReduceFunction())) {
                    log.info("View functions do not match expectation, view will be altered");
                    view.setMapFunction(wantedMapping);
                    view.setReduceFunction(CouchDbClient.COUNT_REDUCE);
                    client.saveDesign(design, metadata.getDatabaseName());
                }
            }
        }
    }

    private static void validate(EntityMetadata metadata, CouchDbClient client) throws IOException, SchemaProcessingException {
        log.info("Validating that database {} exists", metadata.getDatabaseName());
        if (!client.databaseExists(metadata.getDatabaseName())) {
            throw new SchemaProcessingException(String.format("Database %s does not exists", metadata.getDatabaseName()));
        }
        log.info("Validating that all expected basic design document and views exists");
        Optional<DesignDocument> designAll = client.readDesignSafely(CouchDbClient.ALL_DESIGN, metadata.getDatabaseName());
        if (!designAll.isPresent()) {
            throw new SchemaProcessingException(String.format("Design '%s' does not exist in %s database", CouchDbClient.ALL_DESIGN, metadata.getDatabaseName()));
        }
        View dataView = designAll.get().getViews().get(CouchDbClient.ALL_DATA_VIEW);
        if (dataView == null) {
            throw new SchemaProcessingException(String.format("View %s does not exist in design %s of %s database",
                    CouchDbClient.ALL_DATA_VIEW, CouchDbClient.ALL_DESIGN, metadata.getDatabaseName()));
        }
        if (!CouchDbClient.ALL_DATA_MAP.equals(dataView.getMapFunction()) || !CouchDbClient.COUNT_REDUCE.equals(dataView.getReduceFunction())) {
            throw new SchemaProcessingException(String.format("View %s in design %s of %s database contains improper functions",
                    CouchDbClient.ALL_DATA_VIEW, CouchDbClient.ALL_DESIGN, metadata.getDatabaseName()));
        }
        if (metadata.isViewed()) {
            log.info("Entities in database {} should be accessed by views and types, validating design and views", metadata.getDatabaseName());
            Optional<DesignDocument> design = client.readDesignSafely(metadata.getDesign(), metadata.getDatabaseName());
            if (!design.isPresent()) {
                throw new SchemaProcessingException(String.format("Design '%s' does not exist in %s database", metadata.getDesign(), metadata.getDatabaseName()));
            }
            View view = design.get().getViews().get(metadata.getView());
            if (view == null) {
                throw new SchemaProcessingException(String.format("View %s does not exist in design %s of %s database",
                        metadata.getView(), metadata.getDesign(), metadata.getDatabaseName()));
            }
            String wantedMapping = String.format(CouchDbClient.VIEW_MAP, metadata.getTypeField(), metadata.getType());
            if (!wantedMapping.equals(view.getMapFunction()) || !CouchDbClient.COUNT_REDUCE.equals(view.getReduceFunction())) {
                throw new SchemaProcessingException(String.format("View %s in design %s of %s database contains improper functions",
                        CouchDbClient.ALL_DATA_VIEW, CouchDbClient.ALL_DESIGN, metadata.getDatabaseName()));
            }
            log.info("All designs and view exits in {} database", metadata.getDatabaseName());
        }
        log.info("Database {} exists", metadata.getDatabaseName());
    }

}
