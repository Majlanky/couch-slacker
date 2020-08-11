package com.groocraft.couchdb.slacker.repository;

import com.groocraft.couchdb.slacker.CouchDbClient;
import com.groocraft.couchdb.slacker.TestDocument;
import com.groocraft.couchdb.slacker.exception.CouchDbRuntimeException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.repository.query.Parameter;
import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.QueryMethod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CouchDbDirectQueryTest {

    @Test
    public void testIndexedReplacement() throws IOException {
        CouchDbClient client = Mockito.mock(CouchDbClient.class);
        QueryMethod queryMethod = Mockito.mock(QueryMethod.class);
        Parameter parameter = Mockito.mock(Parameter.class);
        Parameters<?, ?> parameters = Mockito.mock(Parameters.class);
        CouchDbDirectQuery query = new CouchDbDirectQuery("{\"selector\": {\"value\": {\"$eq\": \"?0\"}}}", client, queryMethod, TestDocument.class);
        assertEquals(queryMethod, query.getQueryMethod(), "CouchDbDirectQuery do not remember given queryMethod");
        Mockito.when(client.find(Mockito.any(), Mockito.any())).thenReturn(new ArrayList<Object>());
        Mockito.doReturn(Object.class).when(queryMethod).getReturnedObjectType();
        Mockito.when(parameter.getName()).thenReturn(Optional.of("value"));
        Mockito.when(parameter.getIndex()).thenReturn(0);
        Mockito.doReturn(List.of(parameter).iterator()).when(parameters).iterator();
        Mockito.doReturn(parameters).when(queryMethod).getParameters();
        query.execute(new Object[]{"test"});
        Mockito.verify(client).find("{\"selector\": {\"value\": {\"$eq\": \"test\"}}}", TestDocument.class);
    }

    @Test
    public void testNamedReplacement() throws IOException {
        CouchDbClient client = Mockito.mock(CouchDbClient.class);
        QueryMethod queryMethod = Mockito.mock(QueryMethod.class);
        Parameter parameter = Mockito.mock(Parameter.class);
        Parameters<?, ?> parameters = Mockito.mock(Parameters.class);
        CouchDbDirectQuery query = new CouchDbDirectQuery("{\"selector\": {\"value\": {\"$eq\": \":value\"}}}", client, queryMethod, TestDocument.class);
        assertEquals(queryMethod, query.getQueryMethod(), "CouchDbDirectQuery do not remember given queryMethod");
        Mockito.when(client.find(Mockito.any(), Mockito.any())).thenReturn(new ArrayList<Object>());
        Mockito.doReturn(Object.class).when(queryMethod).getReturnedObjectType();
        Mockito.when(parameter.getName()).thenReturn(Optional.of("value"));
        Mockito.when(parameter.getIndex()).thenReturn(0);
        Mockito.doReturn(List.of(parameter).iterator()).when(parameters).iterator();
        Mockito.doReturn(parameters).when(queryMethod).getParameters();
        query.execute(new Object[]{"test"});
        Mockito.verify(client).find("{\"selector\": {\"value\": {\"$eq\": \"test\"}}}", TestDocument.class);
    }

    @Test
    public void testFindException() throws IOException {
        assertThrows(CouchDbRuntimeException.class, () -> {
            CouchDbClient client = Mockito.mock(CouchDbClient.class);
            QueryMethod queryMethod = Mockito.mock(QueryMethod.class);
            Parameter parameter = Mockito.mock(Parameter.class);
            Parameters<?, ?> parameters = Mockito.mock(Parameters.class);
            CouchDbDirectQuery query = new CouchDbDirectQuery("{\"selector\": {\"value\": {\"$eq\": \":value\"}}}", client, queryMethod, TestDocument.class);
            assertEquals(queryMethod, query.getQueryMethod(), "CouchDbDirectQuery do not remember given queryMethod");
            Mockito.when(client.find(Mockito.any(), Mockito.any())).thenThrow(new IOException());
            Mockito.doReturn(Object.class).when(queryMethod).getReturnedObjectType();
            Mockito.when(parameter.getName()).thenReturn(Optional.of("value"));
            Mockito.when(parameter.getIndex()).thenReturn(0);
            Mockito.doReturn(List.of(parameter).iterator()).when(parameters).iterator();
            Mockito.doReturn(parameters).when(queryMethod).getParameters();
            query.execute(new Object[]{"test"});
        }, "Any issue must be reported as " + CouchDbRuntimeException.class);
    }

}