package com.groocraft.couchdb.slacker.repository;

import com.groocraft.couchdb.slacker.CouchDbClient;
import com.groocraft.couchdb.slacker.TestDocument;
import com.groocraft.couchdb.slacker.annotation.Query;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.Param;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CouchDbQueryLookupStrategyTest {

    @Test
    public void testParsing() throws NoSuchMethodException {
        Method method = CouchDbQueryLookupStrategyTest.class.getDeclaredMethod("findByValue", String.class);
        RepositoryMetadata repositoryMetadata = Mockito.mock(RepositoryMetadata.class);
        ProjectionFactory projectionFactory = Mockito.mock(ProjectionFactory.class);
        NamedQueries namedQueries = Mockito.mock(NamedQueries.class);
        CouchDbClient client = Mockito.mock(CouchDbClient.class);
        CouchDbQueryLookupStrategy strategy = new CouchDbQueryLookupStrategy(client);

        Mockito.doReturn(TestDocument.class).when(repositoryMetadata).getDomainType();
        Mockito.doReturn(TestDocument.class).when(repositoryMetadata).getReturnedDomainClass(method);
        Mockito.when(namedQueries.hasQuery(Mockito.any())).thenReturn(false);

        assertEquals(CouchDbParsingQuery.class, strategy.resolveQuery(method, repositoryMetadata, projectionFactory, namedQueries).getClass(), "Method " +
                "without Query annotation must be process by CouchDbParsingQuery");

        Mockito.verify(namedQueries, Mockito.atLeastOnce().description("Strategy must check named queries")).hasQuery("TestDocument.findByValue");

    }

    @Test
    public void testDirect() throws NoSuchMethodException {
        Method method = CouchDbQueryLookupStrategyTest.class.getDeclaredMethod("queryAnnotated", String.class);
        RepositoryMetadata repositoryMetadata = Mockito.mock(RepositoryMetadata.class);
        ProjectionFactory projectionFactory = Mockito.mock(ProjectionFactory.class);
        NamedQueries namedQueries = Mockito.mock(NamedQueries.class);
        CouchDbClient client = Mockito.mock(CouchDbClient.class);
        CouchDbQueryLookupStrategy strategy = new CouchDbQueryLookupStrategy(client);

        Mockito.doReturn(TestDocument.class).when(repositoryMetadata).getDomainType();
        Mockito.doReturn(TestDocument.class).when(repositoryMetadata).getReturnedDomainClass(method);
        Mockito.when(namedQueries.hasQuery(Mockito.any())).thenReturn(false);

        assertEquals(CouchDbDirectQuery.class, strategy.resolveQuery(method, repositoryMetadata, projectionFactory, namedQueries).getClass(), "Method " +
                "with Query annotation must be process by CouchDbDirectQuery");

        Mockito.verify(namedQueries, Mockito.atLeastOnce().description("Strategy must check named queries")).hasQuery("TestDocument.queryAnnotated");
    }

    private List<TestDocument> findByValue(@Param("value") String value) {
        return Collections.emptyList();
    }

    @Query("{\"selector\": {\"value\": {\"$eq\": ?1}}}")
    private List<TestDocument> queryAnnotated(@Param("value") String value) {
        return Collections.emptyList();
    }

}