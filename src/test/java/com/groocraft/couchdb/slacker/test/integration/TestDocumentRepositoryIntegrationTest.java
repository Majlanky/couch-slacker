package com.groocraft.couchdb.slacker.test.integration;

import com.groocraft.couchdb.slacker.CouchDbClient;
import com.groocraft.couchdb.slacker.TestDocument;
import com.groocraft.couchdb.slacker.annotation.EnableCouchDbRepositories;
import com.groocraft.couchdb.slacker.configuration.CouchSlackerConfiguration;
import com.groocraft.couchdb.slacker.exception.CouchDbRuntimeException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CouchSlackerConfiguration.class, TestDocumentRepository.class},
        initializers = ConfigFileApplicationContextInitializer.class)
@ActiveProfiles("test")
@EnableCouchDbRepositories
public class TestDocumentRepositoryIntegrationTest {

    @Autowired
    CouchDbClient client;

    @Autowired
    TestDocumentRepository repository;

    @BeforeAll
    public void setUp(){
        try {
            client.createDatabase("_users");
            client.createDatabase("_replicator");
            client.createDatabase("test");
        } catch (IOException ex){
            fail("Unable to initialize database", ex);
        }
    }

    @BeforeEach
    public void clear() {
        repository.deleteAll();
    }

    @Test
    public void testSaveNewAndRead() {
        String randomValue = UUID.randomUUID().toString();
        TestDocument saved = repository.save(new TestDocument(randomValue));
        assertNotNull(saved.getId(), "Id must be present after save");
        assertNotNull(saved.getRevision(), "Revision must be resent after save");
        Optional<TestDocument> read = repository.findById(saved.getId());
        assertTrue(read.isPresent(), "It must be possible to read document created few lines above");
        assertEquals(randomValue, read.get().getValue(), "Values must match if the same document read");
    }

    @Test
    public void testSaveAndUpdate() {
        String randomValue = UUID.randomUUID().toString();
        TestDocument saved = repository.save(new TestDocument(randomValue));
        assertNotNull(saved.getId(), "Id must be present after save");
        assertNotNull(saved.getRevision(), "Revision must be resent after save");
        String newRandomValue = UUID.randomUUID().toString();
        saved.setValue(newRandomValue);
        saved = repository.save(saved);
        Optional<TestDocument> read = repository.findById(saved.getId());
        assertTrue(read.isPresent(), "It must be possible to read document created few lines above");
        assertEquals(newRandomValue, read.get().getValue(), "Values must match if the same document read");
    }

    @Test
    public void testSaveNewAndDeleteById() {
        String randomValue = UUID.randomUUID().toString();
        TestDocument saved = repository.save(new TestDocument(randomValue));
        assertNotNull(saved.getId(), "Id must be present after save");
        assertNotNull(saved.getRevision(), "Revision must be present after save");
        repository.deleteById(saved.getId());
        assertFalse(repository.existsById(saved.getId()), "Database must be empty because created document was deleted");
    }

    @Test
    public void testSaveNewAndUpdateAndRead() {
        String randomValue = UUID.randomUUID().toString();
        TestDocument saved = repository.save(new TestDocument(randomValue));
        assertNotNull(saved.getId(), "Id must be present after save");
        assertNotNull(saved.getRevision(), "Revision must be resent after save");
        String newValue = UUID.randomUUID().toString();
        TestDocument altered = new TestDocument(saved.getId(), saved.getRevision(), newValue, saved.getValue2());
        repository.save(altered);
        Optional<TestDocument> read = repository.findById(saved.getId());
        assertTrue(read.isPresent(), "It must be possible to read document created few lines above");
        assertEquals(newValue, read.get().getValue(), "Values must match if the same document read");
    }

