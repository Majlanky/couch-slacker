package com.groocraft.couchdb.slacker.repository;

import com.groocraft.couchdb.slacker.CouchDbClient;
import com.groocraft.couchdb.slacker.TestDocument;
import com.groocraft.couchdb.slacker.exception.CouchDbRuntimeException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.repository.query.Parameter;
import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.util.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

class CouchDbDirectQueryTest {

    @Test
    void testIndexedReplacement() throws IOException {
        CouchDbClient client = Mockito.mock(CouchDbClient.class);
        QueryMethod queryMethod = Mockito.mock(QueryMethod.class);
        Parameter parameter = Mockito.mock(Parameter.class);
        Parameters<?, ?> parameters = Mockito.mock(Parameters.class);
        CouchDbDirectQuery query = new CouchDbDirectQuery("{\"selector\": {\"value\": {\"$eq\": ?1}}}", client, queryMethod, TestDocument.class);
        assertEquals(queryMethod, query.getQueryMethod(), "CouchDbDirectQuery do not remember given queryMethod");
        Mockito.when(client.find(Mockito.any(String.class), Mockito.any())).thenReturn(Pair.of(new ArrayList<>(), ""));
        Mockito.doReturn(Object.class).when(queryMethod).getReturnedObjectType();
        Mockito.when(parameter.getName()).thenReturn(Optional.of("value"));
        doReturn(String.class).when(parameter).getType();
        Mockito.when(parameter.getIndex()).thenReturn(0);
        Mockito.doReturn(Collections.singletonList(parameter).iterator()).when(parameters).iterator();
        Mockito.doReturn(parameters).when(queryMethod).getParameters();
        query.execute(new Object[]{"test"});
        Mockito.verify(client).find("{\"selector\": {\"value\": {\"$eq\": \"test\"}}}", TestDocument.class);
    }

    @Test
    void testNamedStringReplacement() throws IOException {
        CouchDbClient client = Mockito.mock(CouchDbClient.class);
        QueryMethod queryMethod = Mockito.mock(QueryMethod.class);
        Parameter parameter = Mockito.mock(Parameter.class);
        Parameters<?, ?> parameters = Mockito.mock(Parameters.class);
        CouchDbDirectQuery query = new CouchDbDirectQuery("{\"selector\": {\"value\": {\"$eq\": :value}}}", client, queryMethod, TestDocument.class);
        assertEquals(queryMethod, query.getQueryMethod(), "CouchDbDirectQuery do not remember given queryMethod");
        Mockito.when(client.find(Mockito.any(String.class), Mockito.any())).thenReturn(Pair.of(new ArrayList<>(), ""));
        Mockito.doReturn(Object.class).when(queryMethod).getReturnedObjectType();
        Mockito.when(parameter.getName()).thenReturn(Optional.of("value"));
        doReturn(String.class).when(parameter).getType();
        Mockito.when(parameter.getIndex()).thenReturn(0);
        Mockito.doReturn(Collections.singletonList(parameter).iterator()).when(parameters).iterator();
        Mockito.doReturn(parameters).when(queryMethod).getParameters();
        query.execute(new Object[]{"test"});
        Mockito.verify(client).find("{\"selector\": {\"value\": {\"$eq\": \"test\"}}}", TestDocument.class);
    }

    @Test
    void testNamedNonStringReplacement() throws IOException {
        CouchDbClient client = Mockito.mock(CouchDbClient.class);
        QueryMethod queryMethod = Mockito.mock(QueryMethod.class);
        Parameter parameter = Mockito.mock(Parameter.class);
        Parameters<?, ?> parameters = Mockito.mock(Parameters.class);
        CouchDbDirectQuery query = new CouchDbDirectQuery("{\"selector\": {\"value\": {\"$eq\": :value}}}", client, queryMethod, TestDocument.class);
        assertEquals(queryMethod, query.getQueryMethod(), "CouchDbDirectQuery do not remember given queryMethod");
        Mockito.when(client.find(Mockito.any(String.class), Mockito.any())).thenReturn(Pair.of(new ArrayList<>(), ""));
        Mockito.doReturn(Object.class).when(queryMethod).getReturnedObjectType();
        Mockito.when(parameter.getName()).thenReturn(Optional.of("value"));
        doReturn(Integer.class).when(parameter).getType();
        Mockito.when(parameter.getIndex()).thenReturn(0);
        Mockito.doReturn(Collections.singletonList(parameter).iterator()).when(parameters).iterator();
        Mockito.doReturn(parameters).when(queryMethod).getParameters();
        query.execute(new Object[]{1});
        Mockito.verify(client).find("{\"selector\": {\"value\": {\"$eq\": 1}}}", TestDocument.class);
    }

    @Test
    void testNamedIterableReplacement() throws IOException {
        CouchDbClient client = Mockito.mock(CouchDbClient.class);
        QueryMethod queryMethod = Mockito.mock(QueryMethod.class);
        Parameter parameter = Mockito.mock(Parameter.class);
        Parameters<?, ?> parameters = Mockito.mock(Parameters.class);
        CouchDbDirectQuery query = new CouchDbDirectQuery("{\"selector\": {\"value\": {\"$eq\": :value}}}", client, queryMethod, TestDocument.class);
        assertEquals(queryMethod, query.getQueryMethod(), "CouchDbDirectQuery do not remember given queryMethod");
        Mockito.when(client.find(Mockito.any(String.class), Mockito.any())).thenReturn(Pair.of(new ArrayList<>(), ""));
        Mockito.doReturn(Object.class).when(queryMethod).getReturnedObjectType();
        Mockito.when(parameter.getName()).thenReturn(Optional.of("value"));
        doReturn(List.class).when(parameter).getType();
        Mockito.when(parameter.getIndex()).thenReturn(0);
        Mockito.doReturn(Collections.singletonList(parameter).iterator()).when(parameters).iterator();
        Mockito.doReturn(parameters).when(queryMethod).getParameters();
        query.execute(new Object[]{Arrays.asList("test", "test1")});
        Mockito.verify(client).find("{\"selector\": {\"value\": {\"$eq\": [\"test\",\"test1\"]}}}", TestDocument.class);
    }

    @Test
    void testFindException() throws IOException {
        CouchDbClient client = Mockito.mock(CouchDbClient.class);
        QueryMethod queryMethod = Mockito.mock(QueryMethod.class);
        Parameter parameter = Mockito.mock(Parameter.class);
        Parameters<?, ?> parameters = Mockito.mock(Parameters.class);
        CouchDbDirectQuery query = new CouchDbDirectQuery("{\"selector\": {\"value\": {\"$eq\": :value}}}", client, queryMethod, TestDocument.class);
        assertEquals(queryMethod, query.getQueryMethod(), "CouchDbDirectQuery do not remember given queryMethod");
        Mockito.when(client.find(Mockito.any(String.class), Mockito.any())).thenThrow(new IOException());
        Mockito.doReturn(Object.class).when(queryMethod).getReturnedObjectType();
        Mockito.when(parameter.getName()).thenReturn(Optional.of("value"));
        doReturn(String.class).when(parameter).getType();
        Mockito.when(parameter.getIndex()).thenReturn(0);
        Mockito.doReturn(Collections.singletonList(parameter).iterator()).when(parameters).iterator();
        Mockito.doReturn(parameters).when(queryMethod).getParameters();
        Object[] p = new Object[]{"test"};
        assertThrows(CouchDbRuntimeException.class, () -> query.execute(p), "Any issue must be reported as " + CouchDbRuntimeException.class);
    }

}