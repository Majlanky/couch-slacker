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
import com.groocraft.couchdb.slacker.TestDocument;
import com.groocraft.couchdb.slacker.exception.SchemaProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CouchDBSchemaProcessorTest {

    @Mock
    public CouchDbClient client;

    @Test
    public void testNone() throws IOException, SchemaProcessingException {
        CouchDBSchemaProcessor schemaProcessor = new CouchDBSchemaProcessor(client, SchemaOperation.NONE);
        schemaProcessor.process(List.of(TestDocument.class));
        verify(client, never().description("No test should be called if operation is NONE")).databaseExists(TestDocument.class);
        verify(client, never().description("No create should be called if operation is NONE")).createDatabase(TestDocument.class);
        verify(client, never().description("No delete should be called if operation is NONE")).deleteDatabase(TestDocument.class);
    }

    @Test
    public void testValidate() throws IOException {
        when(client.getDatabaseName(TestDocument.class)).thenReturn("test");
        when(client.databaseExists(TestDocument.class)).thenReturn(true, false);
        CouchDBSchemaProcessor schemaProcessor = new CouchDBSchemaProcessor(client, SchemaOperation.VALIDATE);
        assertDoesNotThrow(() -> schemaProcessor.process(List.of(TestDocument.class)));
        assertThrows(SchemaProcessingException.class, () -> schemaProcessor.process(List.of(TestDocument.class)));
    }

    @Test
    public void testCreate() throws IOException, SchemaProcessingException {
        when(client.getDatabaseName(TestDocument.class)).thenReturn("test");
        when(client.databaseExists(TestDocument.class)).thenReturn(true, true, false, true);
        CouchDBSchemaProcessor schemaProcessor = new CouchDBSchemaProcessor(client, SchemaOperation.CREATE);
        schemaProcessor.process(List.of(TestDocument.class));
        verify(client, never().description("Create must no be called when database already exists")).createDatabase(TestDocument.class);
        schemaProcessor.process(List.of(TestDocument.class));
        verify(client, times(1).description("Missing database must be created")).createDatabase(TestDocument.class);
    }

    @Test
    public void testDrop() throws IOException, SchemaProcessingException{
        when(client.getDatabaseName(TestDocument.class)).thenReturn("test");
        when(client.databaseExists(TestDocument.class)).thenReturn(true, false, true);
        CouchDBSchemaProcessor schemaProcessor = new CouchDBSchemaProcessor(client, SchemaOperation.DROP);
        schemaProcessor.process(List.of(TestDocument.class));
        verify(client, times(1).description("Database must be deleted when DROP")).deleteDatabase(TestDocument.class);
        verify(client, times(1).description("Database must be created when DROP")).createDatabase(TestDocument.class);
    }
}