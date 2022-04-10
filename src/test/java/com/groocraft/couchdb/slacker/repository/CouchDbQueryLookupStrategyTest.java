package com.groocraft.couchdb.slacker.repository;

import com.groocraft.couchdb.slacker.CouchDbClient;
import com.groocraft.couchdb.slacker.annotation.Query;
import com.groocraft.couchdb.slacker.configuration.CouchDbProperties;
import com.groocraft.couchdb.slacker.test.integration.TestDocument;
import org.junit.jupiter.api.Test;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.Param;
import org.springframework.data.util.TypeInformation;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CouchDbQueryLookupStrategyTest {

    @Test
    void testParsing() throws NoSuchMethodException {
        Method method = CouchDbQueryLookupStrategyTest.class.getDeclaredMethod("findByValue", String.class);
        RepositoryMetadata repositoryMetadata = mock(RepositoryMetadata.class);
        ProjectionFactory projectionFactory = mock(ProjectionFactory.class);
        NamedQueries namedQueries = mock(NamedQueries.class);
        CouchDbClient client = mock(CouchDbClient.class);
        CouchDbProperties properties = mock(CouchDbProperties.class);
        when(properties.getBulkMaxSize()).thenReturn(100);
        CouchDbQueryLookupStrategy strategy = new CouchDbQueryLookupStrategy(client, properties);
        TypeInformation<?> typeInformation = mock(TypeInformation.class);

        doReturn(typeInformation).when(repositoryMetadata).getReturnType(method);
        doReturn(TestDocument.class).when(typeInformation).getType();
        doReturn(TestDocument.class).when(repositoryMetadata).getDomainType();
        doReturn(TestDocument.class).when(repositoryMetadata).getReturnedDomainClass(method);

        when(namedQueries.hasQuery(any())).thenReturn(false);

        assertEquals(CouchDbParsingQuery.class, strategy.resolveQuery(method, repositoryMetadata, projectionFactory, namedQueries).getClass(), "Method " +
                "without Query annotation must be process by CouchDbParsingQuery");

        verify(namedQueries, atLeastOnce().description("Strategy must check named queries")).hasQuery("TestDocument.findByValue");

    }

    @Test
    void testDirect() throws NoSuchMethodException {
        Method method = CouchDbQueryLookupStrategyTest.class.getDeclaredMethod("queryAnnotated", String.class);
        RepositoryMetadata repositoryMetadata = mock(RepositoryMetadata.class);
        ProjectionFactory projectionFactory = mock(ProjectionFactory.class);
        NamedQueries namedQueries = mock(NamedQueries.class);
        CouchDbClient client = mock(CouchDbClient.class);
        CouchDbProperties properties = mock(CouchDbProperties.class);
        CouchDbQueryLookupStrategy strategy = new CouchDbQueryLookupStrategy(client, properties);
        TypeInformation<?> typeInformation = mock(TypeInformation.class);

        doReturn(typeInformation).when(repositoryMetadata).getReturnType(method);
        doReturn(TestDocument.class).when(typeInformation).getType();
        doReturn(TestDocument.class).when(repositoryMetadata).getDomainType();
        doReturn(TestDocument.class).when(repositoryMetadata).getReturnedDomainClass(method);
        when(namedQueries.hasQuery(any())).thenReturn(false);

        assertEquals(CouchDbDirectQuery.class, strategy.resolveQuery(method, repositoryMetadata, projectionFactory, namedQueries).getClass(), "Method " +
                "with Query annotation must be process by CouchDbDirectQuery");

        verify(namedQueries, atLeastOnce().description("Strategy must check named queries")).hasQuery("TestDocument.queryAnnotated");
    }

    private List<TestDocument> findByValue(@Param("value") String value) {
        return Collections.emptyList();
    }

    @Query("{\"selector\": {\"value\": {\"$eq\": ?1}}}")
    private List<TestDocument> queryAnnotated(@Param("value") String value) {
        return Collections.emptyList();
    }

}