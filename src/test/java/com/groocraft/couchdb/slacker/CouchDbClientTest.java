package com.groocraft.couchdb.slacker;

import com.groocraft.couchdb.slacker.repository.CouchDbEntityInformation;
import com.groocraft.couchdb.slacker.utils.ThrowingConsumer;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.HttpContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opentest4j.AssertionFailedError;
import org.springframework.data.domain.Sort;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouchDbClientTest {

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpHost httpHost;

    @Mock
    private HttpContext httpContext;

    private URI baseURI;
    private CouchDbClient client;

    @BeforeEach
    public void setUp() throws URISyntaxException {
        baseURI = new URI("http://localhost:5984/");
        client = new CouchDbClient(httpClient, httpHost, httpContext, baseURI);
    }

    @Test
    public void testGetEntityInformation() {
        CouchDbEntityInformation<TestDocument, String> information = client.getEntityInformation(TestDocument.class);
        assertNotNull(information, "Returned entity information must not be null");
        assertEquals(TestDocument.class, information.getJavaType(), "Returned entity information must match to entity class");
    }

    @Test
    public void testSaveNew() throws IOException {
        IOException thrown = new IOException("error");
        InputStream content = new ByteArrayInputStream("{\"id\":\"unique\",\"rev\":\"revision\",\"ok\":\"true\"}".getBytes());
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        HttpResponse response = mock(HttpResponse.class);
        HttpEntity entity = mock(HttpEntity.class);
        when(entity.getContent()).thenReturn(content);
        when(response.getEntity()).thenReturn(entity);
        when(httpClient.execute(eq(httpHost), requestCaptor.capture(), eq(httpContext))).thenReturn(response).thenThrow(thrown);

        TestDocument saved = client.save(new TestDocument("test"));

        HttpRequest request = requestCaptor.getValue();
        assertEquals(HttpPut.class, request.getClass(), "Save has to be done as PUT request");
        HttpPut put = (HttpPut) request;
        assertTrue(put.getURI().toString().startsWith("http://localhost:5984/test"), "URI must be based on base URI and database name");
        assertEquals("application/json", put.getEntity().getContentType().getValue(), "Content type must be set to json");
        assertContent("{\"value\":\"test\",\"value2\":null}", put.getEntity().getContent(), "Body of request is not properly created");
        assertEquals("unique", saved.getId(), "Saved document must be updated by new id, if generated");
        assertEquals("revision", saved.getRevision(), "Saved document must be updated by given revision");

        assertEquals(thrown, assertThrows(IOException.class, () -> client.save(new TestDocument("test"))), "CouchDb client should not alternate original " +
                "exception");
        request = requestCaptor.getValue();
        put = (HttpPut) request;
        assertTrue(put.isAborted(), "Request must be aborted when exception thrown");
    }

    @Test
    public void testSave() throws IOException {
        IOException thrown = new IOException("error");
        TestDocument testDocument = new TestDocument("test");
        String uuid = UUID.randomUUID().toString();
        testDocument.setId(uuid);
        InputStream content = new ByteArrayInputStream(("{\"id\":\"" + uuid + "\",\"rev\":\"revision\",\"ok\":\"true\"}").getBytes());
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        HttpResponse response = mock(HttpResponse.class);
        HttpEntity entity = mock(HttpEntity.class);
        when(entity.getContent()).thenReturn(content);
        when(response.getEntity()).thenReturn(entity);
        when(httpClient.execute(eq(httpHost), requestCaptor.capture(), eq(httpContext))).thenReturn(response).thenThrow(thrown);

        TestDocument saved = client.save(testDocument);

        HttpRequest request = requestCaptor.getValue();
        assertEquals(HttpPut.class, request.getClass(), "Save has to be done as PUT request");
        HttpPut put = (HttpPut) request;
        assertEquals("http://localhost:5984/test/" + uuid, put.getURI().toString(), "URI must be based on base URI and database name");
        assertEquals("application/json", put.getEntity().getContentType().getValue(), "Content type must be set to json");
        assertContent("{\"_id\":\"" + uuid + "\",\"value\":\"test\",\"value2\":null}", put.getEntity().getContent(), "Body of request is not properly created");
        assertEquals(uuid, saved.getId(), "If document has id set, it must not be changed");
        assertEquals("revision", saved.getRevision(), "Saved document must be updated by given revision");

        assertEquals(thrown, assertThrows(IOException.class, () -> client.save(new TestDocument("test"))), "CouchDb client should not alternate original " +
                "exception");
        request = requestCaptor.getValue();
        put = (HttpPut) request;
        assertTrue(put.isAborted(), "Request must be aborted when exception thrown");
    }

    @Test
    public void testSaveAll() throws IOException{
        IOException thrown = new IOException("error");
        InputStream content = new ByteArrayInputStream(("[{\"id\": \"a\",\"ok\": true,\"rev\": \"rev1\"},{\"id\": \"b\",\"ok\": true,\"rev\": \"rev1\"}]").getBytes());
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        HttpResponse response = mock(HttpResponse.class);
        HttpEntity entity = mock(HttpEntity.class);
        when(entity.getContent()).thenReturn(content);
        when(response.getEntity()).thenReturn(entity);
        when(httpClient.execute(eq(httpHost), requestCaptor.capture(), eq(httpContext))).thenReturn(response).thenThrow(thrown);

        TestDocument a = new TestDocument("a", null, "a", "a");
        TestDocument b = new TestDocument("b", null, "b", "b");
        Iterable<TestDocument> saved = client.saveAll(List.of(a, b), TestDocument.class);

        HttpRequest request = requestCaptor.getValue();
        assertEquals(HttpPost.class, request.getClass(), "Save has to be done as PUT request");
        HttpPost post = (HttpPost) request;
        assertEquals("http://localhost:5984/test/_bulk_docs", post.getURI().toString(), "URI must be based on base URI and database name");
        assertEquals("application/json", post.getEntity().getContentType().getValue(), "Content type must be set to json");
        assertContent("{\"docs\":[{\"_id\":\"a\",\"value\":\"a\",\"value2\":\"a\"},{\"_id\":\"b\",\"value\":\"b\",\"value2\":\"b\"}]}", post.getEntity().getContent(), "Body of request is not properly created");
        assertEquals(2, StreamSupport.stream(saved.spliterator(), false).count(), "Two entities were passed to save");
        assertEquals("aaa", a.getId()+a.getValue()+a.getValue2(), "Id must be updated, value must stay");
        assertEquals("rev1", a.getRevision(), "Revision must be updated");
        assertEquals("bbb", b.getId()+b.getValue()+b.getValue2(), "Id must be updated, value must stay");
        assertEquals("rev1", b.getRevision(), "Revision must be updated");

        assertEquals(thrown, assertThrows(IOException.class, () -> client.saveAll(List.of(new TestDocument("a", "b"), new TestDocument("b", "b")),
                TestDocument.class), "CouchDb client should not alternate original exception"));
        request = requestCaptor.getValue();
        post = (HttpPost) request;
        assertTrue(post.isAborted(), "Request must be aborted when exception thrown");
    }

    @Test
    public void testRead() throws IOException {
        IOException thrown = new IOException("error");
        String id = "unique";
        String revision = "123";
        String value = "test";
        InputStream content =
                new ByteArrayInputStream(("{\"_id\":\"" + id + "\",\"_rev\":\"" + revision + "\",\"value\":\"" + value + "\"}\n").getBytes());
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        HttpResponse response = mock(HttpResponse.class);
        HttpEntity entity = mock(HttpEntity.class);
        when(entity.getContent()).thenReturn(content);
        when(response.getEntity()).thenReturn(entity);
        when(httpClient.execute(eq(httpHost), requestCaptor.capture(), eq(httpContext))).thenReturn(response).thenThrow(thrown);

        TestDocument read = client.read(id, TestDocument.class);

        HttpRequest request = requestCaptor.getValue();
        assertEquals(HttpGet.class, request.getClass(), "Read has to be done as GET request");
        HttpGet get = (HttpGet) request;
        assertEquals("http://localhost:5984/test/" + id, get.getURI().toString(), "URI must be based on base URI and database name");
        assertEquals("application/json", get.getFirstHeader(HttpHeaders.ACCEPT).getValue(), "Get document request should declare accepting json");
        assertEquals(id, read.getId(), "Document entity must be de-serialized completely from content");
        assertEquals(revision, read.getRevision(), "Document entity must be de-serialized completely from content");
        assertEquals(value, read.getValue(), "Document entity must be de-serialized completely from content");

        assertEquals(thrown, assertThrows(IOException.class, () -> client.read(id, TestDocument.class)), "CouchDb client should not alternate original " +
                "exception");
        request = requestCaptor.getValue();
        get = (HttpGet) request;
        assertTrue(get.isAborted(), "Request must be aborted when exception thrown");
    }

    @Test
    public void testReadAllByIds() throws IOException{
        IOException thrown = new IOException("error");
        InputStream content = new ByteArrayInputStream(("{\"results\": [{\"id\": \"a\", \"docs\": [{\"ok\":{\"_id\":\"a\",\"_rev\":\"revA\",\"value\":\"valueA\"," +
                        "\"value2\":\"value2a\"}}]},{\"id\": \"b\", \"docs\": [{\"ok\":{\"_id\":\"b\",\"_rev\":\"revB\",\"value\":\"valueB\",\"value2\":\"value2b\"}}]}]}").getBytes());
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        HttpResponse response = mock(HttpResponse.class);
        HttpEntity entity = mock(HttpEntity.class);
        when(entity.getContent()).thenReturn(content);
        when(response.getEntity()).thenReturn(entity);
        when(httpClient.execute(eq(httpHost), requestCaptor.capture(), eq(httpContext))).thenReturn(response).thenThrow(thrown);

        Iterable<TestDocument> read = client.readAll(List.of("a", "b"), TestDocument.class);

        HttpRequest request = requestCaptor.getValue();
        assertEquals(HttpPost.class, request.getClass(), "Read has to be done as POST request");
        HttpPost post = (HttpPost) request;
        assertEquals("http://localhost:5984/test/_bulk_get" , post.getURI().toString(), "URI must be based on base URI and database name");
        assertEquals("application/json", post.getEntity().getContentType().getValue(), "Find request should declare json content");
        assertContent("{\"docs\":[{\"id\":\"a\"},{\"id\":\"b\"}]}", post.getEntity().getContent(), "Body of request is not properly created");
        StreamSupport.stream(read.spliterator(), false).forEach(d -> {
            assertTrue("a".equals(d.getId()) || "b".equals(d.getId()), "Id was not parsed properly, see json above");
            assertTrue("revA".equals(d.getRevision()) || "revB".equals(d.getRevision()), "Revision was not parsed properly, see json above");
            assertTrue("valueA".equals(d.getValue()) || "valueB".equals(d.getValue()), "Value was not parsed properly, see json above");
            assertTrue("value2a".equals(d.getValue2()) || "value2b".equals(d.getValue2()), "Value2 was not parsed properly, see json above");
        });

        assertThrows(IOException.class, () -> client.readAll(List.of("a", "b"), TestDocument.class));
        request = requestCaptor.getValue();
        post = (HttpPost) request;
        assertTrue(post.isAborted(), "Request must be aborted when exception thrown");
    }

    @Test
    public void testDelete() throws IOException {
        IOException thrown = new IOException("error");
        InputStream content = new ByteArrayInputStream(("").getBytes());
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        HttpResponse response = mock(HttpResponse.class);
        HttpEntity entity = mock(HttpEntity.class);
        when(entity.getContent()).thenReturn(content);
        when(response.getEntity()).thenReturn(entity);
        when(httpClient.execute(eq(httpHost), requestCaptor.capture(), eq(httpContext))).thenReturn(response).thenThrow(thrown);

        String id = "unique";
        String revision = "123";
        String value = "test";
        TestDocument testDocument = new TestDocument(id, revision, value);
        client.delete(testDocument);

        HttpRequest request = requestCaptor.getValue();
        assertEquals(HttpDelete.class, request.getClass(), "Delete has to be done as DELETE request");
        HttpDelete delete = (HttpDelete) request;
        assertEquals("http://localhost:5984/test/" + id + "?rev=" + revision, delete.getURI().toString(), "URI must be based on base URI and database name");

        assertEquals(thrown, assertThrows(IOException.class, () -> client.delete(testDocument)), "CouchDb client should not alternate original " +
                "exception");
        request = requestCaptor.getValue();
        delete = (HttpDelete) request;
        assertTrue(delete.isAborted(), "Request must be aborted when exception thrown");
    }

    @Test
    public void testFind() throws IOException {
        IOException thrown = new IOException("error");
        InputStream content = new ByteArrayInputStream(("{\"docs\":[{\"_id\":\"unique1\",\"_rev\":\"1231\",\"value\":\"value1\"},{\"_id\":\"unique2\"," +
                "\"_rev\":\"1232\",\"value\":\"value2\"},{\"_id\":\"unique3\",\"_rev\":\"1233\",\"value\":\"value3\"}],\"bookmark\": \"1234\",\"warning\": " +
                "\"warning\"}").getBytes());
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        HttpResponse response = mock(HttpResponse.class);
        HttpEntity entity = mock(HttpEntity.class);
        when(entity.getContent()).thenReturn(content);
        when(response.getEntity()).thenReturn(entity);
        when(httpClient.execute(eq(httpHost), requestCaptor.capture(), eq(httpContext))).thenReturn(response).thenThrow(thrown);

        Iterable<TestDocument> read = client.find("", TestDocument.class);

        HttpRequest request = requestCaptor.getValue();
        assertEquals(HttpPost.class, request.getClass(), "Find has to be done as POST request");
        HttpPost post = (HttpPost) request;
        assertEquals("http://localhost:5984/test/_find", post.getURI().toString(), "URI must be based on base URI and database name");
        assertEquals("application/json", post.getEntity().getContentType().getValue(), "Find request should declare json content");
        assertContent("", post.getEntity().getContent(), "Body of request is not properly created");
        assertEquals(3, StreamSupport.stream(read.spliterator(), false).count(), "Based on mocked json, there are 3 documents returned");
        int i = 1;
        for(TestDocument d : read) {
            assertEquals("unique" + i, d.getId(), "Document with index " + i + " has in-properly de-serialized id");
            assertEquals("123" + i, d.getRevision(), "Document with index " + i + " has in-properly de-serialized revision");
            assertEquals("value" + i, d.getValue(), "Document with index " + i + " has in-properly de-serialized value");
            i++;
        }

        assertEquals(thrown, assertThrows(IOException.class, () -> client.find("", TestDocument.class)), "CouchDb client should not alternate original " +
                "exception");
        request = requestCaptor.getValue();
        post = (HttpPost) request;
        assertTrue(post.isAborted(), "Request must be aborted when exception thrown");
    }

    @Test
    public void testClose() throws IOException {
        CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
        client = new CouchDbClient(httpClient, httpHost, httpContext, baseURI);
        client.close();
        verify(httpClient, only().description("Http client must be closed")).close();
    }

    @Test
    public void testReadAll() throws IOException {
        IOException thrown = new IOException("error");
        InputStream content = new ByteArrayInputStream(("{\"total_rows\":3,\"offset\":0,\"rows\":[{\"id\":\"1\",\"key\":\"1\",\"value\":{\"rev\":\"1-0\"}}," +
                "{\"id\":\"2\",\"key\":\"2\",\"value\":{\"rev\":\"2-0\"}},{\"id\":\"3\",\"key\":\"3\",\"value\":{\"rev\":\"3-0\"}},{\"id\":\"_design0\"," +
                "\"key\":\"_design0\",\"value\":{\"rev\":\"d-0\"}}]}\n").getBytes());
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        HttpResponse response = mock(HttpResponse.class);
        HttpEntity entity = mock(HttpEntity.class);
        when(entity.getContent()).thenReturn(content);
        when(response.getEntity()).thenReturn(entity);
        when(httpClient.execute(eq(httpHost), requestCaptor.capture(), eq(httpContext))).thenReturn(response, response).thenThrow(thrown);
        Iterable<String> all = client.readAll(TestDocument.class);
        HttpRequest request = requestCaptor.getValue();
        assertEquals(HttpGet.class, request.getClass(), "Find has to be done as GET request");
        HttpGet get = (HttpGet) request;
        assertEquals("http://localhost:5984/test/_all_docs", get.getURI().toString(), "URI must be based on base URI and database name");
        assertEquals("application/json", get.getFirstHeader(HttpHeaders.ACCEPT).getValue(), "Get document request should declare accepting json");
        assertEquals(3, StreamSupport.stream(all.spliterator(), false).count(), "Result of find was not properly read or filtered");
        int i = 1;
        for(String id : all){
            assertEquals(i++ + "", id, "One row is missing in the result");
        }
        content.reset();

        Iterable<String> allDesign = client.readAllDesign(TestDocument.class);
        request = requestCaptor.getValue();
        assertEquals(HttpGet.class, request.getClass(), "Find has to be done as GET request");
        get = (HttpGet) request;
        assertEquals("http://localhost:5984/test/_all_docs", get.getURI().toString(), "URI must be based on base URI and database name");
        assertEquals("application/json", get.getFirstHeader(HttpHeaders.ACCEPT).getValue(), "Get document request should declare accepting json");
        assertEquals(1, StreamSupport.stream(allDesign.spliterator(), false).count(), "Result of find was not properly read or filtered");
        i = 0;
        for(String id : allDesign){
            assertEquals("_design" + i++, id, "One row is missing in the result");
        }

        assertEquals(thrown, assertThrows(IOException.class, () -> client.readAll(TestDocument.class)), "CouchDb client should not alternate original " +
                "exception");
        request = requestCaptor.getValue();
        get = (HttpGet) request;
        assertTrue(get.isAborted(), "Request must be aborted when exception thrown");
    }

    @Test
    public void testDeleteById() throws IOException {
        IOException thrown = new IOException("error");

        InputStream getContent = new ByteArrayInputStream(("{\"_id\":\"unique\",\"_rev\":\"1\",\"value\":\"value\",\"value2\":\"value2\"}").getBytes());
        InputStream deleteContent = new ByteArrayInputStream(("").getBytes());
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        HttpResponse response = mock(HttpResponse.class);
        HttpEntity entity = mock(HttpEntity.class);
        when(entity.getContent()).thenReturn(getContent).thenReturn(deleteContent);
        when(response.getEntity()).thenReturn(entity);
        when(httpClient.execute(eq(httpHost), requestCaptor.capture(), eq(httpContext))).thenReturn(response, response).thenThrow(thrown);
        TestDocument deleted = client.deleteById("unique", TestDocument.class);
        HttpRequest request = requestCaptor.getValue();
        assertEquals(HttpDelete.class, request.getClass(), "Find has to be done as DELETE request");
        HttpDelete delete = (HttpDelete) request;
        assertTrue(delete.getURI().toString().startsWith("http://localhost:5984/test/unique"), "URI must be based on base URI and database name");
        assertEquals("http://localhost:5984/test/unique?rev=1", delete.getURI().toString(), "Request must specify revision which was read from DB");
        assertEquals("application/json", delete.getFirstHeader(HttpHeaders.ACCEPT).getValue(), "Delete document request should declare accepting json");
        assertEquals("1", deleted.getRevision(), "Returned document is not matching the deleted one");
        assertEquals("unique", deleted.getId(), "Returned document is not matching the deleted one");
        assertEquals("value", deleted.getValue(), "Returned document is not matching the deleted one");
        assertEquals("value2", deleted.getValue2(), "Returned document is not matching the deleted one");

        assertEquals(thrown, assertThrows(IOException.class, () -> client.deleteById("unique", TestDocument.class)), "CouchDb client should not alternate original " +
                "exception");
        request = requestCaptor.getValue();
        HttpGet get = (HttpGet) request;
        assertTrue(get.isAborted(), "Request must be aborted when exception thrown");
    }

    @Test
    public void testCreateDatabase() throws IOException {
        createDatabaseTestWrapper(c -> c.createDatabase(TestDocument.class));
    }

    @Test
    public void testCreateDatabase2() throws IOException {
        createDatabaseTestWrapper(c -> c.createDatabase("test"));
    }

    @Test
    public void testCreateDatabase3() throws IOException {
        createDatabaseTestWrapper(c -> c.createDatabase(TestDocument.class, 8, 3, false));
    }

    @Test
    public void testCreateDatabase4() throws IOException {
        createDatabaseTestWrapper(c -> c.createDatabase("test", 8, 3, false));
    }

    private void createDatabaseTestWrapper(ThrowingConsumer<CouchDbClient, IOException> testedAction) throws IOException {
        IOException thrown = new IOException("error");
        InputStream content = new ByteArrayInputStream(("").getBytes());
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        HttpResponse response = mock(HttpResponse.class);
        HttpEntity entity = mock(HttpEntity.class);
        when(entity.getContent()).thenReturn(content);
        when(response.getEntity()).thenReturn(entity);
        when(httpClient.execute(eq(httpHost), requestCaptor.capture(), eq(httpContext))).thenReturn(response).thenThrow(thrown);

        testedAction.accept(client);

        HttpRequest request = requestCaptor.getValue();
        assertEquals(HttpPut.class, request.getClass(), "Find has to be done as PUT request");
        HttpPut put = (HttpPut) request;
        assertEquals("http://localhost:5984/test?q=8&n=3&partitioned=false", put.getURI().toString(), "URI must be based on base URI and database name");
        assertContent("", put.getEntity().getContent(), "Body of request is not properly created");
        assertEquals(thrown, assertThrows(IOException.class, () -> testedAction.accept(client)), "CouchDb client should not alternate original " +
                "exception");
        request = requestCaptor.getValue();
        put = (HttpPut) request;
        assertTrue(put.isAborted(), "Request must be aborted when exception thrown");
    }

    @Test
    public void testCreateIndex() throws IOException {
        createIndexTestWrapper(c -> c.createIndex("test", "test", List.of(Sort.Order.asc("value"))));
    }

    @Test
    public void testCreateIndex2() throws IOException {
        createIndexTestWrapper(c -> c.createIndex("test", TestDocument.class, List.of(Sort.Order.asc("value"))));
    }

    @Test
    public void testCreateIndex3() throws IOException {
        createIndexTestWrapper(c -> c.createIndex("test", TestDocument.class, Sort.Order.asc("value")));
    }

    private void createIndexTestWrapper(ThrowingConsumer<CouchDbClient, IOException> testedAction) throws IOException {
        IOException thrown = new IOException("error");
        InputStream content = new ByteArrayInputStream(("").getBytes());
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        HttpResponse response = mock(HttpResponse.class);
        HttpEntity entity = mock(HttpEntity.class);
        when(entity.getContent()).thenReturn(content);
        when(response.getEntity()).thenReturn(entity);
        when(httpClient.execute(eq(httpHost), requestCaptor.capture(), eq(httpContext))).thenReturn(response).thenThrow(thrown);

        testedAction.accept(client);

        HttpRequest request = requestCaptor.getValue();
        assertEquals(HttpPost.class, request.getClass(), "Find has to be done as POST request");
        HttpPost post = (HttpPost) request;
        assertEquals("http://localhost:5984/test/_index", post.getURI().toString(), "URI must be based on base URI and database name");
        assertEquals("application/json", post.getEntity().getContentType().getValue(), "Find request should declare json content");

        assertEquals(thrown, assertThrows(IOException.class, () -> testedAction.accept(client)), "CouchDb client should not alternate original " +
                "exception");
        request = requestCaptor.getValue();
        post = (HttpPost) request;
        assertTrue(post.isAborted(), "Request must be aborted when exception thrown");
    }

    private static void assertContent(String s, InputStream actual, String message) throws IOException{
        InputStream expected = new ByteArrayInputStream(s.getBytes());
        if(!IOUtils.contentEquals(actual, expected)){
            actual.reset();
            expected.reset();
            throw new AssertionFailedError(message, IOUtils.toString(expected, StandardCharsets.UTF_8.name()), IOUtils.toString(actual,
                    StandardCharsets.UTF_8.name()) );
        }
    }

}