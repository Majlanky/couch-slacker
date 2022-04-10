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

package com.groocraft.couchdb.slacker.configuration;

import com.groocraft.couchdb.slacker.CouchDbInitializationStrategy;
import com.groocraft.couchdb.slacker.QueryStrategy;
import com.groocraft.couchdb.slacker.SchemaOperation;
import org.hibernate.validator.constraints.URL;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Properties pojo class for Couch Slacker configuration. It is used in {@link CouchSlackerConfiguration} class
 *
 * @author Majlanky
 * @see CouchSlackerConfiguration
 */
@Validated
@ConfigurationProperties(prefix = "couchdb.client")
public class CouchDbProperties {

    public static final String COUCH_ID_NAME = "_id";
    public static final String COUCH_REVISION_NAME = "_rev";

    /**
     * Must be valid URL.
     * Port is mandatory.
     * Scheme can be http or https.
     * Scheme is not mandatory, http is used if not present.
     */
    @URL
    private String url;

    /**
     * Name of user used for authentication to CouchDB.
     * Must not be empty.
     */
    @NotEmpty
    private String username;

    /**
     * Password for name used for authentication to CouchDB.
     * Must not be empty.
     */
    @NotEmpty
    private String password;

    /**
     * If bulk operation (findAll, deleteAll, etc.) is executed, this is the limit of document number processed in one batch.
     * Operation requesting operation with more documents than limit is processed in more batches.
     * Minimum is 10, maximum is 100000.
     * Default value is 10000.
     */
    @Min(10)
    @Max(100000)
    private int bulkMaxSize = 10000;

    /**
     * Flag which can turn on/off execution stats in the _find query result.
     * The stats are logged if turned on.
     * Default value is false.
     */
    private boolean findExecutionStats = false;

    /**
     * Schema operation is done before a usage of a database. Couch Slacker read all defined document mapping which gives list of used databases. Depending on
     * the configured operation validation that databases in the list exists(validate),
     * creates databases from the list(create),
     * delete and create databases from the list(drop)
     * or no operation is done (none).
     * With what parameters a database is created depends on Database annotation values or the following three values default-shards, default-replicas,
     * default-partitioned.
     * Default value is validate.
     */
    private SchemaOperation schemaOperation = SchemaOperation.VALIDATE;

    /**
     * If database is created by Couch Slacker, this value says number of shards.
     * Default value is 8
     */
    private int defaultShards = 8;

    /**
     * If database is created by Couch Slacker, this value says number of replicas.
     * Default value is 3
     */
    private int defaultReplicas = 3;

    /**
     * If database is created by Couch Slacker, this value says if it should be partitioned
     * Default value is false
     */
    private boolean defaultPartitioned = false;

    /**
     * Query strategy defines how query methods are executed. If mango is used, query methods are parsed to mango query and process standard CouchDB way. If
     * "view" is used, Couch Slacker will define view with matching rules for every query to speed up query time.
     */
    private QueryStrategy queryStrategy = QueryStrategy.MANGO;

    /**
     * Initialization strategy defines what is done during startup of application. DB can be initialized as a single node or node in a CouchDB cluster. If
     * initialization of a single node is configured, no more data is needed. If the cluster initialization is configured, there is need to configure
     * additional data {@link Cluster}. Default value is NONE, means no initialization is done by the application.
     */
    private CouchDbInitializationStrategy initializationStrategy = CouchDbInitializationStrategy.NONE;

    /**
     * Sub-node of all cluster information. The node is mandatory when initialization-strategy ({@link #initializationStrategy}) is se to cluster
     * ({@link CouchDbInitializationStrategy#CLUSTER}).
     * Default value is null.
     */
    @NestedConfigurationProperty
    private Cluster cluster = new Cluster();

    /**
     * Map of entity mapping override/extension. The map is keyed by name of a context. Values of the map are lists of {@link Document}
     * Default value is empty map meaning no overriding/extending is done.
     */
    @NestedConfigurationProperty
    private Map<String, List<Document>> mapping = Collections.emptyMap();

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getBulkMaxSize() {
        return bulkMaxSize;
    }

