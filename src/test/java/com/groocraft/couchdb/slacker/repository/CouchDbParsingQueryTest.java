package com.groocraft.couchdb.slacker.repository;

import com.groocraft.couchdb.slacker.CouchDbClient;
import com.groocraft.couchdb.slacker.TestDocument;
import com.groocraft.couchdb.slacker.annotation.Index;
import com.groocraft.couchdb.slacker.exception.CouchDbRuntimeException;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings({"null", "cast", "unused"})
class CouchDbParsingQueryTest {

    @Test
    @SuppressWarnings({"unchecked", "ResultOfMethodCallIgnored", "null"})
    public void testNoProjectionWithStringParameter() throws IOException {
        CouchDbClient client = mock(CouchDbClient.class);
        QueryMethod queryMethod = mock(QueryMethod.class);
        ResultProcessor resultProcessor = mock(ResultProcessor.class);
        ReturnedType returnedType = mock(ReturnedType.class);
        Method method = mock(Method.class);
        Parameter parameter = mock(Parameter.class);
        Parameters<?, ?> parameters = mock(Parameters.class);

        when(queryMethod.getName()).thenReturn("findByValue");
        when(client.find(any(), any())).thenReturn(new ArrayList<>());
        doReturn(TestDocument.class).when(queryMethod).getReturnedObjectType();
        when(queryMethod.getResultProcessor()).thenReturn(resultProcessor);
        when(resultProcessor.getReturnedType()).thenReturn(returnedType);
        doReturn(TestDocument.class).when(returnedType).getDomainType();
        when(method.getAnnotation(Index.class)).thenReturn(null);
        when(parameter.getName()).thenReturn(Optional.of("value"));
        when(parameter.getIndex()).thenReturn(0);
        doReturn(String.class).when(parameter).getType();
        doReturn(List.of(parameter).iterator()).when(parameters).iterator();
        doReturn(parameters).when(queryMethod).getParameters();
        when(parameters.getSortIndex()).thenReturn(-1);
        when(parameters.getPageableIndex()).thenReturn(-1);
        TestDocument result = new TestDocument();
        when(client.find(any(), eq(TestDocument.class))).thenReturn(List.of(result)).thenThrow(new IOException("error"));

        CouchDbParsingQuery query = new CouchDbParsingQuery(client, method, queryMethod, TestDocument.class);
        assertEquals(queryMethod, query.getQueryMethod(), "CouchDbDirectQuery do not remember given queryMethod");
        Object o = query.execute(new Object[]{"test"});
        verify(client).find("{\"selector\":{\"$or\":[{\"value\":{\"$eq\":\"test\"}}]}}", TestDocument.class);
        assertDoesNotThrow(() -> (List<TestDocument>) o, "Result must be list of documents");
        assertNotNull(o, "Returned list must not be null");
        assertEquals(1, ((List<TestDocument>) o).size(), "Query should not alternate result in this case");
        assertEquals(result, ((List<TestDocument>) o).get(0), "Query should not alternate result in this case");
        CouchDbRuntimeException ex = assertThrows(CouchDbRuntimeException.class, () -> query.execute(new Object[]{"test"}), "Every thrown exception must be reported");
        assertEquals("error", ex.getCause().getMessage(), "Repository must pass original cause of exceptional state");
    }

