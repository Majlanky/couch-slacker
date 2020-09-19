package com.groocraft.couchdb.slacker.repository;

import com.groocraft.couchdb.slacker.CouchDbClient;
import com.groocraft.couchdb.slacker.TestDocument;
import com.groocraft.couchdb.slacker.configuration.CouchDbProperties;
import org.junit.jupiter.api.Test;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CouchDbRepositoryFactoryTest {

    @Test
    public void test() {
        CouchDbClient client = mock(CouchDbClient.class);
        RepositoryMetadata repositoryMetadata = mock(RepositoryMetadata.class);
        QueryLookupStrategy.Key key = mock(QueryLookupStrategy.Key.class);
        QueryMethodEvaluationContextProvider evaluationContextProvider = mock(QueryMethodEvaluationContextProvider.class);
        RepositoryInformation information = mock(RepositoryInformation.class);
        CouchDbProperties properties = mock(CouchDbProperties.class);
        doReturn(SimpleCouchDbRepository.class).when(information).getRepositoryBaseClass();
        doReturn(TestDocument.class).when(information).getDomainType();
        when(properties.getBulkMaxSize()).thenReturn(100);
        CouchDbRepositoryFactory factory = new CouchDbRepositoryFactory(client, properties);
        factory.getEntityInformation(TestDocument.class);
        assertEquals(SimpleCouchDbRepository.class, factory.getRepositoryBaseClass(repositoryMetadata),
                "Factory must report " + SimpleCouchDbRepository.class +
                        "as base repository");
        assertTrue(factory.getQueryLookupStrategy(key, evaluationContextProvider).isPresent(), CouchDbRepositoryFactory.class + " must support querying");
        assertEquals(CouchDbQueryLookupStrategy.class, factory.getQueryLookupStrategy(key, evaluationContextProvider).get().getClass(),
                CouchDbRepositoryFactory.class + " must support querying by " + CouchDbQueryLookupStrategy.class);
        assertEquals(SimpleCouchDbRepository.class, factory.getTargetRepository(information).getClass(),
                "Factory must return " + SimpleCouchDbRepository.class);


    }

}