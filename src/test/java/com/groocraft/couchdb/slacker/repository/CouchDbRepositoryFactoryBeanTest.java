package com.groocraft.couchdb.slacker.repository;

import com.groocraft.couchdb.slacker.CouchDbClient;
import com.groocraft.couchdb.slacker.configuration.CouchDbProperties;
import org.junit.jupiter.api.Test;
import org.springframework.data.repository.CrudRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CouchDbRepositoryFactoryBeanTest {

    @Test
    void test() {
        CouchDbClient client = mock(CouchDbClient.class);
        CouchDbProperties properties = mock(CouchDbProperties.class);
        when(properties.getBulkMaxSize()).thenReturn(100);
        CouchDbRepositoryFactoryBean<?, ?, ?> factoryBean = new CouchDbRepositoryFactoryBean<>(CrudRepository.class, client, properties);
        assertEquals(CouchDbRepositoryFactory.class, factoryBean.createRepositoryFactory().getClass(),
                CouchDbRepositoryFactoryBean.class + "must produce " + CouchDbRepositoryFactory.class);
    }

}