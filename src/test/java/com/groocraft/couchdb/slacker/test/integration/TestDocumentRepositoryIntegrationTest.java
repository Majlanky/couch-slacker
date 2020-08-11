package com.groocraft.couchdb.slacker.test.integration;

import com.groocraft.couchdb.slacker.CouchSlackerConfiguration;
import com.groocraft.couchdb.slacker.TestDocument;
import com.groocraft.couchdb.slacker.annotation.EnableCouchDbRepositories;
import com.groocraft.couchdb.slacker.exception.CouchDbException;
import com.groocraft.couchdb.slacker.exception.CouchDbRuntimeException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.repository.query.parser.PartTree;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CouchSlackerConfiguration.class, TestDocumentRepository.class}, initializers = ConfigFileApplicationContextInitializer.class)
@ActiveProfiles("test")
@EnableCouchDbRepositories
//TODO Parametrize bulk operation test to some huge numbers
public class TestDocumentRepositoryIntegrationTest {

    @Autowired
    TestDocumentRepository repository;

    @BeforeAll
    public void setUp(){
        repository.deleteAll();
    }

    @Test
    public void testSaveNewAndRead(){
        System.out.println("testSaveNewAndRead " + Thread.currentThread().getName());
        String randomValue = UUID.randomUUID().toString();
        TestDocument saved = repository.save(new TestDocument(randomValue));
        assertNotNull(saved.getId(), "Id must be present after save");
        assertNotNull(saved.getRevision(), "Revision must be resent after save");
        Optional<TestDocument> read = repository.findById(saved.getId());
        assertTrue(read.isPresent(), "It must be possible to read document created few lines above");
        assertEquals(randomValue, read.get().getValue(), "Values must match if the same document read");
    }

    @Test
    public void testSaveNewAndDeleteById(){
        System.out.println("testSaveNewAndDeleteById " + Thread.currentThread().getName());
        String randomValue = UUID.randomUUID().toString();
        TestDocument saved = repository.save(new TestDocument(randomValue));
        assertNotNull(saved.getId(), "Id must be present after save");
        assertNotNull(saved.getRevision(), "Revision must be present after save");
        repository.deleteById(saved.getId());
        assertFalse(repository.existsById(saved.getId()), "Database must be empty because created document was deleted");
    }

    @Test
    public void testSaveNewAndUpdateAndRead(){
        System.out.println("testSaveNewAndUpdateAndRead " + Thread.currentThread().getName());
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
    public void testSavingAll(){
        System.out.println("testSavingAll " + Thread.currentThread().getName());
        List<TestDocument> all = new LinkedList<>();
        IntStream.range(1, 21).forEach(i -> all.add(new TestDocument( "value" + i, "value2" + i)));
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
                StreamSupport.stream(repository.findAllById(StreamSupport.stream(saved.spliterator(), false).map(d -> d.getId()).collect(Collectors.toList())).spliterator(), false).count(),
                "One or more of passed entities was not truly saved");
    }

    @Test
    public void testExistsByIdNonExisting(){
        System.out.println("testExistsByIdNonExisting " + Thread.currentThread().getName());
        assertFalse(repository.existsById("NonExisting"), "False must be returned when id does not exists");
    }

    @Test
    public void testFindAllById(){
        System.out.println("testFindAllById " + Thread.currentThread().getName());
        List<TestDocument> all = new LinkedList<>();
        IntStream.range(1, 21).forEach(i -> all.add(new TestDocument( "value" + i, "value2" + i)));
        Iterable<TestDocument> saved = repository.saveAll(all);
        List<TestDocument> found = new LinkedList<>();
        repository.findAllById(StreamSupport.stream(saved.spliterator(), false).map(d -> d.getId()).collect(Collectors.toList())).forEach(found::add);

        saved.forEach(s -> {
            assertTrue(found.stream().anyMatch(s::equals), "One of saved not found");
        });
    }

    @Test
    public void testFindAllByIdWithNonExisting(){
        System.out.println("testFindAllByIdWithNonExisting " + Thread.currentThread().getName());
        List<String> all = List.of("nonExisting");
        Iterable<TestDocument> found =
                assertDoesNotThrow(() -> repository.findAllById(StreamSupport.stream(all.spliterator(), false).collect(Collectors.toList()))
                        , "Exception must not be thrown when one of wanted ids does not exist");
        assertEquals(0, StreamSupport.stream(found.spliterator(), false).count(), "For non existing id, no entity should be returned");
    }

    @Test
    public void testReadingNonExisting(){
        System.out.println("testReadingNonExisting " + Thread.currentThread().getName());
        Optional<TestDocument> read = assertDoesNotThrow(() -> repository.findById("nonExisting"), "Exception must not be thrown cause of non existing id");
        assertFalse(read.isPresent(), "No entity can be present if the id not exists in the DB");
    }

    @Test
    public void testDeletingByIdNonExisting(){
        System.out.println("testDeletingByIdNonExisting " + Thread.currentThread().getName());
        assertThrows(CouchDbRuntimeException.class, () -> repository.deleteById("nonExisting"), "Exception must be thrown when id does not exist");
    }

    @Test
    public void testDeletingNonExisting(){
        System.out.println("testDeletingNonExisting " + Thread.currentThread().getName());
        assertThrows(CouchDbRuntimeException.class, () -> repository.delete(new TestDocument("nonExisting", "1", "empty")),
                "Exception must be thrown when id of given entity does not exist");
    }

    @Test
    public void testCreateAllAndCountAndDeleteAll(){
        System.out.println("testCreateAllAndCountAndDeleteAll " + Thread.currentThread().getName());
        repository.deleteAll();
        System.out.println("start");
        List<TestDocument> all = new LinkedList<>();
        IntStream.range(1, 21).forEach(i -> all.add(new TestDocument( "value" + i, "value2" + i)));
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
        System.out.println("hrh");
    }

    @Test
    public void testSpecialQueries(){
        System.out.println("testSpecialQueries " + Thread.currentThread().getName());
        PartTree tree = new PartTree("findByValueIsNotLikeAndValueIsBetween", TestDocument.class);
        for(PartTree.OrPart orPart : tree){
            for(Part part : orPart){
                System.out.println(part);
            }
        }
    }


}
