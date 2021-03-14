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
import com.groocraft.couchdb.slacker.CouchDbContext;
import com.groocraft.couchdb.slacker.DocumentDescriptor;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
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
    CouchDbContext context;

    @Test
    void testNone() throws Exception {
        when(properties.getSchemaOperation()).thenReturn(SchemaOperation.NONE);
        Map<String, Map<Class<?>, EntityMetadata>> mapping = new HashMap<>();
        mapping.computeIfAbsent("default", k -> new HashMap<>()).put(SchemaUnitTestDocument.class,
                new EntityMetadata(DocumentDescriptor.of(SchemaUnitTestDocument.class)));
        when(context.getAll()).thenReturn(mapping);
        new CouchDBSchemaProcessor(client, properties, context, null);
        verify(client, never().description("No test should be called if operation is NONE")).databaseExists("test");
        verify(client, never().description("No create should be called if operation is NONE")).createDatabase("test");
        verify(client, never().description("No delete should be called if operation is NONE")).deleteDatabase("test");
    }

    @Test
    void testValidate() throws Exception {
        when(properties.getSchemaOperation()).thenReturn(SchemaOperation.VALIDATE);
        Map<String, Map<Class<?>, EntityMetadata>> mapping = new HashMap<>();
        mapping.computeIfAbsent("default", k -> new HashMap<>()).put(SchemaUnitTestDocument.class,
                new EntityMetadata(DocumentDescriptor.of(SchemaUnitTestDocument.class)));
        when(context.getAll()).thenReturn(mapping);
        when(client.databaseExists("test")).thenReturn(true, false);
        when(client.readDesignSafely("all", "test")).thenReturn(Optional.of(
                new DesignDocument("all", Collections.singleton(new View("data", "function(doc){emit(null);}", "_count")))));
        assertDoesNotThrow(() -> new CouchDBSchemaProcessor(client, properties, context, null));
        assertThrows(SchemaProcessingException.class, () -> new CouchDBSchemaProcessor(client, properties, context, null));
    }

    @Test
    void testCreate() throws Exception {
        when(properties.getSchemaOperation()).thenReturn(SchemaOperation.CREATE);
        Map<String, Map<Class<?>, EntityMetadata>> mapping = new HashMap<>();
        mapping.computeIfAbsent("default", k -> new HashMap<>()).put(SchemaUnitTestDocument.class,
                new EntityMetadata(DocumentDescriptor.of(SchemaUnitTestDocument.class)));
        when(context.getAll()).thenReturn(mapping);
        when(client.databaseExists("test")).thenReturn(true, true, false, true);
        when(client.readDesignSafely("all", "test")).thenReturn(Optional.of(
                new DesignDocument("all", Collections.singleton(new View("data", "function(doc){emit(null);}", "_count")))));
        new CouchDBSchemaProcessor(client, properties, context, null);
        verify(client, never().description("Create must no be called when database already exists")).createDatabase("test");
        new CouchDBSchemaProcessor(client, properties, context, null);
        verify(client, times(1).description("Missing database must be created")).createDatabase("test");
    }

    @Test
    void testDrop() throws Exception {
        when(properties.getSchemaOperation()).thenReturn(SchemaOperation.DROP);
        Map<String, Map<Class<?>, EntityMetadata>> mapping = new HashMap<>();
        mapping.computeIfAbsent("default", k -> new HashMap<>()).put(SchemaUnitTestDocument.class,
                new EntityMetadata(DocumentDescriptor.of(SchemaUnitTestDocument.class)));
        when(context.getAll()).thenReturn(mapping);
        when(client.databaseExists("test")).thenReturn(true, false, true);
        when(client.readDesignSafely("all", "test")).thenReturn(Optional.of(
                new DesignDocument("all", Collections.singleton(new View("data", "function(doc){emit(null);}", "_count")))));
        new CouchDBSchemaProcessor(client, properties, context, null);
        verify(client, times(1).description("Database must be deleted when DROP")).deleteDatabase("test");
        verify(client, times(1).description("Database must be created when DROP")).createDatabase("test");
    }

    @Test
    void testContextualNone() throws Exception {
        when(properties.getSchemaOperation()).thenReturn(SchemaOperation.NONE);
        Map<String, Map<Class<?>, EntityMetadata>> mapping = new HashMap<>();
        mapping.computeIfAbsent("default", k -> new HashMap<>()).put(SchemaUnitTestDocument.class,
                new EntityMetadata(DocumentDescriptor.of(SchemaUnitTestDocument.class)));
        mapping.computeIfAbsent("context1", k -> new HashMap<>()).put(SchemaUnitTestDocument.class,
                new EntityMetadata(DocumentDescriptor.of(SchemaUnitTestDocument.class, "context1Test")));
        mapping.computeIfAbsent("context2", k -> new HashMap<>()).put(SchemaUnitTestDocument.class,
                new EntityMetadata(DocumentDescriptor.of(SchemaUnitTestDocument.class, "context2Test")));
        mapping.computeIfAbsent("context3", k -> new HashMap<>()).put(SchemaUnitTestDocument.class,
                new EntityMetadata(DocumentDescriptor.of(SchemaUnitTestDocument.class, "context3Test")));
        when(context.getAll()).thenReturn(mapping);
        new CouchDBSchemaProcessor(client, properties, context, null);
        verify(client, never().description("No test should be called if operation is NONE")).databaseExists("test");
        verify(client, never().description("No create should be called if operation is NONE")).createDatabase("test");
        verify(client, never().description("No delete should be called if operation is NONE")).deleteDatabase("test");
        verify(client, never().description("No test should be called if operation is NONE")).databaseExists("context1Test");
        verify(client, never().description("No create should be called if operation is NONE")).createDatabase("context1Test");
        verify(client, never().description("No delete should be called if operation is NONE")).deleteDatabase("context1Test");
        verify(client, never().description("No test should be called if operation is NONE")).databaseExists("context2Test");
        verify(client, never().description("No create should be called if operation is NONE")).createDatabase("context2Test");
        verify(client, never().description("No delete should be called if operation is NONE")).deleteDatabase("context2Test");
        verify(client, never().description("No test should be called if operation is NONE")).databaseExists("context3Test");
        verify(client, never().description("No create should be called if operation is NONE")).createDatabase("context3Test");
        verify(client, never().description("No delete should be called if operation is NONE")).deleteDatabase("context3Test");
    }

    @Test
    void testContextualValidate() throws Exception {
        when(properties.getSchemaOperation()).thenReturn(SchemaOperation.VALIDATE);
        Map<String, Map<Class<?>, EntityMetadata>> mapping = new HashMap<>();
        mapping.computeIfAbsent("default", k -> new HashMap<>()).put(SchemaUnitTestDocument.class,
                new EntityMetadata(DocumentDescriptor.of(SchemaUnitTestDocument.class)));
        mapping.computeIfAbsent("context1", k -> new HashMap<>()).put(SchemaUnitTestDocument.class,
                new EntityMetadata(DocumentDescriptor.of(SchemaUnitTestDocument.class, "context1Test")));
        mapping.computeIfAbsent("context2", k -> new HashMap<>()).put(SchemaUnitTestDocument.class,
                new EntityMetadata(DocumentDescriptor.of(SchemaUnitTestDocument.class, "context2Test")));
        mapping.computeIfAbsent("context3", k -> new HashMap<>()).put(SchemaUnitTestDocument.class,
                new EntityMetadata(DocumentDescriptor.of(SchemaUnitTestDocument.class, "context3Test")));
        when(context.getAll()).thenReturn(mapping);
        when(client.databaseExists(anyString())).thenReturn(true, true, true, true, false, false, false, false);
        when(client.readDesignSafely("all", "test")).thenReturn(Optional.of(
                new DesignDocument("all", Collections.singleton(new View("data", "function(doc){emit(null);}", "_count")))));
        when(client.readDesignSafely("all", "context1Test")).thenReturn(Optional.of(
                new DesignDocument("all", Collections.singleton(new View("data", "function(doc){emit(null);}", "_count")))));
        when(client.readDesignSafely("all", "context2Test")).thenReturn(Optional.of(
                new DesignDocument("all", Collections.singleton(new View("data", "function(doc){emit(null);}", "_count")))));
        when(client.readDesignSafely("all", "context3Test")).thenReturn(Optional.of(
                new DesignDocument("all", Collections.singleton(new View("data", "function(doc){emit(null);}", "_count")))));
        assertDoesNotThrow(() -> new CouchDBSchemaProcessor(client, properties, context, null));
        assertThrows(SchemaProcessingException.class, () -> new CouchDBSchemaProcessor(client, properties, context, null));
    }

    @Test
    void testContextualCreate() throws Exception {
        when(properties.getSchemaOperation()).thenReturn(SchemaOperation.CREATE);
        Map<String, Map<Class<?>, EntityMetadata>> mapping = new HashMap<>();
        mapping.computeIfAbsent("default", k -> new HashMap<>()).put(SchemaUnitTestDocument.class,
                new EntityMetadata(DocumentDescriptor.of(SchemaUnitTestDocument.class)));
        mapping.computeIfAbsent("context1", k -> new HashMap<>()).put(SchemaUnitTestDocument.class,
                new EntityMetadata(DocumentDescriptor.of(SchemaUnitTestDocument.class, "context1Test")));
        mapping.computeIfAbsent("context2", k -> new HashMap<>()).put(SchemaUnitTestDocument.class,
                new EntityMetadata(DocumentDescriptor.of(SchemaUnitTestDocument.class, "context2Test")));
        mapping.computeIfAbsent("context3", k -> new HashMap<>()).put(SchemaUnitTestDocument.class,
                new EntityMetadata(DocumentDescriptor.of(SchemaUnitTestDocument.class, "context3Test")));
        when(context.getAll()).thenReturn(mapping);
        when(client.databaseExists(anyString())).thenReturn(true, true, true, true, true, true, true, true,
                false, false, false, false, true, true, true, true);
        when(client.readDesignSafely("all", "test")).thenReturn(Optional.of(
                new DesignDocument("all", Collections.singleton(new View("data", "function(doc){emit(null);}", "_count")))));
        when(client.readDesignSafely("all", "context1Test")).thenReturn(Optional.of(
                new DesignDocument("all", Collections.singleton(new View("data", "function(doc){emit(null);}", "_count")))));
        when(client.readDesignSafely("all", "context2Test")).thenReturn(Optional.of(
                new DesignDocument("all", Collections.singleton(new View("data", "function(doc){emit(null);}", "_count")))));
        when(client.readDesignSafely("all", "context3Test")).thenReturn(Optional.of(
                new DesignDocument("all", Collections.singleton(new View("data", "function(doc){emit(null);}", "_count")))));
        new CouchDBSchemaProcessor(client, properties, context, null);
        verify(client, never().description("Create must no be called when database already exists")).createDatabase("test");
        verify(client, never().description("Create must no be called when database already exists")).createDatabase("context1Test");
        verify(client, never().description("Create must no be called when database already exists")).createDatabase("context2Test");
        verify(client, never().description("Create must no be called when database already exists")).createDatabase("context3Test");
        new CouchDBSchemaProcessor(client, properties, context, null);
        verify(client, times(1).description("Missing database must be created")).createDatabase("test");
        verify(client, times(1).description("Missing database must be created")).createDatabase("context1Test");
        verify(client, times(1).description("Missing database must be created")).createDatabase("context2Test");
        verify(client, times(1).description("Missing database must be created")).createDatabase("context3Test");
    }

    @Test
    void testContextualDrop() throws Exception {
        when(properties.getSchemaOperation()).thenReturn(SchemaOperation.DROP);
        Map<String, Map<Class<?>, EntityMetadata>> mapping = new HashMap<>();
        mapping.computeIfAbsent("default", k -> new HashMap<>()).put(SchemaUnitTestDocument.class,
                new EntityMetadata(DocumentDescriptor.of(SchemaUnitTestDocument.class)));
        mapping.computeIfAbsent("context1", k -> new HashMap<>()).put(SchemaUnitTestDocument.class,
                new EntityMetadata(DocumentDescriptor.of(SchemaUnitTestDocument.class, "context1Test")));
        mapping.computeIfAbsent("context2", k -> new HashMap<>()).put(SchemaUnitTestDocument.class,
                new EntityMetadata(DocumentDescriptor.of(SchemaUnitTestDocument.class, "context2Test")));
        mapping.computeIfAbsent("context3", k -> new HashMap<>()).put(SchemaUnitTestDocument.class,
                new EntityMetadata(DocumentDescriptor.of(SchemaUnitTestDocument.class, "context3Test")));
        when(context.getAll()).thenReturn(mapping);
        when(client.databaseExists(anyString())).thenReturn(true, true, true, true, false, false, false, false, true, true, true, true);
        when(client.readDesignSafely("all", "test")).thenReturn(Optional.of(
                new DesignDocument("all", Collections.singleton(new View("data", "function(doc){emit(null);}", "_count")))));
        when(client.readDesignSafely("all", "context1Test")).thenReturn(Optional.of(
                new DesignDocument("all", Collections.singleton(new View("data", "function(doc){emit(null);}", "_count")))));
        when(client.readDesignSafely("all", "context2Test")).thenReturn(Optional.of(
                new DesignDocument("all", Collections.singleton(new View("data", "function(doc){emit(null);}", "_count")))));
        when(client.readDesignSafely("all", "context3Test")).thenReturn(Optional.of(
                new DesignDocument("all", Collections.singleton(new View("data", "function(doc){emit(null);}", "_count")))));
        new CouchDBSchemaProcessor(client, properties, context, null);
        verify(client, times(1).description("Database must be deleted when DROP")).deleteDatabase("test");
        verify(client, times(1).description("Database must be created when DROP")).createDatabase("test");
        verify(client, times(1).description("Database must be deleted when DROP")).deleteDatabase("context1Test");
        verify(client, times(1).description("Database must be created when DROP")).createDatabase("context1Test");
        verify(client, times(1).description("Database must be deleted when DROP")).deleteDatabase("context2Test");
        verify(client, times(1).description("Database must be created when DROP")).createDatabase("context2Test");
        verify(client, times(1).description("Database must be deleted when DROP")).deleteDatabase("context3Test");
        verify(client, times(1).description("Database must be created when DROP")).createDatabase("context3Test");
    }
}