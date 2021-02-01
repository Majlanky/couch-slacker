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
import com.groocraft.couchdb.slacker.EntityMetadata;
import com.groocraft.couchdb.slacker.SchemaOperation;
import com.groocraft.couchdb.slacker.configuration.CouchDbProperties;
import com.groocraft.couchdb.slacker.exception.SchemaProcessingException;
import com.groocraft.couchdb.slacker.repository.schema.SchemaUnitTestDocument;
import com.groocraft.couchdb.slacker.structure.DesignDocument;
import com.groocraft.couchdb.slacker.structure.View;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CouchDBSchemaProcessorTest {

    @Mock
    CouchDbClient client;

    @Mock
    CouchDbProperties properties;

    @Mock
    ApplicationContext context;

    @Test
    void testNone() throws Exception {
        when(properties.getSchemaOperation()).thenReturn(SchemaOperation.NONE);
        Map<String, Object> beans = new HashMap<>();
        beans.put("test", new SchemaUnitTestDocument());
        when(context.getBeansWithAnnotation(any())).thenReturn(beans);
        new CouchDBSchemaProcessor(client, properties, context, null);
        verify(client, never().description("No test should be called if operation is NONE")).databaseExists(SchemaUnitTestDocument.class);
        verify(client, never().description("No create should be called if operation is NONE")).createDatabase(SchemaUnitTestDocument.class);
        verify(client, never().description("No delete should be called if operation is NONE")).deleteDatabase(SchemaUnitTestDocument.class);
    }

    @Test
    void testValidate() throws Exception {
        when(properties.getSchemaOperation()).thenReturn(SchemaOperation.VALIDATE);
        Map<String, Object> beans = new HashMap<>();
        beans.put("test", new SchemaUnitTestDocument());
        when(context.getBeansWithAnnotation(any())).thenReturn(beans);
        when(client.getEntityMetadata(SchemaUnitTestDocument.class)).thenReturn(new EntityMetadata(SchemaUnitTestDocument.class));
        when(client.databaseExists(SchemaUnitTestDocument.class)).thenReturn(true, false);
        when(client.readDesignSafely("all", "test")).thenReturn(Optional.of(
                new DesignDocument("all", Collections.singleton(new View("data", "function(doc){emit(null);}", "_count")))));
        assertDoesNotThrow(() -> new CouchDBSchemaProcessor(client, properties, context, null));
        assertThrows(SchemaProcessingException.class, () -> new CouchDBSchemaProcessor(client, properties, context, null));
    }

    @Test
    void testCreate() throws Exception {
        when(properties.getSchemaOperation()).thenReturn(SchemaOperation.CREATE);
        Map<String, Object> beans = new HashMap<>();
        beans.put("test", new SchemaUnitTestDocument());
        when(context.getBeansWithAnnotation(any())).thenReturn(beans);
        when(client.getEntityMetadata(SchemaUnitTestDocument.class)).thenReturn(new EntityMetadata(SchemaUnitTestDocument.class));
        when(client.databaseExists(SchemaUnitTestDocument.class)).thenReturn(true, true, false, true);
        when(client.readDesignSafely("all", "test")).thenReturn(Optional.of(
                new DesignDocument("all", Collections.singleton(new View("data", "function(doc){emit(null);}", "_count")))));
        new CouchDBSchemaProcessor(client, properties, context, null);
        verify(client, never().description("Create must no be called when database already exists")).createDatabase(SchemaUnitTestDocument.class);
        new CouchDBSchemaProcessor(client, properties, context, null);
        verify(client, times(1).description("Missing database must be created")).createDatabase(SchemaUnitTestDocument.class);
    }

    @Test
    void testDrop() throws Exception {
        when(properties.getSchemaOperation()).thenReturn(SchemaOperation.DROP);
        Map<String, Object> beans = new HashMap<>();
        beans.put("test", new SchemaUnitTestDocument());
        when(context.getBeansWithAnnotation(any())).thenReturn(beans);
        when(client.getEntityMetadata(SchemaUnitTestDocument.class)).thenReturn(new EntityMetadata(SchemaUnitTestDocument.class));
        when(client.databaseExists(SchemaUnitTestDocument.class)).thenReturn(true, false, true);
        when(client.readDesignSafely("all", "test")).thenReturn(Optional.of(
                new DesignDocument("all", Collections.singleton(new View("data", "function(doc){emit(null);}", "_count")))));
        new CouchDBSchemaProcessor(client, properties, context, null);
        verify(client, times(1).description("Database must be deleted when DROP")).deleteDatabase(SchemaUnitTestDocument.class);
        verify(client, times(1).description("Database must be created when DROP")).createDatabase(SchemaUnitTestDocument.class);
    }
}