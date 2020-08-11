package com.groocraft.couchdb.slacker.repository;

import com.groocraft.couchdb.slacker.CouchDbClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.repository.CrudRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CouchDbRepositoryFactoryBeanTest {

    @Test
    public void test() {
        CouchDbClient client = Mockito.mock(CouchDbClient.class);
        CouchDbRepositoryFactoryBean factoryBean = new CouchDbRepositoryFactoryBean(CrudRepository.class, client);
        assertEquals(CouchDbRepositoryFactory.class, factoryBean.createRepositoryFactory().getClass(),
                CouchDbRepositoryFactoryBean.class + "must produce " + CouchDbRepositoryFactory.class);
    }

}