    @Test
    public void testSavingAll() {
        List<TestDocument> all = new LinkedList<>();
        IntStream.range(1, 21).forEach(i -> all.add(new TestDocument("value" + i, "value2" + i)));
        Iterable<TestDocument> saved = repository.saveAll(all);
        assertEquals(20, StreamSupport.stream(saved.spliterator(), false).count(), "All given entities must be saved");
        AtomicInteger i = new AtomicInteger(1);
        StreamSupport.stream(saved.spliterator(), false).forEach(s -> {
            assertNotNull(s.getId(), "All saved entities must contain id");
            assertNotNull(s.getRevision(), "All saved entities must contain revision");
            assertEquals("value" + i.get(), s.getValue(), "Value of entity is not matching. Order or data of entities is messed up");
            assertEquals("value2" + i.getAndIncrement(), s.getValue2(), "Value of entity is not matching. Order or data of entities is messed up");
        });
        assertEquals(20,
                StreamSupport.stream(repository.findAllById(StreamSupport.stream(saved.spliterator(), false).map(TestDocument::getId).collect(Collectors.toList())).spliterator(), false).count(),
                "One or more of passed entities was not truly saved");
    }

    @Test
    public void testExistsByIdNonExisting() {
        assertFalse(repository.existsById("NonExisting"), "False must be returned when id does not exists");
    }

    @Test
    public void testFindAllById() {
        List<TestDocument> all = new LinkedList<>();
        IntStream.range(1, 21).forEach(i -> all.add(new TestDocument("value" + i, "value2" + i)));
        Iterable<TestDocument> saved = repository.saveAll(all);
        List<TestDocument> found = new LinkedList<>();
        repository.findAllById(StreamSupport.stream(saved.spliterator(), false).map(TestDocument::getId).collect(Collectors.toList())).forEach(found::add);

        saved.forEach(s -> assertTrue(found.stream().anyMatch(s::equals), "One of saved not found"));
    }

    @Test
    public void testFindAllByIdWithNonExisting() {
        List<String> all = List.of("nonExisting");
        Iterable<TestDocument> found =
                assertDoesNotThrow(() -> repository.findAllById(all), "Exception must not be thrown when one of wanted ids does not exist");
        assertEquals(0, StreamSupport.stream(found.spliterator(), false).count(), "For non existing id, no entity should be returned");
    }

    @Test
    public void testReadingNonExisting() {
        Optional<TestDocument> read = assertDoesNotThrow(() -> repository.findById("nonExisting"), "Exception must not be thrown cause of non existing id");
        assertFalse(read.isPresent(), "No entity can be present if the id not exists in the DB");
    }

    @Test
    public void testDeletingByIdNonExisting() {
        assertThrows(CouchDbRuntimeException.class, () -> repository.deleteById("nonExisting"), "Exception must be thrown when id does not exist");
    }

    @Test
    public void testDeletingNonExisting() {
        assertThrows(CouchDbRuntimeException.class, () -> repository.delete(new TestDocument("nonExisting", "1", "empty")),
                "Exception must be thrown when id of given entity does not exist");
    }

    @Test
    public void testCreateAllAndCountAndDeleteAll() {
        repository.deleteAll();
        List<TestDocument> all = new LinkedList<>();
        IntStream.range(1, 21).forEach(i -> all.add(new TestDocument("value" + i, "value2" + i)));
        Iterable<TestDocument> saved = repository.saveAll(all);
        assertEquals(20, StreamSupport.stream(saved.spliterator(), false).count(), "Must be saved all given entities");
        AtomicInteger i = new AtomicInteger(1);
        StreamSupport.stream(saved.spliterator(), false).forEach(s -> {
            assertNotNull(s.getId(), "All saved entities must contain id");
            assertNotNull(s.getRevision(), "All saved entities must contain revision");
            assertEquals("value" + i.get(), s.getValue(), "Value of entity is not matching. Order or data of entities is messed up");
            assertEquals("value2" + i.getAndIncrement(), s.getValue2(), "Value of entity is not matching. Order or data of entities is messed up");
            assertTrue(repository.existsById(s.getId()), "One of passed entities was not truly saved");
        });
        assertEquals(20, repository.count(), "There must be only 20 entities in BD now");
        repository.deleteAll();
        assertEquals(0, repository.count(), "DB must be empty now");
    }

    @Test
    public void testQueryBased() {
        String randomValue = UUID.randomUUID().toString();
        TestDocument saved = repository.save(new TestDocument(null, null, randomValue));
        List<TestDocument> read = repository.queryBased(randomValue);
        assertNotNull(read, "Returned list must not be null");
        assertEquals(1, read.size(), "Returned list must contain only the document we have created");
        assertEquals(randomValue, read.get(0).getValue(), "We get different document we wanted");
        assertEquals(saved.getId(), read.get(0).getId(), "We have to get the original document");
    }