    @Test
    @SuppressWarnings({"unchecked", "ResultOfMethodCallIgnored", "null"})
    public void testNoProjectionWithNonStringParameter() throws IOException {
        CouchDbClient client = mock(CouchDbClient.class);
        QueryMethod queryMethod = mock(QueryMethod.class);
        ResultProcessor resultProcessor = mock(ResultProcessor.class);
        ReturnedType returnedType = mock(ReturnedType.class);
        Method method = mock(Method.class);
        Parameter parameter = mock(Parameter.class);
        Parameters<?, ?> parameters = mock(Parameters.class);

        when(queryMethod.getName()).thenReturn("findByValue");
        when(client.find(any(), any())).thenReturn(new ArrayList<>());
        doReturn(TestDocument.class).when(queryMethod).getReturnedObjectType();
        when(queryMethod.getResultProcessor()).thenReturn(resultProcessor);
        when(resultProcessor.getReturnedType()).thenReturn(returnedType);
        doReturn(TestDocument.class).when(returnedType).getDomainType();
        when(method.getAnnotation(Index.class)).thenReturn(null);
        when(parameter.getName()).thenReturn(Optional.of("value"));
        when(parameter.getIndex()).thenReturn(0);
        doReturn(Integer.class).when(parameter).getType();
        doReturn(List.of(parameter).iterator()).when(parameters).iterator();
        doReturn(parameters).when(queryMethod).getParameters();
        when(parameters.getSortIndex()).thenReturn(-1);
        when(parameters.getPageableIndex()).thenReturn(-1);
        TestDocument result = new TestDocument();
        when(client.find(any(), eq(TestDocument.class))).thenReturn(List.of(result)).thenThrow(new IOException("error"));

        CouchDbParsingQuery query = new CouchDbParsingQuery(client, method, queryMethod, TestDocument.class);
        assertEquals(queryMethod, query.getQueryMethod(), "CouchDbDirectQuery do not remember given queryMethod");
        Object o = query.execute(new Object[]{1});
        verify(client).find("{\"selector\":{\"$or\":[{\"value\":{\"$eq\":1}}]}}", TestDocument.class);
        assertDoesNotThrow(() -> (List<TestDocument>) o, "Result must be list of documents");
        assertNotNull(o, "Returned list must not be null");
        assertEquals(1, ((List<TestDocument>) o).size(), "Query should not alternate result in this case");
        assertEquals(result, ((List<TestDocument>) o).get(0), "Query should not alternate result in this case");
        CouchDbRuntimeException ex = assertThrows(CouchDbRuntimeException.class, () -> query.execute(new Object[]{"test"}), "Every thrown exception must be reported");
        assertEquals("error", ex.getCause().getMessage(), "Repository must pass original cause of exceptional state");
    }

    @Test
    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    public void testCountProjection() throws IOException {
        CouchDbClient client = mock(CouchDbClient.class);
        QueryMethod queryMethod = mock(QueryMethod.class);
        ResultProcessor resultProcessor = mock(ResultProcessor.class);
        ReturnedType returnedType = mock(ReturnedType.class);
        Method method = mock(Method.class);
        Parameter parameter = mock(Parameter.class);
        Parameters<?, ?> parameters = mock(Parameters.class);

        when(queryMethod.getName()).thenReturn("countByValue");
        when(client.find(any(), any())).thenReturn(new ArrayList<>());
        doReturn(TestDocument.class).when(queryMethod).getReturnedObjectType();
        when(queryMethod.getResultProcessor()).thenReturn(resultProcessor);
        when(resultProcessor.getReturnedType()).thenReturn(returnedType);
        doReturn(TestDocument.class).when(returnedType).getDomainType();
        when(method.getAnnotation(Index.class)).thenReturn(null);
        when(parameter.getName()).thenReturn(Optional.of("value"));
        when(parameter.getIndex()).thenReturn(0);
        doReturn(String.class).when(parameter).getType();
        doReturn(List.of(parameter).iterator()).when(parameters).iterator();
        doReturn(parameters).when(queryMethod).getParameters();
        when(parameters.getSortIndex()).thenReturn(-1);
        when(parameters.getPageableIndex()).thenReturn(-1);
        when(client.find(any(), eq(TestDocument.class))).thenReturn(List.of(new TestDocument()));

        CouchDbParsingQuery query = new CouchDbParsingQuery(client, method, queryMethod, TestDocument.class);
        assertEquals(queryMethod, query.getQueryMethod(), "CouchDbDirectQuery do not remember given queryMethod");
        Object o = query.execute(new Object[]{"test"});
        verify(client).find("{\"selector\":{\"$or\":[{\"value\":{\"$eq\":\"test\"}}]}}", TestDocument.class);
        assertDoesNotThrow(() -> (Integer) o, "Result must be number if count projection is configured");
        assertEquals(1, (Integer) o, "Find return list of one, so result should be one");
    }

