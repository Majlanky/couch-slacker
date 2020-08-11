package com.groocraft.couchdb.slacker.repository;

import com.groocraft.couchdb.slacker.annotation.EnableCouchDbRepositories;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CouchDbRepositoriesRegistrarTest {

    @Test
    public void test(){
        CouchDbRepositoriesRegistrar registrar = new CouchDbRepositoriesRegistrar();
        assertEquals(EnableCouchDbRepositories.class, registrar.getAnnotation(), "Registrar has to process " + EnableCouchDbRepositories.class);
        assertEquals(CouchDbRepositoryConfigurationExtension.class, registrar.getExtension().getClass(),
                "Registrar must return " + CouchDbRepositoryConfigurationExtension.class + " as the extension");
    }

}