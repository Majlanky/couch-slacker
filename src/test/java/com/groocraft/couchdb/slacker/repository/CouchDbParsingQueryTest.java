package com.groocraft.couchdb.slacker.repository;

import com.groocraft.couchdb.slacker.CouchDbClient;
import com.groocraft.couchdb.slacker.TestDocument;
import com.groocraft.couchdb.slacker.annotation.Index;
import com.groocraft.couchdb.slacker.exception.CouchDbRuntimeException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.repository.query.Parameter;
import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.ResultProcessor;
import org.springframework.data.repository.query.ReturnedType;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CouchDbParsingQueryTest {

    @Test
    public void testNoProjection() throws IOException {
        CouchDbClient client = mock(CouchDbClient.class);
        QueryMethod queryMethod = mock(QueryMethod.class);
        ResultProcessor resultProcessor = mock(ResultProcessor.class);
        ReturnedType returnedType = mock(ReturnedType.class);
        Method method = mock(Method.class);
        Parameter parameter = mock(Parameter.class);
        Parameters<?, ?> parameters = mock(Parameters.class);

        when(queryMethod.getName()).thenReturn("findByValue");
        when(client.find(any(), any())).thenReturn(new ArrayList<Object>());
        doReturn(TestDocument.class).when(queryMethod).getReturnedObjectType();
        when(queryMethod.getResultProcessor()).thenReturn(resultProcessor);
        when(resultProcessor.getReturnedType()).thenReturn(returnedType);
        doReturn(TestDocument.class).when(returnedType).getDomainType();
        when(method.getAnnotation(Index.class)).thenReturn(null);
        when(parameter.getName()).thenReturn(Optional.of("value"));
        when(parameter.getIndex()).thenReturn(0);
        doReturn(List.of(parameter).iterator()).when(parameters).iterator();
        doReturn(parameters).when(queryMethod).getParameters();
        when(parameters.getSortIndex()).thenReturn(-1);
        TestDocument result = new TestDocument();
        when(client.find(any(), eq(TestDocument.class))).thenReturn(List.of(result)).thenThrow(new IOException("error"));

        CouchDbParsingQuery query = new CouchDbParsingQuery(client, method, queryMethod, TestDocument.class);
        assertEquals(queryMethod, query.getQueryMethod(), "CouchDbDirectQuery do not remember given queryMethod");
        Object o = query.execute(new Object[]{"test"});
        verify(client).find("{\"selector\":{\"$or\":[{\"value\":{\"$eq\":\"test\"}}]}}", TestDocument.class);
        assertDoesNotThrow(() -> (List<TestDocument>)o, "Result must be list of documents");
        assertEquals(1, ((List<TestDocument>)o).size(), "Query should not alternate result in this case");
        assertEquals(result, ((List<TestDocument>)o).get(0), "Query should not alternate result in this case");
        CouchDbRuntimeException ex = assertThrows(CouchDbRuntimeException.class, () -> {
            query.execute(new Object[]{"test"});
        }, "Every thrown exception must be reported");
        assertEquals("error", ex.getCause().getMessage(), "Repository must pass original cause of exceptional state");
    }

    @Test
    public void testCountProjection() throws IOException{
        CouchDbClient client = mock(CouchDbClient.class);
        QueryMethod queryMethod = mock(QueryMethod.class);
        ResultProcessor resultProcessor = mock(ResultProcessor.class);
        ReturnedType returnedType = mock(ReturnedType.class);
        Method method = mock(Method.class);
        Parameter parameter = mock(Parameter.class);
        Parameters<?, ?> parameters = mock(Parameters.class);

        when(queryMethod.getName()).thenReturn("countByValue");
        when(client.find(any(), any())).thenReturn(new ArrayList<Object>());
        doReturn(TestDocument.class).when(queryMethod).getReturnedObjectType();
        when(queryMethod.getResultProcessor()).thenReturn(resultProcessor);
        when(resultProcessor.getReturnedType()).thenReturn(returnedType);
        doReturn(TestDocument.class).when(returnedType).getDomainType();
        when(method.getAnnotation(Index.class)).thenReturn(null);
        when(parameter.getName()).thenReturn(Optional.of("value"));
        when(parameter.getIndex()).thenReturn(0);
        doReturn(List.of(parameter).iterator()).when(parameters).iterator();
        doReturn(parameters).when(queryMethod).getParameters();
        when(parameters.getSortIndex()).thenReturn(-1);
        when(client.find(any(), eq(TestDocument.class))).thenReturn(List.of(new TestDocument()));

        CouchDbParsingQuery query = new CouchDbParsingQuery(client, method, queryMethod, TestDocument.class);
        assertEquals(queryMethod, query.getQueryMethod(), "CouchDbDirectQuery do not remember given queryMethod");
        Object o = query.execute(new Object[]{"test"});
        verify(client).find("{\"selector\":{\"$or\":[{\"value\":{\"$eq\":\"test\"}}]}}", TestDocument.class);
        assertDoesNotThrow(() -> (Integer)o, "Result must be number if count projection is configured");
        assertEquals(1, (Integer)o, "Find return list of one, so result should be one");
    }

    @Test
    public void testDelete() throws IOException {
        CouchDbClient client = mock(CouchDbClient.class);
        QueryMethod queryMethod = mock(QueryMethod.class);
        ResultProcessor resultProcessor = mock(ResultProcessor.class);
        ReturnedType returnedType = mock(ReturnedType.class);
        Method method = mock(Method.class);
        Parameter parameter = mock(Parameter.class);
        Parameters<?, ?> parameters = mock(Parameters.class);

        when(queryMethod.getName()).thenReturn("deleteByValue");
        when(client.find(any(), any())).thenReturn(new ArrayList<Object>());
        doReturn(TestDocument.class).when(queryMethod).getReturnedObjectType();
        when(queryMethod.getResultProcessor()).thenReturn(resultProcessor);
        when(resultProcessor.getReturnedType()).thenReturn(returnedType);
        doReturn(TestDocument.class).when(returnedType).getDomainType();
        when(method.getAnnotation(Index.class)).thenReturn(null);
        when(parameter.getName()).thenReturn(Optional.of("value"));
        when(parameter.getIndex()).thenReturn(0);
        doReturn(List.of(parameter).iterator()).when(parameters).iterator();
        doReturn(parameters).when(queryMethod).getParameters();
        when(parameters.getSortIndex()).thenReturn(-1);
        TestDocument result = new TestDocument();
        when(client.find(any(), eq(TestDocument.class))).thenReturn(List.of(result));

        CouchDbParsingQuery query = new CouchDbParsingQuery(client, method, queryMethod, TestDocument.class);
        assertEquals(queryMethod, query.getQueryMethod(), "CouchDbDirectQuery do not remember given queryMethod");
        query.execute(new Object[]{"test"});
        verify(client).find("{\"selector\":{\"$or\":[{\"value\":{\"$eq\":\"test\"}}]}}", TestDocument.class);
        verify(client, atLeastOnce().description("Every document find by given rules must be deleted")).delete(result);
    }

    @Test
    public void testExistsProjection() throws IOException{
        CouchDbClient client = mock(CouchDbClient.class);
        QueryMethod queryMethod = mock(QueryMethod.class);
        ResultProcessor resultProcessor = mock(ResultProcessor.class);
        ReturnedType returnedType = mock(ReturnedType.class);
        Method method = mock(Method.class);
        Parameter parameter = mock(Parameter.class);
        Parameters<?, ?> parameters = mock(Parameters.class);

        when(queryMethod.getName()).thenReturn("existsByValue");
        when(client.find(any(), any())).thenReturn(new ArrayList<Object>());
        doReturn(TestDocument.class).when(queryMethod).getReturnedObjectType();
        when(queryMethod.getResultProcessor()).thenReturn(resultProcessor);
        when(resultProcessor.getReturnedType()).thenReturn(returnedType);
        doReturn(TestDocument.class).when(returnedType).getDomainType();
        when(method.getAnnotation(Index.class)).thenReturn(null);
        when(parameter.getName()).thenReturn(Optional.of("value"));
        when(parameter.getIndex()).thenReturn(0);
        doReturn(List.of(parameter).iterator()).when(parameters).iterator();
        doReturn(parameters).when(queryMethod).getParameters();
        when(parameters.getSortIndex()).thenReturn(-1);
        when(client.find(any(), eq(TestDocument.class))).thenReturn(List.of(new TestDocument()));

        CouchDbParsingQuery query = new CouchDbParsingQuery(client, method, queryMethod, TestDocument.class);
        assertEquals(queryMethod, query.getQueryMethod(), "CouchDbDirectQuery do not remember given queryMethod");
        Object o = query.execute(new Object[]{"test"});
        verify(client).find("{\"selector\":{\"$or\":[{\"value\":{\"$eq\":\"test\"}}]}}", TestDocument.class);
        assertDoesNotThrow(() -> (Boolean)o, "Result must be boolean if delete projection is configured");
        assertEquals(true, (Boolean)o, "Find return list of one, so result should be true");
    }

    @Test
    public void testIndex() throws IOException{
        CouchDbClient client = mock(CouchDbClient.class);
        QueryMethod queryMethod = mock(QueryMethod.class);
        ResultProcessor resultProcessor = mock(ResultProcessor.class);
        ReturnedType returnedType = mock(ReturnedType.class);
        Method method = mock(Method.class);
        Parameter parameter = mock(Parameter.class);
        Parameters<?, ?> parameters = mock(Parameters.class);
        Index index = mock(Index.class);

        when(queryMethod.getName()).thenReturn("findByValue");
        when(client.find(any(), any())).thenReturn(new ArrayList<Object>());
        doReturn(TestDocument.class).when(queryMethod).getReturnedObjectType();
        when(queryMethod.getResultProcessor()).thenReturn(resultProcessor);
        when(resultProcessor.getReturnedType()).thenReturn(returnedType);
        doReturn(TestDocument.class).when(returnedType).getDomainType();
        when(method.getAnnotation(Index.class)).thenReturn(index);
        when(index.value()).thenReturn(new String[]{"test"});
        when(parameter.getName()).thenReturn(Optional.of("value"));
        when(parameter.getIndex()).thenReturn(0);
        doReturn(List.of(parameter).iterator()).when(parameters).iterator();
        doReturn(parameters).when(queryMethod).getParameters();
        when(parameters.getSortIndex()).thenReturn(-1);
        TestDocument result = new TestDocument();
        when(client.find(any(), eq(TestDocument.class))).thenReturn(List.of(result));

        CouchDbParsingQuery query = new CouchDbParsingQuery(client, method, queryMethod, TestDocument.class);
        assertEquals(queryMethod, query.getQueryMethod(), "CouchDbDirectQuery do not remember given queryMethod");
        Object o = query.execute(new Object[]{"test"});
        verify(client).find("{\"use_index\":[\"test\"],\"selector\":{\"$or\":[{\"value\":{\"$eq\":\"test\"}}]}}", TestDocument.class);
        assertDoesNotThrow(() -> (List<TestDocument>)o, "Result must be list of documents");
        assertEquals(1, ((List<TestDocument>)o).size(), "Query should not alternate result in this case");
        assertEquals(result, ((List<TestDocument>)o).get(0), "Query should not alternate result in this case");
    }

}