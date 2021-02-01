/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.groocraft.couchdb.slacker.test.integration.initialization;

import com.groocraft.couchdb.slacker.CouchDbClient;
import com.groocraft.couchdb.slacker.CouchDbInitializer;
import com.groocraft.couchdb.slacker.annotation.EnableCouchDbRepositories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CusterInitializationConfiguration.class, CouchDbInitializer.class},
        initializers = ConfigFileApplicationContextInitializer.class)
@ActiveProfiles("test-cluster-initialization")
@EnableCouchDbRepositories
class ClusterInitializationIntegrationTest {

    @Autowired
    CouchDbClient client;

    @Test
    void initializedTest() {
        assertDoesNotThrow(() -> client.verifyCluster());
    }

}
