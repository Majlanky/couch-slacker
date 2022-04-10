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

package com.groocraft.couchdb.slacker.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.groocraft.couchdb.slacker.CouchDbClient;
import com.groocraft.couchdb.slacker.DocumentDescriptor;
import com.groocraft.couchdb.slacker.EntityMetadata;
import com.groocraft.couchdb.slacker.annotation.Index;
import com.groocraft.couchdb.slacker.exception.CouchDbRuntimeException;
import com.groocraft.couchdb.slacker.structure.DocumentFindRequest;
import com.groocraft.couchdb.slacker.structure.FindResult;
import com.groocraft.couchdb.slacker.test.integration.TestDocument;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Parameter;
import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.ResultProcessor;
import org.springframework.data.repository.query.ReturnedType;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings({"null", "cast"})
class CouchDbParsingQueryTest {

    @Test
    @SuppressWarnings({"unchecked", "null"})
    void testNoProjectionWithStringParameter() throws IOException {
        ArgumentCaptor<DocumentFindRequest> captor = ArgumentCaptor.forClass(DocumentFindRequest.class);
        CouchDbClient client = mock(CouchDbClient.class);
        QueryMethod queryMethod = mock(QueryMethod.class);
        ResultProcessor resultProcessor = mock(ResultProcessor.class);
        ReturnedType returnedType = mock(ReturnedType.class);
        Method method = mock(Method.class);
        Parameter parameter = mock(Parameter.class);
        Parameters<?, ?> parameters = mock(Parameters.class);

        when(queryMethod.getName()).thenReturn("findByValue");
        when(client.getEntityMetadata(TestDocument.class)).thenReturn(new EntityMetadata(DocumentDescriptor.of(TestDocument.class)));
        when(client.find(captor.capture(), eq(TestDocument.class))).
                thenReturn(FindResult.of(Collections.singletonList(new TestDocument()), Collections.singletonMap(1, ""))).
                thenThrow(new IOException("error"));
        doReturn(TestDocument.class).when(queryMethod).getReturnedObjectType();
        when(queryMethod.getResultProcessor()).thenReturn(resultProcessor);
        when(resultProcessor.getReturnedType()).thenReturn(returnedType);
        doReturn(TestDocument.class).when(returnedType).getDomainType();
        when(method.getAnnotation(Index.class)).thenReturn(null);
        when(parameter.getName()).thenReturn(Optional.of("value"));
        when(parameter.getIndex()).thenReturn(0);
        doReturn(String.class).when(parameter).getType();
        doReturn(Collections.singletonList(parameter).iterator()).when(parameters).iterator();
        doReturn(parameters).when(queryMethod).getParameters();
        when(parameters.getSortIndex()).thenReturn(-1);
        when(parameters.getPageableIndex()).thenReturn(-1);
        TestDocument result = new TestDocument();

        CouchDbParsingQuery<TestDocument> query = new CouchDbParsingQuery<>(client, false, method, queryMethod, TestDocument.class);
        assertEquals(queryMethod, query.getQueryMethod(), "CouchDbDirectQuery do not remember given queryMethod");
        Object o = query.execute(new Object[]{"test"});
        DocumentFindRequest r = captor.getValue();
        r.setLimit(10);
        assertEquals("{\"limit\":10,\"selector\":{\"$or\":[{\"value\":{\"$eq\":\"test\"}}]}}",
                new ObjectMapper().writeValueAsString(r),
                "Request it wrongly initialized");
        assertDoesNotThrow(() -> (List<TestDocument>) o, "Result must be list of documents");
        assertNotNull(o, "Returned list must not be null");
        assertEquals(1, ((List<TestDocument>) o).size(), "Query should not alternate result in this case");
        assertEquals(result, ((List<TestDocument>) o).get(0), "Query should not alternate result in this case");
        CouchDbRuntimeException ex = assertThrows(CouchDbRuntimeException.class, () -> query.execute(new Object[]{"test"}), "Every thrown exception must be reported");
        assertEquals("error", ex.getCause().getMessage(), "Repository must pass original cause of exceptional state");
    }

