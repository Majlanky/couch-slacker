package com.groocraft.couchdb.slacker.repository;

import com.groocraft.couchdb.slacker.CouchDbClient;
import com.groocraft.couchdb.slacker.TestDocument;
import com.groocraft.couchdb.slacker.exception.CouchDBBulkOperationException;
import com.groocraft.couchdb.slacker.exception.CouchDbException;
import com.groocraft.couchdb.slacker.exception.CouchDbRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SimpleCouchDbRepositoryTest {

    @Mock
    private CouchDbClient client;

    private SimpleCouchDbRepository<TestDocument> repository;

    @BeforeEach
    public void setUp() {
        repository = new SimpleCouchDbRepository(client, TestDocument.class);
    }

    @Test
    public void testSave() throws IOException {
        TestDocument clientProcessed = new TestDocument("something");
        when(client.save(any())).thenReturn(clientProcessed).thenThrow(new IOException("error"));
        assertEquals(clientProcessed, repository.save(new TestDocument()), "Repository should not alternate returned objects");
        verify(client, only().description("Save on client must be called for repository save")).save(any());
        CouchDbRuntimeException ex = assertThrows(CouchDbRuntimeException.class, () -> {
            repository.save(new TestDocument());
        }, "All exceptions thrown by client must be reported");
        assertEquals("error", ex.getCause().getMessage(), "Repository must pass original cause of exceptional state");
    }

    @Test
    public void testSaveAll() throws IOException {
        TestDocument clientProcessed = new TestDocument("something");
        when(client.saveAll(any(), any())).thenReturn(List.of(clientProcessed, clientProcessed, clientProcessed)).thenThrow(new IOException("error"));
        Iterable<TestDocument> saved = repository.saveAll(List.of(new TestDocument(), new TestDocument(), new TestDocument()));
        StreamSupport.stream(saved.spliterator(), false).forEach(s -> assertEquals(clientProcessed, s, "Repository should not alternate returned objects"));
        TestDocument failingDocument = new TestDocument();
        CouchDbRuntimeException ex = assertThrows(CouchDbRuntimeException.class, () -> {
            repository.saveAll(List.of(failingDocument));
        }, "All exceptions thrown by client must be reported");
        assertEquals("error", ex.getCause().getMessage(), "Repository must pass original cause of exceptional state");
    }

    @Test
    public void testFindById() throws IOException {
        TestDocument clientProcessed = new TestDocument("something");
        when(client.read("unique", TestDocument.class)).thenReturn(clientProcessed).thenThrow(new IOException("error")).thenThrow(new CouchDbException(404,
                "GET", "uri", "reason"));
        Optional<TestDocument> found = repository.findById("unique");
        assertTrue(found.isPresent(), "If client find data, it must be returned");
        assertEquals(clientProcessed, found.get(), "Repository should not alternate returned objects");
        verify(client, only().description("Repository must do real search for the id")).read("unique", TestDocument.class);
        CouchDbRuntimeException ex = assertThrows(CouchDbRuntimeException.class, () -> {
            repository.findById("unique");
        }, "All exceptions thrown by client must be reported");
        assertEquals("error", ex.getCause().getMessage(), "Repository must pass original cause of exceptional state");
        assertFalse(assertDoesNotThrow(() -> repository.findById("nonExist")).isPresent(), "Optional must be empty when 404 is returned");
    }

    @Test
    public void testFindAllById() throws IOException {
        TestDocument clientProcessed = new TestDocument("something");
        when(client.readAll(any(), eq(TestDocument.class))).thenReturn(List.of(clientProcessed, clientProcessed, clientProcessed)).thenThrow(new IOException("error"));
        Iterable<TestDocument> result = repository.findAllById(List.of("1", "2", "3"));
        assertEquals(3, StreamSupport.stream(result.spliterator(), false).count(), "The same count as requested must be returned");
        CouchDbRuntimeException ex = assertThrows(CouchDbRuntimeException.class, () -> {
            repository.findAllById(List.of("4")).forEach(t -> t.getValue());
        }, "All exceptions thrown by client must be reported");
        assertEquals("error", ex.getCause().getMessage(), "Repository must pass original cause of exceptional state");
    }

    @Test
    public void testFindAll() throws IOException {
        TestDocument clientProcessed = new TestDocument("something");
        when(client.readAll(TestDocument.class)).thenReturn(List.of("1", "2", "3")).thenReturn(List.of("4")).thenReturn(List.of("5")).thenThrow(new IOException("error"));
        when(client.read(any(), eq(TestDocument.class))).thenReturn(clientProcessed, clientProcessed, clientProcessed, null).thenThrow(new IOException("error"));
        Iterable<TestDocument> result = repository.findAll();
        //Testing the lazy way is still here
        verify(client, never().description("Actual GET should not be send till data needed")).read(any(), eq(TestDocument.class));
        result.forEach(t -> t.getValue());
        verify(client, atLeastOnce().description("Repository must do real search for every id")).read("1", TestDocument.class);
        verify(client, atLeastOnce().description("Repository must do real search for every id")).read("2", TestDocument.class);
        verify(client, atLeastOnce().description("Repository must do real search for every id")).read("3", TestDocument.class);
        //This result contains already not existing document, so iteration above it should process read exception
        assertThrows(CouchDbRuntimeException.class, () -> {
            repository.findAll().forEach(t -> t.getValue());
        }, "All exceptions thrown by client must be reported");
        CouchDbRuntimeException ex = assertThrows(CouchDbRuntimeException.class, () -> {
            repository.findAll().forEach(t -> t.getValue());
        }, "All exceptions thrown by client must be reported");
        assertEquals("error", ex.getCause().getMessage(), "Repository must pass original cause of exceptional state");
        ex = assertThrows(CouchDbRuntimeException.class, () -> {
            repository.findAll();
        }, "All exceptions thrown by client must be reported");
        assertEquals("error", ex.getCause().getMessage(), "Repository must pass original cause of exceptional state");

    }

    @Test
    public void testExistsById() throws IOException {
        TestDocument clientProcessed = new TestDocument("something");
        when(client.read("unique", TestDocument.class)).thenReturn(clientProcessed).thenThrow(new IOException("error"));
        assertTrue(repository.existsById("unique"), "If client find data, the existence must be reported");
        verify(client, only().description("Repository must do real search for the id")).read("unique", TestDocument.class);
        CouchDbRuntimeException ex = assertThrows(CouchDbRuntimeException.class, () -> {
            repository.existsById("unique");
        }, "All exceptions thrown by client must be reported");
        assertEquals("error", ex.getCause().getMessage(), "Repository must pass original cause of exceptional state");
    }

    @Test
    public void testDelete() throws IOException {
        TestDocument toBeDeleted = new TestDocument("something");
        when(client.delete(any())).thenReturn(toBeDeleted).thenThrow(new IOException("error"));
        repository.delete(toBeDeleted);
        verify(client, only().description("Delete must be called with non-altered object")).delete(toBeDeleted);
        CouchDbRuntimeException ex = assertThrows(CouchDbRuntimeException.class, () -> {
            repository.delete(toBeDeleted);
        }, "All exceptions thrown by client must be reported");
        assertEquals("error", ex.getCause().getMessage(), "Repository must pass original cause of exceptional state");
    }

    @Test
    public void testDeleteAllGiven() throws IOException {
        when(client.deleteAll(any(), eq(TestDocument.class))).thenReturn(List.of(new TestDocument(), new TestDocument(), new TestDocument())).thenThrow(new IOException("error"));
        repository.deleteAll(List.of(new TestDocument("1"), new TestDocument("2"), new TestDocument("3")));

        TestDocument failingDocument = new TestDocument();
        CouchDbRuntimeException ex = assertThrows(CouchDbRuntimeException.class, () -> {
            repository.deleteAll(List.of(failingDocument));
        }, "All exceptions thrown by client must be reported");
        assertEquals("error", ex.getCause().getMessage(), "Repository must pass original cause of exceptional state");
    }

    @Test
    public void testDeleteAll() throws IOException {
        //TODO
        /*AllDocumentRow failingRow = new AllDocumentRow("4");
        when(client.readAll(TestDocument.class)).thenReturn(List.of(new AllDocumentRow("1"), new AllDocumentRow("2"), new AllDocumentRow("3"))).thenReturn(List.of(failingRow)).thenThrow(new IOException("error"));
        when(client.deleteById(any(), any())).thenReturn(new TestDocument(), new TestDocument(), new TestDocument()).thenThrow(new IOException("error"));
        repository.deleteAll();
        verify(client, times(3).description("Every document present in DB must be deleted")).deleteById(any(), eq(TestDocument.class));
        verify(client, atLeastOnce().description("One of passed object was not processed")).deleteById("1", TestDocument.class);
        verify(client, atLeastOnce().description("One of passed object was not processed")).deleteById("2", TestDocument.class);
        verify(client, atLeastOnce().description("One of passed object was not processed")).deleteById("3", TestDocument.class);
        CouchDBBulkOperationException ex = assertThrows(CouchDBBulkOperationException.class, () -> {
            repository.deleteAll();
        }, "");
        assertEquals(1, ex.getFailed().size(), "All failed document must be reported");
        assertTrue(ex.getFailed().containsKey(failingRow), "All failed document must be reported");
        assertEquals(IOException.class, ex.getFailed().get(failingRow).getClass(), "Original exception must be reported for every save failed document");
        CouchDbRuntimeException ex2 = assertThrows(CouchDbRuntimeException.class, () -> {
            repository.deleteAll();
        }, "All exceptions thrown by client must be reported");
        assertEquals("error", ex2.getCause().getMessage(), "Repository must pass original cause of exceptional state");*/
    }

    @Test
    public void testDeleteById() throws IOException {
        when(client.deleteById(any(), any())).thenReturn(new TestDocument()).thenThrow(new IOException("error"));
        repository.deleteById("unique");
        verify(client).deleteById("unique", TestDocument.class);
        CouchDbRuntimeException ex = assertThrows(CouchDbRuntimeException.class, () -> {
            repository.deleteById("unique");
        }, "All exceptions thrown by client must be reported");
        assertEquals("error", ex.getCause().getMessage(), "Repository must pass original cause of exceptional state");
    }

    @Test
    public void testCount() throws IOException{
        when(client.readAll(TestDocument.class)).thenReturn(List.of("1", "2", "3")).thenThrow(new IOException("error"));
        assertEquals(3, repository.count());
        CouchDbRuntimeException ex = assertThrows(CouchDbRuntimeException.class, () -> {
            repository.count();
        }, "All exceptions thrown by client must be reported");
        assertEquals("error", ex.getCause().getMessage(), "Repository must pass original cause of exceptional state");
    }

}