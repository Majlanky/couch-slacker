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

package com.groocraft.couchdb.slacker.test.integration.mapping;

import com.groocraft.couchdb.slacker.CouchDbClient;
import com.groocraft.couchdb.slacker.CouchDbContext;
import com.groocraft.couchdb.slacker.annotation.EnableCouchDbRepositories;
import com.groocraft.couchdb.slacker.test.integration.TestDocument;
import com.groocraft.couchdb.slacker.test.integration.TestDocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {MappingTestConfiguration.class, TestDocumentRepository.class},
        initializers = ConfigDataApplicationContextInitializer.class)
@ActiveProfiles("mapping-test")
@EntityScan({"com.groocraft.couchdb.slacker.test.integration"})
@EnableCouchDbRepositories(basePackageClasses = TestDocumentRepository.class)
class MappingIntegrationTest {

    @Autowired
    CouchDbClient client;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    CouchDbContext context;

    @Autowired
    TestDocumentRepository repository;

    @BeforeEach
    void clear() {
        context.getAll().keySet().forEach(k -> context.doIn(k, () -> repository.deleteAll()));
    }

    @Test
    void testDatabaseExists() throws IOException {
        assertTrue(client.databaseExists("test1"), "Database test1 must be created by SchemaProcessor");
        assertTrue(client.databaseExists("test2"), "Database test2 must be created by SchemaProcessor");
        assertTrue(client.databaseExists("test3"), "Database test3 must be created by SchemaProcessor");
    }

    @ParameterizedTest
    @MethodSource("switchingSource")
    void testSwitching(String contextName, String databaseName) {
        context.doIn(contextName, () -> {
            assertEquals(0L, repository.count(), "Database " + databaseName + " should be empty in context " + contextName);
            repository.save(new TestDocument("defaultContext"));
            assertEquals(1L, repository.count(), "Database should contain saved document");
        });
    }

    private static Stream<Arguments> switchingSource() {
        return Stream.of(
                Arguments.of("default", "test"),
                Arguments.of("context1", "test1"),
                Arguments.of("context2", "test2"),
                Arguments.of("context3", "test3"));
    }

}
