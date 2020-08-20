package com.groocraft.couchdb.slacker.repository;

import com.groocraft.couchdb.slacker.CouchDbClient;
import com.groocraft.couchdb.slacker.TestDocument;
import org.junit.jupiter.api.Test;
import org.springframework.data.repository.CrudRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class CouchDbRepositoryFactoryBeanTest {

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void test() {
        CouchDbClient client = mock(CouchDbClient.class);
        CouchDbRepositoryFactoryBean<CrudRepository<TestDocument, String>, TestDocument, String> factoryBean =
                new CouchDbRepositoryFactoryBean<>(CrudRepository.class, client);
        assertEquals(CouchDbRepositoryFactory.class, factoryBean.createRepositoryFactory().getClass(),
                CouchDbRepositoryFactoryBean.class + "must produce " + CouchDbRepositoryFactory.class);
    }

}