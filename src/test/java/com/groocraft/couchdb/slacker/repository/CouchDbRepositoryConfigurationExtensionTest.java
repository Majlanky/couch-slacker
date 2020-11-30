package com.groocraft.couchdb.slacker.repository;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CouchDbRepositoryConfigurationExtensionTest {

    @Test
    void test() {
        CouchDbRepositoryConfigurationExtension extension = new CouchDbRepositoryConfigurationExtension();
        assertEquals("couchDb", extension.getModulePrefix(), "Extension must return couchDb as prefix of named queries lookup");
        assertEquals(CouchDbRepositoryFactoryBean.class.getName(), extension.getRepositoryFactoryBeanClassName(),
                "Extension must return " + CouchDbRepositoryFactoryBean.class + "as the extension");
    }

}