    @Test
    public void testQueryBasedWithNamed() {
        String randomValue = UUID.randomUUID().toString();
        TestDocument saved = repository.save(new TestDocument(null, null, randomValue));
        List<TestDocument> read = repository.queryBasedWithNamed(randomValue);
        assertNotNull(read, "Returned list must not be null");
        assertEquals(1, read.size(), "Returned list must contain only the document we have created");
        assertEquals(randomValue, read.get(0).getValue(), "We get different document we wanted");
        assertEquals(saved.getId(), read.get(0).getId(), "We have to get the original document");
    }

    @Test
    public void testFindByValueAndValue2() {
        String randomValue = UUID.randomUUID().toString();
        String randomValue2 = UUID.randomUUID().toString();
        repository.save(new TestDocument(null, null, randomValue2, "theSame"));
        TestDocument saved = repository.save(new TestDocument(null, null, randomValue, "theSame"));
        List<TestDocument> read = repository.findByValueAndValue2(randomValue, "theSame");
        assertEquals(1, read.size(), "Returned list must contain only the document we have created");
        assertEquals(randomValue, read.get(0).getValue(), "We get different document we wanted");
        assertEquals(saved.getId(), read.get(0).getId(), "We have to get the original document");
    }

    @Test
    public void testExistsByValueAndValue2() {
        String randomValue = UUID.randomUUID().toString();
        String randomValue2 = UUID.randomUUID().toString();
        repository.save(new TestDocument(null, null, randomValue2, "theSame"));
        TestDocument saved = repository.save(new TestDocument(null, null, randomValue, "theSame"));
        boolean exists = repository.existsByValueAndValue2(randomValue, "theSame");
        assertTrue(exists, "Document must exist because we have created it");
    }

    @Test
    public void testCountByValueAndValue2() {
        String randomValue = UUID.randomUUID().toString();
        String randomValue2 = UUID.randomUUID().toString();
        repository.save(new TestDocument(null, null, randomValue2, "theSame"));
        TestDocument saved = repository.save(new TestDocument(null, null, randomValue, "theSame"));
        int count = repository.countByValueAndValue2(randomValue, "theSame");
        assertEquals(1, count, "Exactly one document exists with the wanted value");
    }

    @Test
    public void testDeleteByValueAndValue2() {
        String randomValue = UUID.randomUUID().toString();
        String randomValue2 = UUID.randomUUID().toString();
        repository.save(new TestDocument(null, null, randomValue2, "theSame"));
        TestDocument saved = repository.save(new TestDocument(null, null, randomValue, "theSame"));
        repository.deleteByValueAndValue2(randomValue, "theSame");
        Optional<TestDocument> nonExisting = repository.findById(saved.getId());
        assertFalse(nonExisting.isPresent(), "Document should be deleted by now");
    }

    @Test
    public void testFindByValueIsNot() {
        String randomValue = UUID.randomUUID().toString();
        String randomValue2 = UUID.randomUUID().toString();
        repository.save(new TestDocument(null, null, randomValue2, "theSame"));
        TestDocument saved = repository.save(new TestDocument(null, null, randomValue, "theSame"));
        List<TestDocument> read = repository.findByValueIsNot(randomValue2);
        assertEquals(1, read.size(), "Returned list must contain only the document we have created");
        assertEquals(randomValue, read.get(0).getValue(), "We get different document we wanted");
        assertEquals(saved.getId(), read.get(0).getId(), "We have to get the original document");
    }

    @Test
    public void testFindByValue3GreaterThan() {
        IntStream.range(1, 11).forEach(i -> repository.save(new TestDocument(i)));
        List<TestDocument> read = repository.findByValue3GreaterThan(4);
        assertEquals(6, read.size(), "Returned list must contain documents with value3 from 5 from 10");
        for (TestDocument d : read) {
            assertTrue(d.getValue3() > 4, "We requested only document with value3 greater 4");
        }
    }

    @Test
    public void testFindByValue3GreaterThanEquals() {
        IntStream.range(1, 11).forEach(i -> repository.save(new TestDocument(i)));
        List<TestDocument> read = repository.findByValue3GreaterThanEqual(4);
        assertEquals(7, read.size(), "Returned list must contain documents with value3 from  4 from 10");
        for (TestDocument d : read) {
            assertTrue(d.getValue3() >= 4, "We requested only document with value3 greater or equal to 4");
        }
    }