    @Test
    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    public void testDeleteProjection() throws IOException {
        CouchDbClient client = mock(CouchDbClient.class);
        QueryMethod queryMethod = mock(QueryMethod.class);
        ResultProcessor resultProcessor = mock(ResultProcessor.class);
        ReturnedType returnedType = mock(ReturnedType.class);
        Method method = mock(Method.class);
        Parameter parameter = mock(Parameter.class);
        Parameters<?, ?> parameters = mock(Parameters.class);

        when(queryMethod.getName()).thenReturn("deleteByValue");
        when(client.find(any(), any())).thenReturn(new ArrayList<>());
        doReturn(TestDocument.class).when(queryMethod).getReturnedObjectType();
        when(queryMethod.getResultProcessor()).thenReturn(resultProcessor);
        when(resultProcessor.getReturnedType()).thenReturn(returnedType);
        doReturn(TestDocument.class).when(returnedType).getDomainType();
        when(method.getAnnotation(Index.class)).thenReturn(null);
        when(parameter.getName()).thenReturn(Optional.of("value"));
        when(parameter.getIndex()).thenReturn(0);
        doReturn(String.class).when(parameter).getType();
        doReturn(List.of(parameter).iterator()).when(parameters).iterator();
        doReturn(parameters).when(queryMethod).getParameters();
        when(parameters.getSortIndex()).thenReturn(-1);
        when(parameters.getPageableIndex()).thenReturn(-1);
        List<TestDocument> result = List.of(new TestDocument());
        when(client.find(any(), eq(TestDocument.class))).thenReturn(result);

        CouchDbParsingQuery query = new CouchDbParsingQuery(client, method, queryMethod, TestDocument.class);
        assertEquals(queryMethod, query.getQueryMethod(), "CouchDbDirectQuery do not remember given queryMethod");
        query.execute(new Object[]{"test"});
        verify(client).find("{\"selector\":{\"$or\":[{\"value\":{\"$eq\":\"test\"}}]}}", TestDocument.class);
        verify(client, atLeastOnce().description("Every document find by given rules must be deleted")).deleteAll(result, TestDocument.class);
    }

    @Test
    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    public void testExistsProjection() throws IOException {
        CouchDbClient client = mock(CouchDbClient.class);
        QueryMethod queryMethod = mock(QueryMethod.class);
        ResultProcessor resultProcessor = mock(ResultProcessor.class);
        ReturnedType returnedType = mock(ReturnedType.class);
        Method method = mock(Method.class);
        Parameter parameter = mock(Parameter.class);
        Parameters<?, ?> parameters = mock(Parameters.class);

        when(queryMethod.getName()).thenReturn("existsByValue");
        when(client.find(any(), any())).thenReturn(new ArrayList<>());
        doReturn(TestDocument.class).when(queryMethod).getReturnedObjectType();
        when(queryMethod.getResultProcessor()).thenReturn(resultProcessor);
        when(resultProcessor.getReturnedType()).thenReturn(returnedType);
        doReturn(TestDocument.class).when(returnedType).getDomainType();
        when(method.getAnnotation(Index.class)).thenReturn(null);
        when(parameter.getName()).thenReturn(Optional.of("value"));
        when(parameter.getIndex()).thenReturn(0);
        doReturn(String.class).when(parameter).getType();
        doReturn(List.of(parameter).iterator()).when(parameters).iterator();
        doReturn(parameters).when(queryMethod).getParameters();
        when(parameters.getSortIndex()).thenReturn(-1);
        when(parameters.getPageableIndex()).thenReturn(-1);
        when(client.find(any(), eq(TestDocument.class))).thenReturn(List.of(new TestDocument()));

        CouchDbParsingQuery query = new CouchDbParsingQuery(client, method, queryMethod, TestDocument.class);
        assertEquals(queryMethod, query.getQueryMethod(), "CouchDbDirectQuery do not remember given queryMethod");
        Object o = query.execute(new Object[]{"test"});
        verify(client).find("{\"selector\":{\"$or\":[{\"value\":{\"$eq\":\"test\"}}]}}", TestDocument.class);
        assertDoesNotThrow(() -> (Boolean) o, "Result must be boolean if delete projection is configured");
        assertEquals(true, o, "Find return list of one, so result should be true");
    }