    public void setBulkMaxSize(int bulkMaxSize) {
        this.bulkMaxSize = bulkMaxSize;
    }

    public boolean isFindExecutionStats() {
        return findExecutionStats;
    }

    public void setFindExecutionStats(boolean findExecutionStats) {
        this.findExecutionStats = findExecutionStats;
    }

    public SchemaOperation getSchemaOperation() {
        return schemaOperation;
    }

    public void setSchemaOperation(SchemaOperation schemaOperation) {
        this.schemaOperation = schemaOperation;
    }

    public int getDefaultShards() {
        return defaultShards;
    }

    public void setDefaultShards(int defaultShards) {
        this.defaultShards = defaultShards;
    }

    public int getDefaultReplicas() {
        return defaultReplicas;
    }

    public void setDefaultReplicas(int defaultReplicas) {
        this.defaultReplicas = defaultReplicas;
    }

    public boolean isDefaultPartitioned() {
        return defaultPartitioned;
    }

    public void setDefaultPartitioned(boolean defaultPartitioned) {
        this.defaultPartitioned = defaultPartitioned;
    }

    public QueryStrategy getQueryStrategy() {
        return queryStrategy;
    }

    public void setQueryStrategy(QueryStrategy queryStrategy) {
        this.queryStrategy = queryStrategy;
    }

    public CouchDbInitializationStrategy getInitializationStrategy() {
        return initializationStrategy;
    }

    public void setInitializationStrategy(CouchDbInitializationStrategy initializationStrategy) {
        this.initializationStrategy = initializationStrategy;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }

    public Map<String, List<Document>> getMapping() {
        return mapping;
    }

    public void setMapping(Map<String, List<Document>> mapping) {
        this.mapping = new HashMap<>();
        mapping.forEach((k, v) -> this.mapping.put(k, new ArrayList<>(v)));
    }

    public void copy(CouchDbProperties properties) {
        setPassword(properties.getPassword());
        setUsername(properties.getUsername());
        setUrl(properties.url);
        setBulkMaxSize(properties.getBulkMaxSize());
        setFindExecutionStats(properties.isFindExecutionStats());
        setSchemaOperation(properties.getSchemaOperation());
        setDefaultShards(properties.getDefaultShards());
        setDefaultReplicas(properties.getDefaultReplicas());
        setDefaultPartitioned(properties.isDefaultPartitioned());
        setQueryStrategy(properties.getQueryStrategy());
        setInitializationStrategy(properties.getInitializationStrategy());
        setCluster(new Cluster(properties.getCluster()));
        setMapping(properties.getMapping());
    }

    public static class Cluster {

        /**
         * The flag indicates if the connected CouchDB should coordinate cluster initialization
         * Default value is false;
         */
        private boolean coordinator = false;

        /**
         * List of all nodes of the cluster includes the connected one.
         * Default value is empty list.
         */
        private Set<String> nodes = Collections.emptySet();

        /**
         * Default constructor required by Spring to parse configuration file properly.
         */
        public Cluster() {
        }

        /**
         * Copy constructor
         *
         * @param cluster must not be {@literal null}
         */
        public Cluster(@NotNull Cluster cluster) {
            this.coordinator = cluster.isCoordinator();
            this.nodes = new HashSet<>(cluster.getNodes());
        }

        public boolean isCoordinator() {
            return coordinator;
        }

        public void setCoordinator(boolean coordinator) {
            this.coordinator = coordinator;
        }

        public Set<String> getNodes() {
            return new HashSet<>(nodes);
        }

        public void setNodes(Set<String> nodes) {
            this.nodes = new HashSet<>(nodes);
        }
    }

    public static class Document {

        /**
         * Fully qualified class name of entity
         */
        private String entityClass;

        /**
         * Name of database used for the {@code entityClass}
         */
        private String database;

        public Document() {
        }

        public Document(String entityClass, String database) {
            this.entityClass = entityClass;
            this.database = database;
        }

        public String getEntityClass() {
            return entityClass;
        }

        public void setEntityClass(String entityClass) {
            this.entityClass = entityClass;
        }

        public String getDatabase() {
            return database;
        }

        public void setDatabase(String database) {
            this.database = database;
        }
    }

}