    @Test
    @SuppressWarnings({"unchecked", "null"})
    void testNoProjectionWithNonStringParameter() throws IOException {
        ArgumentCaptor<DocumentFindRequest> captor = ArgumentCaptor.forClass(DocumentFindRequest.class);
        CouchDbClient client = mock(CouchDbClient.class);
        QueryMethod queryMethod = mock(QueryMethod.class);
        ResultProcessor resultProcessor = mock(ResultProcessor.class);
        ReturnedType returnedType = mock(ReturnedType.class);
        Method method = mock(Method.class);
        Parameter parameter = mock(Parameter.class);
        Parameters<?, ?> parameters = mock(Parameters.class);

        when(queryMethod.getName()).thenReturn("findByValue");
        when(client.getEntityMetadata(TestDocument.class)).thenReturn(new EntityMetadata(DocumentDescriptor.of(TestDocument.class)));
        when(client.find(captor.capture(), eq(TestDocument.class))).
                thenReturn(FindResult.of(Collections.singletonList(new TestDocument()), Collections.singletonMap(1, ""))).
                thenThrow(new IOException("error"));
        doReturn(TestDocument.class).when(queryMethod).getReturnedObjectType();
        when(queryMethod.getResultProcessor()).thenReturn(resultProcessor);
        when(resultProcessor.getReturnedType()).thenReturn(returnedType);
        doReturn(TestDocument.class).when(returnedType).getDomainType();
        when(method.getAnnotation(Index.class)).thenReturn(null);
        when(parameter.getName()).thenReturn(Optional.of("value"));
        when(parameter.getIndex()).thenReturn(0);
        doReturn(Integer.class).when(parameter).getType();
        doReturn(Collections.singletonList(parameter).iterator()).when(parameters).iterator();
        doReturn(parameters).when(queryMethod).getParameters();
        when(parameters.getSortIndex()).thenReturn(-1);
        when(parameters.getPageableIndex()).thenReturn(-1);
        TestDocument result = new TestDocument();

        CouchDbParsingQuery<TestDocument> query = new CouchDbParsingQuery<>(client, false, method, queryMethod, TestDocument.class);
        assertEquals(queryMethod, query.getQueryMethod(), "CouchDbDirectQuery do not remember given queryMethod");
        Object o = query.execute(new Object[]{1});
        DocumentFindRequest r = captor.getValue();
        r.setLimit(10);
        assertEquals("{\"limit\":10,\"selector\":{\"$or\":[{\"value\":{\"$eq\":1}}]}}",
                new ObjectMapper().writeValueAsString(r),
                "Request it wrongly initialized");
        assertDoesNotThrow(() -> (List<TestDocument>) o, "Result must be list of documents");
        assertNotNull(o, "Returned list must not be null");
        assertEquals(1, ((List<TestDocument>) o).size(), "Query should not alternate result in this case");
        assertEquals(result, ((List<TestDocument>) o).get(0), "Query should not alternate result in this case");
        CouchDbRuntimeException ex = assertThrows(CouchDbRuntimeException.class, () -> query.execute(new Object[]{"test"}), "Every thrown exception must be reported");
        assertEquals("error", ex.getCause().getMessage(), "Repository must pass original cause of exceptional state");
    }

