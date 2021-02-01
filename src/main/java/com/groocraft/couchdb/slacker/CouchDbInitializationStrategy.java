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

package com.groocraft.couchdb.slacker;

import com.groocraft.couchdb.slacker.configuration.CouchDbProperties;
import com.groocraft.couchdb.slacker.exception.ClusterException;
import com.groocraft.couchdb.slacker.utils.ThrowingBiConsumer;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Enum of all initialization strategies. Strategy determines how the connected CouchDB is initialized. The functionality is provided thru {@link CouchDbInitializer}
 *
 * @author Majlanky
 */
@Slf4j
public enum CouchDbInitializationStrategy {

    /**
     * This strategy does no initialization.
     */
    NONE(CouchDbInitializationStrategy::noInitialization),
    /**
     * This strategy initializes the connected CouchDB node as single node. If the node is already initialized, the initialization is skipped.
     */
    SINGLE(CouchDbInitializationStrategy::initializeSingle),
    /**
     * This strategy initializes the configured cluster if the connected node is coordinator
     * ({@link CouchDbProperties.Cluster#isCoordinator()}). If the node is not a coordinator, no initialization is done, even nodes are configured. If the node
     * is already initialized, the cluster is considered as finished and the initialization is skipped.
     */
    CLUSTER(CouchDbInitializationStrategy::initializeCluster);

    static final String USERS_DATABASE_NAME = "_users";
    static final String REPLICATOR_DATABASE_NAME = "_replicator";
    static final String GLOBAL_CHANGES_DATABASE_NAME = "_global_changes";

    private final ThrowingBiConsumer<CouchDbClient, CouchDbProperties, Exception> initializer;

    /**
     * @param initializer must not be {@literal null}
     */
    CouchDbInitializationStrategy(@NotNull ThrowingBiConsumer<CouchDbClient, CouchDbProperties, Exception> initializer) {
        this.initializer = initializer;
    }

    /**
     * Method executed a functionality of the strategy.
     *
     * @param client     must not be {@literal null}
     * @param properties must not be {@literal null}
     * @throws Exception if the initialization fails.
     */
    public void initialize(CouchDbClient client, CouchDbProperties properties) throws Exception {
        initializer.accept(client, properties);
    }

    /**
     * @param client     must not be {@literal null}
     * @param properties must not be {@literal null}
     */
    private static void noInitialization(CouchDbClient client, CouchDbProperties properties) {
        log.info("No initialization is done");
    }

    /**
     * @param client     must not be {@literal null}
     * @param properties must not be {@literal null}
     * @throws IOException if the initialization fails.
     */
    private static void initializeSingle(CouchDbClient client, CouchDbProperties properties) throws IOException {
        if (!client.databaseExists(USERS_DATABASE_NAME) && !client.databaseExists(REPLICATOR_DATABASE_NAME)) {
            log.info("Going to initialize CouchDB as single node");
            client.createDatabase(USERS_DATABASE_NAME);
            client.createDatabase(REPLICATOR_DATABASE_NAME);
            client.createDatabase(GLOBAL_CHANGES_DATABASE_NAME);
            log.debug("CouchDB initialized as single node");
        } else {
            log.info("CouchDB is already initialized, initialization steps skipped");
        }
    }

    /**
     * @param client     must not be {@literal null}
     * @param properties must not be {@literal null}
     * @throws IOException      if the initialization fails
     * @throws ClusterException when cluster is finished but not working properly
     */
    private static void initializeCluster(CouchDbClient client, CouchDbProperties properties) throws IOException, ClusterException {
        if (!client.databaseExists(USERS_DATABASE_NAME) && !client.databaseExists(REPLICATOR_DATABASE_NAME)) {
            log.info("Going to initialize DB as cluster node");
            if (properties.getCluster().isCoordinator()) {
                log.info("Connected node marked as cluster setup coordinator. Going to initialize cluster");
                String nodeId = client.getNodeId();
                Optional<String> uuid = client.getUUID();
                if (uuid.isPresent()) {
                    log.debug("The connected CouchDB has UUID set to {}, no generation needed", uuid);
                } else {
                    String newUuid = "couch-slacker-rules-" + UUID.randomUUID().toString();
                    client.setUUID(newUuid);
                    log.info("The connected CouchDB had not UUID set, has been set to {}", newUuid);
                }
                Set<String> nodesToAdd = properties.getCluster().getNodes();
                nodesToAdd.removeIf(s -> s.startsWith(nodeId.substring(8)));
                for (String nodeUrl : nodesToAdd) {
                    URI uri = URI.create("unknown://" + nodeUrl);
                    client.joinNodeToCluster(uri.getHost(), uri.getPort(), properties.getUsername(), properties.getPassword());
                }
                client.finishClusterSetting();
                client.verifyCluster();
                log.info("Cluster initialized");
            }
            log.debug("CouchDB initialized as cluster node");
        } else {
            log.info("CouchDB is already initialized, initialization steps skipped");
        }
    }

}
