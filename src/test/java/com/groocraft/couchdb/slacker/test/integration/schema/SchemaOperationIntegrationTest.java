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

package com.groocraft.couchdb.slacker.test.integration.schema;

import com.groocraft.couchdb.slacker.CouchDbClient;
import com.groocraft.couchdb.slacker.CouchDbInitializer;
import com.groocraft.couchdb.slacker.DocumentDescriptor;
import com.groocraft.couchdb.slacker.EntityMetadata;
import com.groocraft.couchdb.slacker.SchemaOperation;
import com.groocraft.couchdb.slacker.annotation.EnableCouchDbRepositories;
import com.groocraft.couchdb.slacker.repository.CouchDBSchemaProcessor;
import com.groocraft.couchdb.slacker.structure.DesignDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SchemaOperationTestConfiguration.class, CouchDbInitializer.class},
        initializers = ConfigFileApplicationContextInitializer.class)
@ActiveProfiles("schema-test")
@EntityScan({"com.groocraft.couchdb.slacker.test.integration.schema"})
@EnableCouchDbRepositories
class SchemaOperationIntegrationTest {

    @Autowired
    CouchDbClient client;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    CouchDBSchemaProcessor processor;

    @Test
    void testDatabaseExists() throws IOException {
        assertTrue(client.databaseExists("schema-test"), "Database schema-test do not exists, but schema operation did not failed");
        DesignDocument design = assertDoesNotThrow(() -> client.readDesign("byType", "schema-test"), "It looks like schema processing " +
                "did not create design document byType");
        assertTrue(design.getViews().keySet().stream().anyMatch("schema"::equals), "It looks like schema processing did not create view 'schema' in " +
                "design document or overriding other views");
        assertTrue(design.getViews().keySet().stream().anyMatch("schema2"::equals), "It looks like schema processing did not create view 'schema2' in " +
                "design document or overriding other views");
        DesignDocument all = assertDoesNotThrow(() -> client.readDesign("all", "schema-test"), "It looks like schema processing " +
                "did not create design document all");
        assertTrue(all.getViews().keySet().stream().anyMatch("data"::equals), "It looks like schema processing did not create view 'data' in " +
                "design document or overriding other views");
    }

    @Test
    void testRuntimeAdd() throws Exception {
        processor.processSchema(Arrays.asList(
                new EntityMetadata(DocumentDescriptor.of(SchemaTestDocument.class, "schema-test-2")),
                new EntityMetadata(DocumentDescriptor.of(OtherSchemaTestDocument.class, "schema-test-2"))),
                SchemaOperation.CREATE);

        assertTrue(client.databaseExists("schema-test-2"), "Database schema-test-2 do not exists, but schema operation did not failed");
        DesignDocument design = assertDoesNotThrow(() -> client.readDesign("byType", "schema-test-2"), "It looks like schema processing " +
                "did not create design document byType");
        assertTrue(design.getViews().keySet().stream().anyMatch("schema"::equals), "It looks like schema processing did not create view 'schema' in " +
                "design document or overriding other views");
        assertTrue(design.getViews().keySet().stream().anyMatch("schema2"::equals), "It looks like schema processing did not create view 'schema2' in " +
                "design document or overriding other views");
        DesignDocument all = assertDoesNotThrow(() -> client.readDesign("all", "schema-test"), "It looks like schema processing " +
                "did not create design document all");
        assertTrue(all.getViews().keySet().stream().anyMatch("data"::equals), "It looks like schema processing did not create view 'data' in " +
                "design document or overriding other views");
    }

}