    @Test
    @SuppressWarnings({"unchecked", "ResultOfMethodCallIgnored", "null"})
    public void testIndex() throws IOException {
        CouchDbClient client = mock(CouchDbClient.class);
        QueryMethod queryMethod = mock(QueryMethod.class);
        ResultProcessor resultProcessor = mock(ResultProcessor.class);
        ReturnedType returnedType = mock(ReturnedType.class);
        Method method = mock(Method.class);
        Parameter parameter = mock(Parameter.class);
        Parameters<?, ?> parameters = mock(Parameters.class);
        Index index = mock(Index.class);

        when(queryMethod.getName()).thenReturn("findByValue");
        when(client.find(any(), any())).thenReturn(new ArrayList<>());
        doReturn(TestDocument.class).when(queryMethod).getReturnedObjectType();
        when(queryMethod.getResultProcessor()).thenReturn(resultProcessor);
        when(resultProcessor.getReturnedType()).thenReturn(returnedType);
        doReturn(TestDocument.class).when(returnedType).getDomainType();
        when(method.getAnnotation(Index.class)).thenReturn(index);
        when(index.value()).thenReturn(new String[]{"test"});
        when(parameter.getName()).thenReturn(Optional.of("value"));
        when(parameter.getIndex()).thenReturn(0);
        doReturn(String.class).when(parameter).getType();
        doReturn(List.of(parameter).iterator()).when(parameters).iterator();
        doReturn(parameters).when(queryMethod).getParameters();
        when(parameters.getSortIndex()).thenReturn(-1);
        when(parameters.getPageableIndex()).thenReturn(-1);
        TestDocument result = new TestDocument();
        when(client.find(any(), eq(TestDocument.class))).thenReturn(List.of(result));

        CouchDbParsingQuery query = new CouchDbParsingQuery(client, method, queryMethod, TestDocument.class);
        assertEquals(queryMethod, query.getQueryMethod(), "CouchDbDirectQuery do not remember given queryMethod");
        Object o = query.execute(new Object[]{"test"});
        verify(client).find("{\"use_index\":[\"test\"],\"selector\":{\"$or\":[{\"value\":{\"$eq\":\"test\"}}]}}", TestDocument.class);
        assertDoesNotThrow(() -> (List<TestDocument>) o, "Result must be list of documents");
        assertNotNull(o, "Returned list must not be null");
        assertEquals(1, ((List<TestDocument>) o).size(), "Query should not alternate result in this case");
        assertEquals(result, ((List<TestDocument>) o).get(0), "Query should not alternate result in this case");
    }

    @Test
    @SuppressWarnings({"unchecked", "ResultOfMethodCallIgnored", "null"})
    public void testPaging() throws IOException {
        CouchDbClient client = mock(CouchDbClient.class);
        QueryMethod queryMethod = mock(QueryMethod.class);
        ResultProcessor resultProcessor = mock(ResultProcessor.class);
        ReturnedType returnedType = mock(ReturnedType.class);
        Method method = mock(Method.class);
        Parameter valueParameter = mock(Parameter.class);
        Parameter pageableParameter = mock(Parameter.class);
        Parameters<?, ?> parameters = mock(Parameters.class);

        when(queryMethod.getName()).thenReturn("findByValue");
        when(client.find(any(), any())).thenReturn(new ArrayList<>());
        doReturn(TestDocument.class).when(queryMethod).getReturnedObjectType();
        when(queryMethod.getResultProcessor()).thenReturn(resultProcessor);
        when(resultProcessor.getReturnedType()).thenReturn(returnedType);
        doReturn(TestDocument.class).when(returnedType).getDomainType();
        when(pageableParameter.getIndex()).thenReturn(1);
        when(pageableParameter.isSpecialParameter()).thenReturn(true);
        when(valueParameter.getName()).thenReturn(Optional.of("value"));
        when(valueParameter.getIndex()).thenReturn(0);
        doReturn(String.class).when(valueParameter).getType();
        doReturn(List.of(valueParameter, pageableParameter).iterator()).when(parameters).iterator();
        doReturn(parameters).when(queryMethod).getParameters();
        when(parameters.getSortIndex()).thenReturn(-1);
        when(parameters.getPageableIndex()).thenReturn(1);
        TestDocument result = new TestDocument();
        when(client.find(any(), eq(TestDocument.class))).thenReturn(List.of(result));

        CouchDbParsingQuery query = new CouchDbParsingQuery(client, method, queryMethod, TestDocument.class);
        assertEquals(queryMethod, query.getQueryMethod(), "CouchDbDirectQuery do not remember given queryMethod");
        Object o = query.execute(new Object[]{"test", PageRequest.of(5, 20, Sort.by("value"))});
        verify(client).find("{\"limit\":20,\"skip\":100,\"sort\":[{\"value\":\"asc\"}],\"selector\":{\"$or\":[{\"value\":{\"$eq\":\"test\"}}]}}", TestDocument.class);
        assertDoesNotThrow(() -> (List<TestDocument>) o, "Result must be list of documents");
        assertNotNull(o, "Returned list must not be null");
        assertEquals(1, ((List<TestDocument>) o).size(), "Query should not alternate result in this case");
        assertEquals(result, ((List<TestDocument>) o).get(0), "Query should not alternate result in this case");
    }

}