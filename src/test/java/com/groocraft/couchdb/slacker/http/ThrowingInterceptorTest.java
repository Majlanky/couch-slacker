package com.groocraft.couchdb.slacker.http;

import com.groocraft.couchdb.slacker.exception.CouchDbException;
import com.groocraft.couchdb.slacker.utils.ThrowingStream;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.RequestLine;
import org.apache.http.StatusLine;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.protocol.HttpContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;

import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

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
    public void testOkResponses(int status) {
        assertDoesNotThrow(() -> {
            when(response.getStatusLine()).thenReturn(statusLine);
            when(statusLine.getStatusCode()).thenReturn(status);
            interceptor.process(response, context);
        }, "All listed parameters are OK responses, no exception should be thrown");
    }

    @ParameterizedTest
    @MethodSource("getNonOkHttpStatuses")
    public void testNotOkResponse(int status) {
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
        return ThrowingStream.<Field, Integer>of(List.of(HttpStatus.class.getFields())).forEach(f -> (Integer) f.get(null))
                .throwIfUnprocessed(m -> new RuntimeException("Poorly implemented test"))
                .filter(i -> i > HttpStatus.SC_ACCEPTED && i != HttpStatus.SC_NOT_MODIFIED).collect(Collectors.toList()).stream();
    }

}