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

import com.groocraft.couchdb.slacker.exception.CouchDbException;
import com.groocraft.couchdb.slacker.exception.CouchDbRuntimeException;
import com.groocraft.couchdb.slacker.exception.SchemaProcessingException;
import com.groocraft.couchdb.slacker.structure.DesignDocument;
import com.groocraft.couchdb.slacker.structure.View;
import com.groocraft.couchdb.slacker.utils.ThrowingBiConsumer;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;

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
     * The check that all databases, design documents and views configured in all {@link com.groocraft.couchdb.slacker.annotation.Document} in scanned packages.
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

    private final static String VIEW_MAP = "function(doc){if(doc.%1$s == \"%2$s\"){emit(doc._id, doc);}}";

    private final SchemaOperation following;
    private final ThrowingBiConsumer<Class<?>, CouchDbClient, Exception> action;

    SchemaOperation(SchemaOperation following, ThrowingBiConsumer<Class<?>, CouchDbClient, Exception> action) {
        this.following = following;
        this.action = action;
    }

    public boolean hasFollowing() {
        return following != null;
    }

    public SchemaOperation getFollowing() {
        return following;
    }

    public void accept(Class<?> clazz, CouchDbClient client) throws Exception {
        if (action != null) {
            action.accept(clazz, client);
        }
    }

    private static void drop(Class<?> clazz, CouchDbClient client) throws IOException {
        EntityMetadata<?> metadata = client.getEntityMetadata(clazz);
        if (client.databaseExists(clazz)) {
            log.info("Database {} exists and it will be deleted", metadata.getDatabaseName());
            client.deleteDatabase(clazz);
        }
    }

    private static void create(Class<?> clazz, CouchDbClient client) throws IOException {
        EntityMetadata<?> metadata = client.getEntityMetadata(clazz);
        if (!client.databaseExists(clazz)) {
            log.info("Database {} not found and it will be created", metadata.getDatabaseName());
            client.createDatabase(clazz);
        }
        if (metadata.isViewed()) {
            log.info("Entities of {} should be accessed by views and types, going to create design and views if necessary", clazz.getSimpleName());
            DesignDocument design = getDesignDocument(client, metadata.getDesign(), metadata.getDatabaseName()).
                    orElseGet(() -> {
                        log.info("Design {} not found, creating new", metadata.getDesign());
                        return new DesignDocument(metadata.getDesign(), new HashSet<>());
                    });
            if (design.getViews().keySet().stream().noneMatch(k -> metadata.getView().equals(k))) {
                log.info("View {} not found in design {}, creating new", metadata.getView(), metadata.getDesign());
                View view = new View(metadata.getView(), String.format(VIEW_MAP, metadata.getTypeField(), metadata.getType()), null);
                design.addView(view);
                client.saveDesign(design, metadata.getDatabaseName());
            }
        }
    }

    private static void validate(Class<?> clazz, CouchDbClient client) throws IOException, SchemaProcessingException {
        EntityMetadata<?> metadata = client.getEntityMetadata(clazz);
        log.info("Validating that database {} exists", metadata.getDatabaseName());
        if (!client.databaseExists(clazz)) {
            throw new SchemaProcessingException(String.format("Database %s does not exists", metadata.getDatabaseName()));
        }
        if (metadata.isViewed()) {
            log.info("Entities of {} should be accessed by views and types, validating design and views", clazz.getSimpleName());
            try {
                DesignDocument design = client.readDesign(metadata.getDesign(), metadata.getDatabaseName());
                if (design.getViews().keySet().stream().noneMatch(k -> metadata.getView().equals(k))) {
                    throw new SchemaProcessingException(String.format("View %s does not exist in design %s of %s database", metadata.getView(),
                            metadata.getDesign(), metadata.getDatabaseName()));
                }
            } catch (IOException ex) {
                throw new SchemaProcessingException(String.format("Design document %s does not exists in database %s", metadata.getDesign(),
                        metadata.getDatabaseName()), ex);
            }
            log.info("All designs and view exits for {}", clazz.getSimpleName());
        }
        log.info("Database {} exists", metadata.getDatabaseName());
    }

    private static Optional<DesignDocument> getDesignDocument(CouchDbClient client, String id, String database) {
        try {
            return Optional.of(client.readDesign(id, database));
        } catch (CouchDbException couchDbException) {
            if (couchDbException.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                return Optional.empty();
            } else {
                throw new CouchDbRuntimeException("Unable to find " + id, couchDbException);
            }
        } catch (IOException e) {
            throw new CouchDbRuntimeException("Unable to find " + id, e);
        }
    }

}
