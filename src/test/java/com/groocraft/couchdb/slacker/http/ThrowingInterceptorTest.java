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

import com.groocraft.couchdb.slacker.exception.CouchDbException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.RequestLine;
import org.apache.http.StatusLine;
import org.apache.http.client.protocol.HttpClientContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ThrowingInterceptorTest {

    @Mock
    HttpRequest request;

    @Mock
    RequestLine requestLine;

    @Mock
    HttpResponse response;

    @Mock
    StatusLine statusLine;

    @Mock
    HttpClientContext context;

    ThrowingInterceptor interceptor;

    @BeforeEach
    public void setUp() {
        interceptor = new ThrowingInterceptor();
    }

    @ParameterizedTest
    @ValueSource(ints = {HttpStatus.SC_OK, HttpStatus.SC_CREATED, HttpStatus.SC_ACCEPTED, HttpStatus.SC_NOT_MODIFIED})
    void testOkResponses(int status) {
        assertDoesNotThrow(() -> {
            when(response.getStatusLine()).thenReturn(statusLine);
            when(statusLine.getStatusCode()).thenReturn(status);
            interceptor.process(response, context);
        }, "All listed parameters are OK responses, no exception should be thrown");
    }

    @ParameterizedTest
    @MethodSource("getNonOkHttpStatuses")
    void testNotOkResponse(int status) {
        assertThrows(CouchDbException.class, () -> {
            when(response.getStatusLine()).thenReturn(statusLine);
            when(statusLine.getStatusCode()).thenReturn(status);
            when(statusLine.getReasonPhrase()).thenReturn("because I want to");
            when(context.getRequest()).thenReturn(request);
            when(request.getRequestLine()).thenReturn(requestLine);
            when(requestLine.getMethod()).thenReturn("GET");
            when(requestLine.getUri()).thenReturn("testURI");
            interceptor.process(response, context);
        }, "Every non OK response must be processed as exception with detail message what is wrong");
    }

    private static Stream<Integer> getNonOkHttpStatuses() {
        return Arrays.stream(HttpStatus.class.getFields()).map(f -> {
            try {
                return (Integer) f.get(null);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("We are expecting only static fields in the HttpStatus class which represents a number");
            }
        }).filter(i -> i > HttpStatus.SC_ACCEPTED && i != HttpStatus.SC_NOT_MODIFIED);
    }

}