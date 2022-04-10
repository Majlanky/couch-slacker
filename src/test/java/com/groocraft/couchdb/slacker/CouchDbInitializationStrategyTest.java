/*
 * Copyright 2020-2022 the original author or authors.
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

package com.groocraft.couchdb.slacker;

import com.groocraft.couchdb.slacker.configuration.CouchDbProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CouchDbInitializationStrategyTest {

    @Mock
    CouchDbClient client;

    @Mock
    CouchDbProperties properties;

    @Mock
    CouchDbProperties.Cluster cluster;

    @Test
    void noneTest() throws Exception {
        CouchDbInitializationStrategy.NONE.initialize(client, properties);
        verifyNoInteractions(client, properties);
    }

    @Test
    void singleTest() throws Exception {
        when(client.databaseExists(CouchDbInitializationStrategy.USERS_DATABASE_NAME)).thenReturn(false);
        when(client.databaseExists(CouchDbInitializationStrategy.REPLICATOR_DATABASE_NAME)).thenReturn(false);
        CouchDbInitializationStrategy.SINGLE.initialize(client, properties);
        verify(client, atLeastOnce().description(CouchDbInitializationStrategy.USERS_DATABASE_NAME + " database must be created to initialize CouchDB"))
                .createDatabase(CouchDbInitializationStrategy.USERS_DATABASE_NAME);
        verify(client, atLeastOnce().description(CouchDbInitializationStrategy.REPLICATOR_DATABASE_NAME + " database must be created to initialize CouchDB"))
                .createDatabase(CouchDbInitializationStrategy.REPLICATOR_DATABASE_NAME);
        verify(client, atLeastOnce().description(CouchDbInitializationStrategy.GLOBAL_CHANGES_DATABASE_NAME + " database must be created to initialize CouchDB"))
                .createDatabase(CouchDbInitializationStrategy.GLOBAL_CHANGES_DATABASE_NAME);
    }

    @Test
    void clusterTest() throws Exception {
        when(client.databaseExists(CouchDbInitializationStrategy.USERS_DATABASE_NAME)).thenReturn(false);
        when(client.databaseExists(CouchDbInitializationStrategy.REPLICATOR_DATABASE_NAME)).thenReturn(false);
        when(client.getNodeId()).thenReturn("couchdb@first");
        when(client.getUUID()).thenReturn(Optional.empty());
        when(cluster.isCoordinator()).thenReturn(true);
        when(cluster.getNodes()).thenReturn(new HashSet<>(Arrays.asList("first:5984", "second:5984", "third:5984")));
        when(properties.getCluster()).thenReturn(cluster);
        when(properties.getUsername()).thenReturn("admin");
        when(properties.getPassword()).thenReturn("password");
        CouchDbInitializationStrategy.CLUSTER.initialize(client, properties);
        verify(client, atLeastOnce().description("When UUID is not set on coordinator node, it must be set to identify cluster")).setUUID(any());
        verify(client, atLeastOnce().description("Second node must be added to cluster")).joinNodeToCluster("second", 5984, "admin", "password");
        verify(client, atLeastOnce().description("Third node must be added to cluster")).joinNodeToCluster("third", 5984, "admin", "password");
        verify(client, never().description("Coordinator node must not be added because is already in cluster"))
                .joinNodeToCluster("first", 5984, "admin", "password");
        verify(client, atLeastOnce().description("Without finish, cluster is not ready")).finishClusterSetting();
        verify(client, atLeastOnce().description("Cluster must be verified to prove functionality")).verifyCluster();
    }

}