    @Test
    void testCountProjection() throws IOException {
        ArgumentCaptor<DocumentFindRequest> captor = ArgumentCaptor.forClass(DocumentFindRequest.class);
        CouchDbClient client = mock(CouchDbClient.class);
        QueryMethod queryMethod = mock(QueryMethod.class);
        ResultProcessor resultProcessor = mock(ResultProcessor.class);
        ReturnedType returnedType = mock(ReturnedType.class);
        Method method = mock(Method.class);
        Parameter parameter = mock(Parameter.class);
        Parameters<?, ?> parameters = mock(Parameters.class);

        when(queryMethod.getName()).thenReturn("countByValue");
        when(client.getEntityMetadata(TestDocument.class)).thenReturn(new EntityMetadata(DocumentDescriptor.of(TestDocument.class)));
        when(client.find(captor.capture(), eq(TestDocument.class))).
                thenReturn(FindResult.of(Collections.singletonList(new TestDocument()), Collections.singletonMap(1, ""))).
                thenThrow(new IOException("error"));
        doReturn(TestDocument.class).when(queryMethod).getReturnedObjectType();
        when(queryMethod.getResultProcessor()).thenReturn(resultProcessor);
        when(resultProcessor.getReturnedType()).thenReturn(returnedType);
        doReturn(TestDocument.class).when(returnedType).getDomainType();
        when(method.getAnnotation(Index.class)).thenReturn(null);
        when(parameter.getName()).thenReturn(Optional.of("value"));
        when(parameter.getIndex()).thenReturn(0);
        doReturn(String.class).when(parameter).getType();
        doReturn(Collections.singletonList(parameter).iterator()).when(parameters).iterator();
        doReturn(parameters).when(queryMethod).getParameters();
        when(parameters.getSortIndex()).thenReturn(-1);
        when(parameters.getPageableIndex()).thenReturn(-1);

        CouchDbParsingQuery<TestDocument> query = new CouchDbParsingQuery<>(client, false, method, queryMethod, TestDocument.class);
        assertEquals(queryMethod, query.getQueryMethod(), "CouchDbDirectQuery do not remember given queryMethod");
        Object o = query.execute(new Object[]{"test"});
        DocumentFindRequest r = captor.getValue();
        r.setLimit(10);
        assertEquals("{\"limit\":10,\"selector\":{\"$or\":[{\"value\":{\"$eq\":\"test\"}}]}}",
                new ObjectMapper().writeValueAsString(r),
                "Request it wrongly initialized");
        assertDoesNotThrow(() -> (Integer) o, "Result must be number if count projection is configured");
        assertEquals(1, (Integer) o, "Find return list of one, so result should be one");
    }

    @Test
    void testDeleteProjection() throws IOException {
        ArgumentCaptor<DocumentFindRequest> captor = ArgumentCaptor.forClass(DocumentFindRequest.class);
        CouchDbClient client = mock(CouchDbClient.class);
        QueryMethod queryMethod = mock(QueryMethod.class);
        ResultProcessor resultProcessor = mock(ResultProcessor.class);
        ReturnedType returnedType = mock(ReturnedType.class);
        Method method = mock(Method.class);
        Parameter parameter = mock(Parameter.class);
        Parameters<?, ?> parameters = mock(Parameters.class);

        when(queryMethod.getName()).thenReturn("deleteByValue");
        when(client.getEntityMetadata(TestDocument.class)).thenReturn(new EntityMetadata(DocumentDescriptor.of(TestDocument.class)));
        when(client.find(captor.capture(), eq(TestDocument.class))).
                thenReturn(FindResult.of(Collections.singletonList(new TestDocument()), Collections.singletonMap(1, ""))).
                thenThrow(new IOException("error"));
        doReturn(TestDocument.class).when(queryMethod).getReturnedObjectType();
        when(queryMethod.getResultProcessor()).thenReturn(resultProcessor);
        when(resultProcessor.getReturnedType()).thenReturn(returnedType);
        doReturn(TestDocument.class).when(returnedType).getDomainType();
        when(method.getAnnotation(Index.class)).thenReturn(null);
        when(parameter.getName()).thenReturn(Optional.of("value"));
        when(parameter.getIndex()).thenReturn(0);
        doReturn(String.class).when(parameter).getType();
        doReturn(Collections.singletonList(parameter).iterator()).when(parameters).iterator();
        doReturn(parameters).when(queryMethod).getParameters();
        when(parameters.getSortIndex()).thenReturn(-1);
        when(parameters.getPageableIndex()).thenReturn(-1);
        List<TestDocument> result = Collections.singletonList(new TestDocument());

        CouchDbParsingQuery<TestDocument> query = new CouchDbParsingQuery<>(client, false, method, queryMethod, TestDocument.class);
        assertEquals(queryMethod, query.getQueryMethod(), "CouchDbDirectQuery do not remember given queryMethod");
        query.execute(new Object[]{"test"});
        DocumentFindRequest r = captor.getValue();
        r.setLimit(10);
        assertEquals("{\"limit\":10,\"selector\":{\"$or\":[{\"value\":{\"$eq\":\"test\"}}]}}",
                new ObjectMapper().writeValueAsString(r),
                "Request it wrongly initialized");
        verify(client, atLeastOnce().description("Every document find by given rules must be deleted")).deleteAll(result, TestDocument.class);
    }

