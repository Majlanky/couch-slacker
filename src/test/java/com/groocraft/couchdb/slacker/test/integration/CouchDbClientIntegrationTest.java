package com.groocraft.couchdb.slacker.test.integration;

import com.groocraft.couchdb.slacker.CouchDbClient;
import com.groocraft.couchdb.slacker.configuration.CouchSlackerConfiguration;
import com.groocraft.couchdb.slacker.annotation.EnableCouchDbRepositories;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Disabled
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CouchSlackerConfiguration.class}, initializers = ConfigFileApplicationContextInitializer.class)
@ActiveProfiles("test")
@EnableCouchDbRepositories
public class CouchDbClientIntegrationTest {

    @Autowired
    CouchDbClient client;

}
