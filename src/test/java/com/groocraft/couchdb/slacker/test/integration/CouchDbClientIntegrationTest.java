package com.groocraft.couchdb.slacker.test.integration;

import com.groocraft.couchdb.slacker.CouchDbClient;
import com.groocraft.couchdb.slacker.CouchSlackerConfiguration;
import com.groocraft.couchdb.slacker.annotation.EnableCouchDbRepositories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CouchSlackerConfiguration.class}, initializers = ConfigFileApplicationContextInitializer.class)
@ActiveProfiles("test")
@EnableCouchDbRepositories
public class CouchDbClientIntegrationTest {

    @Autowired
    CouchDbClient client;

    //TODO test everything what is not accessible thru repository

}