    @Test
    void testExistsProjection() throws IOException {
        ArgumentCaptor<DocumentFindRequest> captor = ArgumentCaptor.forClass(DocumentFindRequest.class);
        CouchDbClient client = mock(CouchDbClient.class);
        QueryMethod queryMethod = mock(QueryMethod.class);
        ResultProcessor resultProcessor = mock(ResultProcessor.class);
        ReturnedType returnedType = mock(ReturnedType.class);
        Method method = mock(Method.class);
        Parameter parameter = mock(Parameter.class);
        Parameters<?, ?> parameters = mock(Parameters.class);

        when(queryMethod.getName()).thenReturn("existsByValue");
        when(client.getEntityMetadata(TestDocument.class)).thenReturn(new EntityMetadata(DocumentDescriptor.of(TestDocument.class)));
        when(client.find(captor.capture(), eq(TestDocument.class))).
                thenReturn(FindResult.of(Collections.singletonList(new TestDocument()), Collections.singletonMap(1, ""))).
                thenThrow(new IOException("error"));
        doReturn(TestDocument.class).when(queryMethod).getReturnedObjectType();
        when(queryMethod.getResultProcessor()).thenReturn(resultProcessor);
        when(resultProcessor.getReturnedType()).thenReturn(returnedType);
        doReturn(TestDocument.class).when(returnedType).getDomainType();
        when(method.getAnnotation(Index.class)).thenReturn(null);
        when(parameter.getName()).thenReturn(Optional.of("value"));
        when(parameter.getIndex()).thenReturn(0);
        doReturn(String.class).when(parameter).getType();
        doReturn(Collections.singletonList(parameter).iterator()).when(parameters).iterator();
        doReturn(parameters).when(queryMethod).getParameters();
        when(parameters.getSortIndex()).thenReturn(-1);
        when(parameters.getPageableIndex()).thenReturn(-1);

        CouchDbParsingQuery<TestDocument> query = new CouchDbParsingQuery<>(client, false, method, queryMethod, TestDocument.class);
        assertEquals(queryMethod, query.getQueryMethod(), "CouchDbDirectQuery do not remember given queryMethod");
        Object o = query.execute(new Object[]{"test"});
        DocumentFindRequest r = captor.getValue();
        r.setLimit(10);
        assertEquals("{\"limit\":10,\"selector\":{\"$or\":[{\"value\":{\"$eq\":\"test\"}}]}}",
                new ObjectMapper().writeValueAsString(r),
                "Request it wrongly initialized");
        assertDoesNotThrow(() -> (Boolean) o, "Result must be boolean if delete projection is configured");
        assertEquals(true, o, "Find return list of one, so result should be true");
    }

