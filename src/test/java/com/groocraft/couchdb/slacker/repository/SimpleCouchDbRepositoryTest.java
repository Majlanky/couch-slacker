/*
 * Copyright 2022 the original author or authors.
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
import com.groocraft.couchdb.slacker.exception.CouchDbException;
import com.groocraft.couchdb.slacker.exception.CouchDbRuntimeException;
import com.groocraft.couchdb.slacker.test.integration.TestDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SimpleCouchDbRepositoryTest {

    @Mock
    private CouchDbClient client;

    private SimpleCouchDbRepository<TestDocument> repository;

    @BeforeEach
    void setUp() {
        repository = new SimpleCouchDbRepository<>(client, TestDocument.class);
    }

    @Test
    void testSave() throws IOException {
        TestDocument clientProcessed = new TestDocument("something");
        when(client.save(any())).thenReturn(clientProcessed).thenThrow(new IOException("error"));
        assertEquals(clientProcessed, repository.save(new TestDocument()), "Repository should not alternate returned objects");
        verify(client, only().description("Save on client must be called for repository save")).save(any());
        TestDocument document = new TestDocument();
        CouchDbRuntimeException ex = assertThrows(CouchDbRuntimeException.class, () -> repository.save(document), "All exceptions thrown by client must be reported");
        assertEquals("error", ex.getCause().getMessage(), "Repository must pass original cause of exceptional state");
    }

    @Test
    void testSaveAll() throws IOException {
        TestDocument clientProcessed = new TestDocument("something");
        when(client.saveAll(any(), any())).thenReturn(Arrays.asList(clientProcessed, clientProcessed, clientProcessed)).thenThrow(new IOException("error"));
        Iterable<TestDocument> saved = repository.saveAll(Arrays.asList(new TestDocument(), new TestDocument(), new TestDocument()));
        StreamSupport.stream(saved.spliterator(), false).forEach(s -> assertEquals(clientProcessed, s, "Repository should not alternate returned objects"));
        List<TestDocument> documents = Collections.singletonList(new TestDocument());
        CouchDbRuntimeException ex = assertThrows(CouchDbRuntimeException.class, () -> repository.saveAll(documents),
                "All exceptions thrown by client must be reported");
        assertEquals("error", ex.getCause().getMessage(), "Repository must pass original cause of exceptional state");
    }

    @Test
    void testFindById() throws IOException {
        TestDocument clientProcessed = new TestDocument("something");
        when(client.read("unique", TestDocument.class)).thenReturn(clientProcessed).thenThrow(new IOException("error")).thenThrow(new CouchDbException(404,
                "GET", "uri", "reason"));
        Optional<TestDocument> found = repository.findById("unique");
        assertTrue(found.isPresent(), "If client find data, it must be returned");
        assertEquals(clientProcessed, found.get(), "Repository should not alternate returned objects");
        verify(client, only().description("Repository must do real search for the id")).read("unique", TestDocument.class);
        CouchDbRuntimeException ex = assertThrows(CouchDbRuntimeException.class, () -> repository.findById("unique"), "All exceptions thrown by client must be reported");
        assertEquals("error", ex.getCause().getMessage(), "Repository must pass original cause of exceptional state");
        assertFalse(assertDoesNotThrow(() -> repository.findById("nonExist")).isPresent(), "Optional must be empty when 404 is returned");
    }

    @Test
    void testFindAllById() throws IOException {
        TestDocument clientProcessed = new TestDocument("something");
        when(client.readAll(any(), eq(TestDocument.class))).thenReturn(Arrays.asList(clientProcessed, clientProcessed, clientProcessed)).thenThrow(new IOException("error"));
        Iterable<TestDocument> result = repository.findAllById(Arrays.asList("1", "2", "3"));
        assertEquals(3, StreamSupport.stream(result.spliterator(), false).count(), "The same count as requested must be returned");
        List<String> documents = Collections.singletonList("4");
        CouchDbRuntimeException ex = assertThrows(CouchDbRuntimeException.class, () -> repository.findAllById(documents),
                "All exceptions thrown by client must be reported");
        assertEquals("error", ex.getCause().getMessage(), "Repository must pass original cause of exceptional state");
    }

    @Test
    void testFindAll() throws IOException {
        when(client.readAll(TestDocument.class)).thenReturn(Arrays.asList("1", "2", "3"));
        when(client.readAll(any(), eq(TestDocument.class))).thenReturn(Arrays.asList(new TestDocument(), new TestDocument(), new TestDocument())).thenThrow(new IOException("error"));
        Iterable<TestDocument> result = repository.findAll();
        assertEquals(3, StreamSupport.stream(result.spliterator(), false).count(), "Repository should not alternate result from client");
        CouchDbRuntimeException ex = assertThrows(CouchDbRuntimeException.class, () -> repository.findAll(), "All exceptions thrown by client must be reported");
        assertEquals("error", ex.getCause().getMessage(), "Repository must pass original cause of exceptional state");

    }

    @Test
    void testExistsById() throws IOException {
        TestDocument clientProcessed = new TestDocument("something");
        when(client.read("unique", TestDocument.class)).thenReturn(clientProcessed).thenThrow(new IOException("error"));
        assertTrue(repository.existsById("unique"), "If client find data, the existence must be reported");
        verify(client, only().description("Repository must do real search for the id")).read("unique", TestDocument.class);
        CouchDbRuntimeException ex = assertThrows(CouchDbRuntimeException.class, () -> repository.existsById("unique"), "All exceptions thrown by client must be reported");
        assertEquals("error", ex.getCause().getMessage(), "Repository must pass original cause of exceptional state");
    }

    @Test
    void testDelete() throws IOException {
        TestDocument toBeDeleted = new TestDocument("something");
        when(client.delete(any())).thenReturn(toBeDeleted).thenThrow(new IOException("error"));
        repository.delete(toBeDeleted);
        verify(client, only().description("Delete must be called with non-altered object")).delete(toBeDeleted);
        CouchDbRuntimeException ex = assertThrows(CouchDbRuntimeException.class, () -> repository.delete(toBeDeleted), "All exceptions thrown by client must be reported");
        assertEquals("error", ex.getCause().getMessage(), "Repository must pass original cause of exceptional state");
    }

    @Test
    void testDeleteAllGiven() throws IOException {
        when(client.deleteAll(any(), eq(TestDocument.class))).thenReturn(Arrays.asList(new TestDocument(), new TestDocument(), new TestDocument())).thenThrow(new IOException("error"));
        repository.deleteAll(Arrays.asList(new TestDocument("1"), new TestDocument("2"), new TestDocument("3")));

        List<TestDocument> documents = Collections.singletonList(new TestDocument());
        CouchDbRuntimeException ex = assertThrows(CouchDbRuntimeException.class, () -> repository.deleteAll(documents), "All exceptions thrown by client must be " +
                "reported");
        assertEquals("error", ex.getCause().getMessage(), "Repository must pass original cause of exceptional state");
    }

    @Test
    void testDeleteAll() throws IOException {
        when(client.deleteAll(TestDocument.class)).thenReturn(Collections.singletonList(new TestDocument())).thenThrow(new IOException("error"));
        repository.deleteAll();
        verify(client, only().description("Delete all must be implemented as delete all call on client")).deleteAll(TestDocument.class);
        CouchDbRuntimeException ex = assertThrows(CouchDbRuntimeException.class, () -> repository.deleteAll(), "Every exception thrown by lower layers must be reported");
        assertEquals("error", ex.getCause().getMessage(), "Repository must pass original cause of exceptional state");
    }

    @Test
    void testDeleteById() throws IOException {
        when(client.deleteById(any(), any())).thenReturn(new TestDocument()).thenThrow(new IOException("error"));
        repository.deleteById("unique");
        verify(client).deleteById("unique", TestDocument.class);
        CouchDbRuntimeException ex = assertThrows(CouchDbRuntimeException.class, () -> repository.deleteById("unique"), "All exceptions thrown by client must be reported");
        assertEquals("error", ex.getCause().getMessage(), "Repository must pass original cause of exceptional state");
    }

    @Test
    void testDeleteAllById() throws IOException {
        List<String> ids = Arrays.asList("id1", "id2", "id3");
        TestDocument document = new TestDocument();
        when(client.readAll(anyList(), any(Class.class)))
                .thenReturn(Collections.singletonList(document))
                .thenThrow(new IOException("error"));
        when(client.deleteAll(anyList(), any(Class.class))).thenAnswer(i -> i.getArgument(0));
        repository.deleteAllById(ids);
        verify(client).readAll(ids, TestDocument.class);
        verify(client).deleteAll(Collections.singletonList(document), TestDocument.class);
        CouchDbRuntimeException ex = assertThrows(CouchDbRuntimeException.class, () -> repository.deleteAllById(ids),
                "All exceptions thrown by client must be reported");
        assertEquals("error", ex.getCause().getMessage(), "Repository must pass original cause of exceptional state");
    }

    @Test
    void testCount() throws IOException {
        when(client.countAll(TestDocument.class)).thenReturn(3L).thenThrow(new IOException("error"));
        assertEquals(3L, repository.count());
        CouchDbRuntimeException ex = assertThrows(CouchDbRuntimeException.class, () -> repository.count(), "All exceptions thrown by client must be reported");
        assertEquals("error", ex.getCause().getMessage(), "Repository must pass original cause of exceptional state");
    }

}