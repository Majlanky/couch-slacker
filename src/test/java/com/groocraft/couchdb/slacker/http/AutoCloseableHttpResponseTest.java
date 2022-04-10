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
    void test() throws IOException {
        Mockito.when(response.getEntity()).thenReturn(entity);
        Mockito.when(entity.getContent()).thenReturn(stream);
        AutoCloseableHttpResponse autoResponse = new AutoCloseableHttpResponse();
        autoResponse.set(this.response);
        assertEquals(this.response, autoResponse.get(), "AutoCloseable wrapper do not remember wrapped instance");
        autoResponse.close();
        Mockito.verify(this.stream, Mockito.only().description("Close method must be called on the wrapped instance")).close();
    }

    @Test
    void testTryCatch() throws IOException {
        Mockito.when(response.getEntity()).thenReturn(entity);
        Mockito.when(entity.getContent()).thenReturn(stream);
        try (AutoCloseableHttpResponse autoResponse = new AutoCloseableHttpResponse()) {
            autoResponse.set(this.response);
        }
        Mockito.verify(this.stream, Mockito.only().description("Close method must be called automatically on the wrapped instance")).close();
    }

    @Test
    void testNonClosable() throws IOException {
        Mockito.when(response.getEntity()).thenReturn(entity);
        Mockito.when(entity.getContent()).thenReturn(stream);
        Mockito.doThrow(new IOException()).when(stream).close();
        AutoCloseableHttpResponse autoResponse = new AutoCloseableHttpResponse();
        autoResponse.set(this.response);
        Assertions.assertDoesNotThrow(autoResponse::close);
        Mockito.verify(this.stream, Mockito.only().description("Close method must be called on the wrapped instance")).close();
    }

}