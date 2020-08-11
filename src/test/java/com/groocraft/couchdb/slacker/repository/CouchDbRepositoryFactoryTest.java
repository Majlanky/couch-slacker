package com.groocraft.couchdb.slacker.repository;

import com.groocraft.couchdb.slacker.CouchDbClient;
import com.groocraft.couchdb.slacker.TestDocument;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CouchDbRepositoryFactoryTest {

    @Test
    public void test() {
        CouchDbClient client = Mockito.mock(CouchDbClient.class);
        RepositoryMetadata repositoryMetadata = Mockito.mock(RepositoryMetadata.class);
        QueryLookupStrategy.Key key = Mockito.mock(QueryLookupStrategy.Key.class);
        QueryMethodEvaluationContextProvider evaluationContextProvider = Mockito.mock(QueryMethodEvaluationContextProvider.class);
        RepositoryInformation information = Mockito.mock(RepositoryInformation.class);
        Mockito.doReturn(SimpleCouchDbRepository.class).when(information).getRepositoryBaseClass();
        Mockito.doReturn(TestDocument.class).when(information).getDomainType();
        CouchDbRepositoryFactory factory = new CouchDbRepositoryFactory(client);
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