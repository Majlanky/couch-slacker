/*
 * Copyright 2020 the original author or authors.
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

import com.groocraft.couchdb.slacker.CouchDbClient;
import com.groocraft.couchdb.slacker.EntityMetadata;
import com.groocraft.couchdb.slacker.TestDocument;
import com.groocraft.couchdb.slacker.annotation.ViewQuery;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Parameter;
import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.QueryMethod;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CouchDbViewQueryTest {

    @Test
    void testNoReducing() throws IOException {
        CouchDbClient client = mock(CouchDbClient.class);
        when(client.getEntityMetadata(TestDocument.class)).thenReturn(new EntityMetadata(TestDocument.class));
        ViewQuery viewQuery = mock(ViewQuery.class);
        when(viewQuery.reducing()).thenReturn(false);
        when(viewQuery.design()).thenReturn("testDes");
        when(viewQuery.view()).thenReturn("testView");
        QueryMethod queryMethod = mock(QueryMethod.class);
        when(queryMethod.isSliceQuery()).thenReturn(false);
        Parameters<?, ?> parameters = mock(Parameters.class);
        doReturn(Collections.emptyIterator()).when(parameters).iterator();
        when(parameters.getSortIndex()).thenReturn(-1);
        when(parameters.getPageableIndex()).thenReturn(-1);
        doReturn(parameters).when(queryMethod).getParameters();
        CouchDbViewQuery<TestDocument> query = new CouchDbViewQuery<>(client, viewQuery, queryMethod, TestDocument.class);
        query.execute(new Object[0]);
        verify(client).readFromView("test", "testDes", "testView", null, null, Sort.unsorted());
    }

    @Test
    void testNoReducingWithPageableAndSort() throws IOException {
        CouchDbClient client = mock(CouchDbClient.class);
        when(client.getEntityMetadata(TestDocument.class)).thenReturn(new EntityMetadata(TestDocument.class));
        ViewQuery viewQuery = mock(ViewQuery.class);
        when(viewQuery.reducing()).thenReturn(false);
        when(viewQuery.design()).thenReturn("testDes");
        when(viewQuery.view()).thenReturn("testView");
        QueryMethod queryMethod = mock(QueryMethod.class);
        when(queryMethod.isSliceQuery()).thenReturn(true);
        Parameter sortParameter = mock(Parameter.class);
        when(sortParameter.isSpecialParameter()).thenReturn(true);
        Parameter pageableParameter = mock(Parameter.class);
        when(pageableParameter.isSpecialParameter()).thenReturn(true);
        Parameters<?, ?> parameters = mock(Parameters.class);
        doReturn(Arrays.asList(pageableParameter, sortParameter).iterator()).when(parameters).iterator();
        when(parameters.getSortIndex()).thenReturn(1);
        when(parameters.getPageableIndex()).thenReturn(0);
        doReturn(parameters).when(queryMethod).getParameters();
        CouchDbViewQuery<TestDocument> query = new CouchDbViewQuery<>(client, viewQuery, queryMethod, TestDocument.class);
        query.execute(new Object[]{PageRequest.of(0, 50), Sort.by("value")});
        verify(client).readFromView("test", "testDes", "testView", 0L, 51, Sort.by("value"));
    }

    @Test
    void testReducing() throws IOException {
        CouchDbClient client = mock(CouchDbClient.class);
        when(client.getEntityMetadata(TestDocument.class)).thenReturn(new EntityMetadata(TestDocument.class));
        ViewQuery viewQuery = mock(ViewQuery.class);
        when(viewQuery.reducing()).thenReturn(true);
        when(viewQuery.design()).thenReturn("testDes");
        when(viewQuery.view()).thenReturn("testView");
        QueryMethod queryMethod = mock(QueryMethod.class);
        when(queryMethod.isSliceQuery()).thenReturn(false);
        doReturn(Long.class).when(queryMethod).getReturnedObjectType();
        Parameters<?, ?> parameters = mock(Parameters.class);
        doReturn(Collections.emptyIterator()).when(parameters).iterator();
        when(parameters.getSortIndex()).thenReturn(-1);
        when(parameters.getPageableIndex()).thenReturn(-1);
        doReturn(parameters).when(queryMethod).getParameters();
        CouchDbViewQuery<TestDocument> query = new CouchDbViewQuery<>(client, viewQuery, queryMethod, TestDocument.class);
        query.execute(new Object[0]);
        verify(client).reduce("test", "testDes", "testView", Long.class);
    }

    @Test
    void testReducingWithPageableAndSort() throws IOException {
        CouchDbClient client = mock(CouchDbClient.class);
        when(client.getEntityMetadata(TestDocument.class)).thenReturn(new EntityMetadata(TestDocument.class));
        ViewQuery viewQuery = mock(ViewQuery.class);
        when(viewQuery.reducing()).thenReturn(true);
        when(viewQuery.design()).thenReturn("testDes");
        when(viewQuery.view()).thenReturn("testView");
        QueryMethod queryMethod = mock(QueryMethod.class);
        when(queryMethod.isSliceQuery()).thenReturn(true);
        doReturn(Long.class).when(queryMethod).getReturnedObjectType();
        Parameter sortParameter = mock(Parameter.class);
        when(sortParameter.isSpecialParameter()).thenReturn(true);
        Parameter pageableParameter = mock(Parameter.class);
        when(pageableParameter.isSpecialParameter()).thenReturn(true);
        Parameters<?, ?> parameters = mock(Parameters.class);
        doReturn(Arrays.asList(pageableParameter, sortParameter).iterator()).when(parameters).iterator();
        when(parameters.getSortIndex()).thenReturn(1);
        when(parameters.getPageableIndex()).thenReturn(0);
        doReturn(parameters).when(queryMethod).getParameters();
        CouchDbViewQuery<TestDocument> query = new CouchDbViewQuery<>(client, viewQuery, queryMethod, TestDocument.class);
        query.execute(new Object[]{PageRequest.of(0, 50), Sort.by("value")});
        verify(client).reduce("test", "testDes", "testView", Long.class);
    }

}