    @Test
    @SuppressWarnings({"unchecked", "null"})
    void testIndex() throws IOException {
        ArgumentCaptor<DocumentFindRequest> captor = ArgumentCaptor.forClass(DocumentFindRequest.class);
        CouchDbClient client = mock(CouchDbClient.class);
        QueryMethod queryMethod = mock(QueryMethod.class);
        ResultProcessor resultProcessor = mock(ResultProcessor.class);
        ReturnedType returnedType = mock(ReturnedType.class);
        Method method = mock(Method.class);
        Parameter parameter = mock(Parameter.class);
        Parameters<?, ?> parameters = mock(Parameters.class);
        Index index = mock(Index.class);

        when(queryMethod.getName()).thenReturn("findByValue");
        when(client.getEntityMetadata(TestDocument.class)).thenReturn(new EntityMetadata(DocumentDescriptor.of(TestDocument.class)));
        when(client.find(captor.capture(), eq(TestDocument.class))).
                thenReturn(FindResult.of(Collections.singletonList(new TestDocument()), Collections.singletonMap(1, ""))).
                thenThrow(new IOException("error"));
        doReturn(TestDocument.class).when(queryMethod).getReturnedObjectType();
        when(queryMethod.getResultProcessor()).thenReturn(resultProcessor);
        when(resultProcessor.getReturnedType()).thenReturn(returnedType);
        doReturn(TestDocument.class).when(returnedType).getDomainType();
        when(method.getAnnotation(Index.class)).thenReturn(index);
        when(index.value()).thenReturn(new String[]{"test"});
        when(parameter.getName()).thenReturn(Optional.of("value"));
        when(parameter.getIndex()).thenReturn(0);
        doReturn(String.class).when(parameter).getType();
        doReturn(Collections.singletonList(parameter).iterator()).when(parameters).iterator();
        doReturn(parameters).when(queryMethod).getParameters();
        when(parameters.getSortIndex()).thenReturn(-1);
        when(parameters.getPageableIndex()).thenReturn(-1);
        TestDocument result = new TestDocument();

        CouchDbParsingQuery<TestDocument> query = new CouchDbParsingQuery<>(client, false, method, queryMethod, TestDocument.class);
        assertEquals(queryMethod, query.getQueryMethod(), "CouchDbDirectQuery do not remember given queryMethod");
        Object o = query.execute(new Object[]{"test"});
        DocumentFindRequest r = captor.getValue();
        r.setLimit(10);
        assertEquals("{\"use_index\":[\"test\"],\"limit\":10,\"selector\":{\"$or\":[{\"value\":{\"$eq\":\"test\"}}]}}",
                new ObjectMapper().writeValueAsString(r),
                "Request it wrongly initialized");
        assertDoesNotThrow(() -> (List<TestDocument>) o, "Result must be list of documents");
        assertNotNull(o, "Returned list must not be null");
        assertEquals(1, ((List<TestDocument>) o).size(), "Query should not alternate result in this case");
        assertEquals(result, ((List<TestDocument>) o).get(0), "Query should not alternate result in this case");
    }

    @Test
    @SuppressWarnings({"unchecked", "null"})
    void testPaging() throws IOException {
        TestDocument result = new TestDocument();
        ArgumentCaptor<DocumentFindRequest> captor = ArgumentCaptor.forClass(DocumentFindRequest.class);
        CouchDbClient client = mock(CouchDbClient.class);
        QueryMethod queryMethod = mock(QueryMethod.class);
        ResultProcessor resultProcessor = mock(ResultProcessor.class);
        ReturnedType returnedType = mock(ReturnedType.class);
        Method method = mock(Method.class);
        Parameter valueParameter = mock(Parameter.class);
        Parameter pageableParameter = mock(Parameter.class);
        Parameters<?, ?> parameters = mock(Parameters.class);

        when(queryMethod.getName()).thenReturn("findByValue");
        when(client.getEntityMetadata(TestDocument.class)).thenReturn(new EntityMetadata(DocumentDescriptor.of(TestDocument.class)));
        when(client.find(captor.capture(), eq(TestDocument.class))).
                thenReturn(FindResult.of(Collections.singletonList(new TestDocument()), Collections.singletonMap(1, ""))).
                thenThrow(new IOException("error"));
        doReturn(TestDocument.class).when(queryMethod).getReturnedObjectType();
        when(queryMethod.getResultProcessor()).thenReturn(resultProcessor);
        when(resultProcessor.getReturnedType()).thenReturn(returnedType);
        doReturn(TestDocument.class).when(returnedType).getDomainType();
        when(pageableParameter.getIndex()).thenReturn(1);
        when(pageableParameter.isSpecialParameter()).thenReturn(true);
        when(valueParameter.getName()).thenReturn(Optional.of("value"));
        when(valueParameter.getIndex()).thenReturn(0);
        doReturn(String.class).when(valueParameter).getType();
        doReturn(Arrays.asList(valueParameter, pageableParameter).iterator()).when(parameters).iterator();
        doReturn(parameters).when(queryMethod).getParameters();
        when(parameters.getSortIndex()).thenReturn(-1);
        when(parameters.getPageableIndex()).thenReturn(1);

        CouchDbParsingQuery<TestDocument> query = new CouchDbParsingQuery<>(client, false, method, queryMethod, TestDocument.class);
        assertEquals(queryMethod, query.getQueryMethod(), "CouchDbDirectQuery do not remember given queryMethod");
        Object o = query.execute(new Object[]{"test", PageRequest.of(5, 20, Sort.by("value"))});
        DocumentFindRequest r = captor.getValue();
        r.setLimit(10);
        assertEquals("{\"limit\":10,\"skip\":100,\"sort\":[{\"value\":\"asc\"}],\"selector\":{\"$or\":[{\"value\":{\"$eq\":\"test\"}}]}}",
                new ObjectMapper().writeValueAsString(r),
                "Request it wrongly initialized");
        assertDoesNotThrow(() -> (List<TestDocument>) o, "Result must be list of documents");
        assertNotNull(o, "Returned list must not be null");
        assertEquals(1, ((List<TestDocument>) o).size(), "Query should not alternate result in this case");
        assertEquals(result, ((List<TestDocument>) o).get(0), "Query should not alternate result in this case");
    }