    @Test
    public void testFindByValue3LessThan() {
        IntStream.range(1, 11).forEach(i -> repository.save(new TestDocument(i)));
        List<TestDocument> read = repository.findByValue3LessThan(7);
        assertEquals(6, read.size(), "Returned list must contain documents with value3 from  1 from 6");
        for (TestDocument d : read) {
            assertTrue(d.getValue3() < 7, "We requested only document with value3 lesser than 7");
        }
    }

    @Test
    public void testFindByValue3LessThanEquals() {
        IntStream.range(1, 11).forEach(i -> repository.save(new TestDocument(i)));
        List<TestDocument> read = repository.findByValue3LessThanEqual(7);
        assertEquals(7, read.size(), "Returned list must contain documents with value3 from  1 from 7");
        for (TestDocument d : read) {
            assertTrue(d.getValue3() <= 7, "We requested only document with value3 lesser than or equal to 7");
        }
    }

    @Test
    public void testFindByValueRegex() {
        IntStream.range(1, 21).forEach(i -> repository.save(new TestDocument("value" + i)));
        List<TestDocument> read = repository.findByValueRegex("^value1");
        assertEquals(11, read.size(), "Returned list must contain only document with value starting with value1");
        for (TestDocument d : read) {
            assertTrue(d.getValue().startsWith("value1"), "We requested only document with value starting with value1");
        }
    }

    @Test
    public void testFindByValue2Null() {
        IntStream.range(10, 20).forEach(i -> repository.save(new TestDocument("value" + i)));
        IntStream.range(20, 30).forEach(i -> repository.save(new TestDocument("value" + i, "value2" + i)));
        List<TestDocument> read = repository.findByValue2Null();
        assertEquals(10, read.size(), "Returned list must contain only document with value starting with value1");
        for (TestDocument d : read) {
            assertTrue(d.getValue().startsWith("value1"), "We requested only document with value2 null");
        }
    }

    @Test
    public void testFindByValue2NotNull() {
        IntStream.range(10, 20).forEach(i -> repository.save(new TestDocument("value" + i)));
        IntStream.range(20, 30).forEach(i -> repository.save(new TestDocument("value" + i, "value2" + i)));
        List<TestDocument> read = repository.findByValue2NotNull();
        assertEquals(10, read.size(), "Returned list must contain only document with value starting with value1");
        for (TestDocument d : read) {
            assertTrue(d.getValue().startsWith("value2"), "We requested only document with value2 null");
        }
    }

    @Test
    public void testFindByValue3Before() {
        IntStream.range(1, 11).forEach(i -> repository.save(new TestDocument(i)));
        List<TestDocument> read = repository.findByValue3Before(7);
        assertEquals(6, read.size(), "Returned list must contain documents with value3 from  1 from 6");
        for (TestDocument d : read) {
            assertTrue(d.getValue3() < 7, "We requested only document with value3 lesser than 7");
        }
    }

    @Test
    public void testFindByValue3After() {
        IntStream.range(1, 11).forEach(i -> repository.save(new TestDocument(i)));
        List<TestDocument> read = repository.findByValue3After(4);
        assertEquals(6, read.size(), "Returned list must contain documents with value3 from 5 from 10");
        for (TestDocument d : read) {
            assertTrue(d.getValue3() > 4, "We requested only document with value3 greater 4");
        }
    }

    @Test
    public void testFindByValueStartingWith() {
        IntStream.range(1, 21).forEach(i -> repository.save(new TestDocument("value" + i)));
        List<TestDocument> read = repository.findByValueStartingWith("value1");
        assertEquals(11, read.size(), "Returned list must contain only document with value starting with value1");
        for (TestDocument d : read) {
            assertTrue(d.getValue().startsWith("value1"), "We requested only document with value starting with value1");
        }
    }

    @Test
    public void testFindByValueEndingWith() {
        IntStream.range(1, 21).forEach(i -> repository.save(new TestDocument("value" + i)));
        List<TestDocument> read = repository.findByValueEndingWith("2");
        assertEquals(2, read.size(), "Returned list must contain only document with value ending with 2");
        for (TestDocument d : read) {
            assertTrue(d.getValue().endsWith("2"), "We requested only document with value ending with 2");
        }
    }

