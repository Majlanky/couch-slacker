package com.groocraft.couchdb.slacker.http;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class AutoCloseableHttpResponseTest {

    @Mock
    HttpResponse response;

    @Mock
    HttpEntity entity;

    @Mock
    InputStream stream;

    @Test
    public void test() throws IOException {
        Mockito.when(response.getEntity()).thenReturn(entity);
        Mockito.when(entity.getContent()).thenReturn(stream);
        AutoCloseableHttpResponse autoResponse = new AutoCloseableHttpResponse();
        autoResponse.set(this.response);
        assertEquals(this.response, autoResponse.get(), "AutoCloseable wrapper do not remember wrapped instance");
        autoResponse.close();
        Mockito.verify(this.stream, Mockito.only().description("Close method must be called on the wrapped instance")).close();
    }

    @Test
    public void testTryCatch() throws IOException {
        Mockito.when(response.getEntity()).thenReturn(entity);
        Mockito.when(entity.getContent()).thenReturn(stream);
        try (AutoCloseableHttpResponse autoResponse = new AutoCloseableHttpResponse()) {
            autoResponse.set(this.response);
        }
        Mockito.verify(this.stream, Mockito.only().description("Close method must be called automatically on the wrapped isntance")).close();
    }

    @Test
    public void testNonClosable() throws IOException {
        Mockito.when(response.getEntity()).thenReturn(entity);
        Mockito.when(entity.getContent()).thenReturn(stream);
        Mockito.doThrow(new IOException()).when(stream).close();
        AutoCloseableHttpResponse autoResponse = new AutoCloseableHttpResponse();
        autoResponse.set(this.response);
        Assertions.assertDoesNotThrow(() -> autoResponse.close());
        Mockito.verify(this.stream, Mockito.only().description("Close method must be called on the wrapped instance")).close();
    }

}