    @Test
    @SuppressWarnings({"unchecked", "null"})
    void testNoProjectionWithStringSubAttributeParameter() throws IOException {
        TestDocument result = new TestDocument();
        ArgumentCaptor<DocumentFindRequest> captor = ArgumentCaptor.forClass(DocumentFindRequest.class);
        CouchDbClient client = mock(CouchDbClient.class);
        QueryMethod queryMethod = mock(QueryMethod.class);
        ResultProcessor resultProcessor = mock(ResultProcessor.class);
        ReturnedType returnedType = mock(ReturnedType.class);
        Method method = mock(Method.class);
        Parameter parameter = mock(Parameter.class);
        Parameters<?, ?> parameters = mock(Parameters.class);

        when(queryMethod.getName()).thenReturn("findByAddressStreet");
        when(client.getEntityMetadata(TestDocument.class)).thenReturn(new EntityMetadata(DocumentDescriptor.of(TestDocument.class)));
        when(client.find(captor.capture(), eq(TestDocument.class))).
                thenReturn(FindResult.of(Collections.singletonList(new TestDocument()), Collections.singletonMap(1, ""))).
                thenThrow(new IOException("error"));
        doReturn(TestDocument.class).when(queryMethod).getReturnedObjectType();
        when(queryMethod.getResultProcessor()).thenReturn(resultProcessor);
        when(resultProcessor.getReturnedType()).thenReturn(returnedType);
        doReturn(TestDocument.class).when(returnedType).getDomainType();
        when(method.getAnnotation(Index.class)).thenReturn(null);
        when(parameter.getName()).thenReturn(Optional.of("street"));
        when(parameter.getIndex()).thenReturn(0);
        doReturn(String.class).when(parameter).getType();
        doReturn(Collections.singletonList(parameter).iterator()).when(parameters).iterator();
        doReturn(parameters).when(queryMethod).getParameters();
        when(parameters.getSortIndex()).thenReturn(-1);
        when(parameters.getPageableIndex()).thenReturn(-1);

        CouchDbParsingQuery<TestDocument> query = new CouchDbParsingQuery<>(client, false, method, queryMethod, TestDocument.class);
        assertEquals(queryMethod, query.getQueryMethod(), "CouchDbDirectQuery do not remember given queryMethod");
        Object o = query.execute(new Object[]{"test"});
        DocumentFindRequest r = captor.getValue();
        r.setLimit(10);
        assertEquals("{\"limit\":10,\"selector\":{\"$or\":[{\"address.street\":{\"$eq\":\"test\"}}]}}",
                new ObjectMapper().writeValueAsString(r),
                "Request it wrongly initialized");
        assertDoesNotThrow(() -> (List<TestDocument>) o, "Result must be list of documents");
        assertNotNull(o, "Returned list must not be null");
        assertEquals(1, ((List<TestDocument>) o).size(), "Query should not alternate result in this case");
        assertEquals(result, ((List<TestDocument>) o).get(0), "Query should not alternate result in this case");
        CouchDbRuntimeException ex = assertThrows(CouchDbRuntimeException.class, () -> query.execute(new Object[]{"test"}), "Every thrown exception must be reported");
        assertEquals("error", ex.getCause().getMessage(), "Repository must pass original cause of exceptional state");
    }

}