    @Test
    public void testFindByValue4Empty() {
        IntStream.range(1, 11).forEach(i -> repository.save(new TestDocument(List.of("value" + i))));
        IntStream.range(1, 6).forEach(i -> repository.save(new TestDocument(List.of())));
        List<TestDocument> read = repository.findByValue4Empty();
        assertEquals(5, read.size(), "Returned list must contain only document with empty value4");
        for (TestDocument d : read) {
            assertTrue(d.getValue4().isEmpty(), "We requested only documents with empty value4");
        }
    }

    @Test
    public void testFindByValue4NotEmpty() {
        IntStream.range(1, 11).forEach(i -> repository.save(new TestDocument(List.of("value" + i))));
        IntStream.range(1, 6).forEach(i -> repository.save(new TestDocument(List.of())));
        List<TestDocument> read = repository.findByValue4NotEmpty();
        assertEquals(10, read.size(), "Returned list must contain only document with empty value4");
        for (TestDocument d : read) {
            assertFalse(d.getValue4().isEmpty(), "We requested only documents with empty value4");
        }
    }

    @Test
    public void testFindByValueContaining() {
        IntStream.range(1, 11).forEach(i -> repository.save(new TestDocument("value" + i)));
        IntStream.range(1, 6).forEach(i -> repository.save(new TestDocument()));
        List<TestDocument> read = repository.findByValueContaining("alue");
        assertEquals(10, read.size(), "Returned list must contain only document with value containing \"alue\"");
        for (TestDocument d : read) {
            assertTrue(d.getValue().contains("alue"), "We requested only document with value containing \"alue\"");
        }
    }

    @Test
    public void testFindByValueNotContaining() {
        IntStream.range(1, 11).forEach(i -> repository.save(new TestDocument("value" + i)));
        IntStream.range(1, 6).forEach(i -> repository.save(new TestDocument("test" + i)));
        List<TestDocument> read = repository.findByValueNotContaining("alue");
        assertEquals(5, read.size(), "Returned list must contain only document with value not containing \"alue\"");
        for (TestDocument d : read) {
            assertFalse(d.getValue().contains("alue"), "We requested only document with value not containing \"alue\"");
        }
    }

    @Test
    public void testFindByValueLike() {
        IntStream.range(1, 21).forEach(i -> repository.save(new TestDocument("value" + i)));
        List<TestDocument> read = repository.findByValueLike("value1");
        assertEquals(11, read.size(), "Returned list must contain only document with value starting with value1");
        for (TestDocument d : read) {
            assertTrue(d.getValue().startsWith("value1"), "We requested only document with value starting with value1");
        }
    }

    @Test
    public void testFindByValueNotLike() {
        IntStream.range(1, 11).forEach(i -> repository.save(new TestDocument("value" + i)));
        IntStream.range(1, 6).forEach(i -> repository.save(new TestDocument("test" + i)));
        List<TestDocument> read = repository.findByValueNotLike("alue");
        assertEquals(5, read.size(), "Returned list must contain only document with value not containing \"alue\"");
        for (TestDocument d : read) {
            assertFalse(d.getValue().contains("alue"), "We requested only document with value not containing \"alue\"");
        }
    }

    @Test
    public void testFindByValue5True() {
        IntStream.range(1, 11).forEach(i -> repository.save(new TestDocument(true)));
        IntStream.range(1, 6).forEach(i -> repository.save(new TestDocument(false)));
        List<TestDocument> read = repository.findByValue5True();
        assertEquals(10, read.size(), "Returned list must contain only document with value5 set to true");
        for (TestDocument d : read) {
            assertTrue(d.isValue5(), "We requested only document with value5 set to true");
        }
    }

    @Test
    public void testFindByValue5False() {
        IntStream.range(1, 11).forEach(i -> repository.save(new TestDocument(true)));
        IntStream.range(1, 6).forEach(i -> repository.save(new TestDocument(false)));
        List<TestDocument> read = repository.findByValue5False();
        assertEquals(5, read.size(), "Returned list must contain only document with value5 set to false");
        for (TestDocument d : read) {
            assertFalse(d.isValue5(), "We requested only document with value5 set to false");
        }
    }

}
