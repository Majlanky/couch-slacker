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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.groocraft.couchdb.slacker.exception.CouchDbException;
import com.groocraft.couchdb.slacker.http.AutoCloseableHttpResponse;
import com.groocraft.couchdb.slacker.repository.CouchDbEntityInformation;
import com.groocraft.couchdb.slacker.structure.AllDocumentResponse;
import com.groocraft.couchdb.slacker.structure.BulkGetRequest;
import com.groocraft.couchdb.slacker.structure.BulkGetResponse;
import com.groocraft.couchdb.slacker.structure.BulkRequest;
import com.groocraft.couchdb.slacker.structure.DesignDocument;
import com.groocraft.couchdb.slacker.structure.DocumentFindResponse;
import com.groocraft.couchdb.slacker.structure.DocumentPutResponse;
import com.groocraft.couchdb.slacker.structure.FindResult;
import com.groocraft.couchdb.slacker.structure.IndexCreateRequest;
import com.groocraft.couchdb.slacker.structure.View;
import com.groocraft.couchdb.slacker.utils.BulkGetDeserializer;
import com.groocraft.couchdb.slacker.utils.DeleteDocumentSerializer;
import com.groocraft.couchdb.slacker.utils.DeleteViewedDocumentSerializer;
import com.groocraft.couchdb.slacker.utils.FoundDocumentDeserializer;
import com.groocraft.couchdb.slacker.utils.LazyLog;
import com.groocraft.couchdb.slacker.utils.ThrowingFunction;
import com.groocraft.couchdb.slacker.utils.ViewedDocumentSerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.util.Assert;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Client for CouchDB REST API. It is using Jackson library to work with json.
 *
 * @author Majlanky
 */
@Slf4j
public class CouchDbClient {

    final static String ALL_DESIGN = "all";
    final static String ALL_DATA_VIEW = "data";
    final static String VIEW_MAP = "function(doc){if(doc.%1$s == \"%2$s\"){emit(null);}}";
    final static String ALL_DATA_MAP = "function(doc){emit(null);}";
    final static String COUNT_REDUCE = "_count";
    final static String SORTED_VIEW_MAP = "function(doc){emit([%1$s]);}";
    final static String SORTED_TYPED_VIEW_MAP = "function(doc){if(doc.%1$s == \"%2$s\"){emit([%3$s]);}}";
    final static String SORTED_FIND_VIEW_MAP = "function(doc){if%1$s{emit([%2$s]);}}";
    final static String FIND_VIEW_MAP = "function(doc){if%1$s{emit(null);}}";

    private final static String VIEW_REDUCE_PARAMETER = "reduce";
    private final static String VIEW_LIMIT_PARAMETER = "limit";
    private final static String VIEW_SKIP_PARAMETER = "skip";

    private final HttpClient httpClient;
    private final HttpHost httpHost;
    private final HttpContext httpContext;
    @SuppressWarnings({"rawtypes"})
    private final Map<Class, EntityMetadata> entityMetadataCache;
    private final Set<String> knownIndexes;
    private final Set<String> knownSortedViews;
    private final URI baseURI;
    private final ObjectMapper mapper;
    @SuppressWarnings({"rawtypes"})
    private final Map<Class, IdGenerator> idGenerators;
    private final IdGenerator<?> defaultIdGenerator;
    private final int defaultShards;
    private final int defaultReplicas;
    private final boolean defaultPartitioned;
    private final int bulkMaxSize;
    private final QueryStrategy queryStrategy;

    /**
     * @param httpClient         must not be {@literal null}
     * @param httpHost           must not be {@literal null}
     * @param httpContext        must not be {@literal null}
     * @param baseURI            where CouchDB is accessible without database specification. Must not be {@literal null}
     * @param idGenerators       {@link Iterable} of available {@link IdGenerator}. If empty, default generator {@link IdGeneratorUUID} is used. Must not be {@literal
     *                           null}
     * @param defaultShards      number of shard used for every a newly created database
     * @param defaultReplicas    number of replicas used for every a newly created database
     * @param defaultPartitioned flag of partitioned used for every a newly created database
     * @param bulkMaxSize        maximal size of bulk operations
     */
    CouchDbClient(@NotNull HttpClient httpClient,
                  @NotNull HttpHost httpHost,
                  @NotNull HttpContext httpContext,
                  @NotNull URI baseURI,
                  @NotNull Iterable<IdGenerator<?>> idGenerators,
                  int defaultShards,
                  int defaultReplicas,
                  boolean defaultPartitioned,
                  int bulkMaxSize,
                  QueryStrategy queryStrategy) {
        Assert.notNull(httpClient, "HttpClient must not be null.");
        Assert.notNull(httpHost, "HttpHost must not be null.");
        Assert.notNull(httpContext, "HttpContext must not be null.");
        Assert.notNull(baseURI, "BaseURI must not be null.");
        Assert.notNull(idGenerators, "IdGenerators must not be null.");
        Assert.isTrue(defaultShards > 0, "DefaultShards must be positive number");
        Assert.isTrue(defaultReplicas > 0, "DefaultReplicas must be positive number");
        this.httpClient = httpClient;
        this.baseURI = baseURI;
        this.httpHost = httpHost;
        this.httpContext = httpContext;
        entityMetadataCache = new HashMap<>();
        knownIndexes = new HashSet<>();
        knownSortedViews = new HashSet<>();
        this.mapper = new ObjectMapper();
        this.idGenerators = new HashMap<>();
        this.defaultIdGenerator = new IdGeneratorUUID();
        this.defaultShards = defaultShards;
        this.defaultReplicas = defaultReplicas;
        this.defaultPartitioned = defaultPartitioned;
        this.bulkMaxSize = bulkMaxSize;
        this.queryStrategy = queryStrategy;
        idGenerators.forEach(g -> this.idGenerators.put(g.getEntityClass(), g));
    }

    /**
     * Returns new instance of {@link CouchDbClientBuilder} which is able to build {@link CouchDbClient}
     *
     * @return {@link CouchDbClientBuilder}
     */
    public static CouchDbClientBuilder builder() {
        return new CouchDbClientBuilder();
    }

    /**
     * @param clazz     of entity type about which information is needed. Must not be {@literal null}
     * @param <EntityT> type of entity about which information is needed
     * @param <IdT>     type of entity Id
     * @return {@link org.springframework.data.repository.core.EntityInformation} implementation for CouchDB entities.
     */
    public <EntityT, IdT> @NotNull CouchDbEntityInformation<EntityT, IdT> getEntityInformation(@NotNull Class<EntityT> clazz) {
        return new CouchDbEntityInformation<>(getEntityMetadata(clazz));
    }

    /**
     * @param clazz about which metadata is needed. Must not be {@literal null}
     * @param <T>   type of class
     * @return {@link EntityMetadata} about passed class
     */
    @SuppressWarnings("unchecked")
    public <T> @NotNull EntityMetadata<T> getEntityMetadata(@NotNull Class<T> clazz) {
        return entityMetadataCache.computeIfAbsent(clazz, EntityMetadata::new);
    }

    /**
     * Method to obtain new generated id with a relevant {@link IdGenerator}. There is a default generator which is used by default for all document if a
     * document is not annotated with {@link com.groocraft.couchdb.slacker.annotation.CustomIdGeneration}
     *
     * @param entity    new entity for which the ID needs to be generated
     * @param clazz     Class of the given entity
     * @param <EntityT> Entity type
     * @return Generated ID for the given entity. Can not be {@literal null}
     * @see #CouchDbClient(HttpClient, HttpHost, HttpContext, URI, Iterable, int, int, boolean, int, QueryStrategy)
     */
    @SuppressWarnings("unchecked")
    private <EntityT> @NotNull String generateId(@NotNull EntityT entity, Class<EntityT> clazz) {
        return idGenerators.computeIfAbsent(clazz, c -> defaultIdGenerator).generate(entity);
    }

    /**
     * @param clazz class of entity for which database name is needed. Must not be {@literal null}
     * @return Name of database for given class
     * @see EntityMetadata
     */
    public @NotNull String getDatabaseName(@NotNull Class<?> clazz) {
        return getEntityMetadata(clazz).getDatabaseName();
    }

    /**
     * Method providing wrapping about {@link URIBuilder} which is throwing {@link URISyntaxException}, subtype of {@link Exception}. Because URIes are
     * checked in {@link CouchDbClient} context, there is not need to handle it every time. If URI is not checked {@link IllegalArgumentException} is thrown
     * as warning.
     *
     * @param base         URI from with the result is created. Must not be {@link null}
     * @param pathSegments additional segments to base URI. Must not be {@link null}
     * @return new URI created by joining base and pathSegments
     */
    private @NotNull URI getURI(@NotNull URI base, String... pathSegments) {
        try {
            return new URIBuilder(base).setPathSegments(pathSegments).build();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Method providing wrapping about {@link URIBuilder} which is throwing {@link URISyntaxException}, subtype of {@link Exception}. Because URIes are
     * checked in {@link CouchDbClient} context, there is not need to handle it every time. If URI is not checked {@link IllegalArgumentException} is thrown
     * as warning.
     *
     * @param base         URI from with the result is created. Must not be {@link null}
     * @param pathSegments additional segments to base URI. Must not be {@link null}
     * @param parameters   additional parameters of URI. Must not be {@literal null}
     * @return new URI created by joining base and pathSegments
     */
    private @NotNull URI getURI(@NotNull URI base, @NotNull List<String> pathSegments, @NotNull List<NameValuePair> parameters) {
        try {
            return new URIBuilder(base).setPathSegments(pathSegments).addParameters(parameters).build();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Saving given entity. If there is no ID for given instance, a relevant {@link IdGenerator} is used to generate new ID.
     *
     * @param entity    instance to save. Must not be {@literal null}
     * @param <EntityT> type of entity
     * @return entity instance with id and revision updated to the current state
     * @throws IOException in cases like conflict with revision and etc.
     * @see DocumentBase
     */
    @SuppressWarnings({"unchecked"})
    public <EntityT> @NotNull EntityT save(@NotNull EntityT entity) throws IOException {
        EntityMetadata<?> entityMetadata = getEntityMetadata(entity.getClass());
        ObjectMapper localMapper = mapper;
        String id = entityMetadata.getIdReader().read(entity);
        log.debug("Saving document {} with id {} and revision {} to database {}", entity, id,
                LazyLog.of(() -> entityMetadata.getRevisionReader().read(entity)), entityMetadata.getDatabaseName());
        if ("".equals(id) || id == null) {
            id = generateId(entity, (Class<EntityT>) entity.getClass());
            log.debug("New ID {} generated for saved document", id);
        }
        if (entityMetadata.isViewed()) {
            localMapper = new ObjectMapper();
            SimpleModule module = new SimpleModule();
            module.addSerializer(new ViewedDocumentSerializer<>(entity.getClass(), entityMetadata.getTypeField(), entityMetadata.getType()));
            localMapper.registerModule(module);
        }

        DocumentPutResponse response = put(getURI(baseURI, entityMetadata.getDatabaseName(), id), localMapper.writeValueAsString(entity),
                r -> mapper.readValue(r.getEntity().getContent(),
                        DocumentPutResponse.class));
        entityMetadata.getRevisionWriter().write(entity, response.getRev());
        entityMetadata.getIdWriter().write(entity, response.getId());
        log.debug("Saved document {} with id {} and revision {}", entity, response.getId(), response.getRev());
        return entity;
    }

    /**
     * Saving all given entities in one POST request. If there is no ID for given instance, a relevant {@link IdGenerator} is used to generate new ID.
     * Spring data documentation says, the same list as passed must be returned. Because some updates can fail, it is very unfortunate.
     * The only way to solve this is to check revision and id of returned document to find out, what was saved/updated and what not. The failed documents are
     * logged on warn level.
     *
     * @param entities  {@link Iterable} of entities to save. Must not be {@literal null}
     * @param clazz     Class of entities passed to save. Must not be {@literal null}
     * @param <EntityT> type of entities passed to save
     * @return {@link Iterable} of all passed entities with updated revisions and ids. Can not be {@literal null}
     * @throws IOException if http request is not successful or json processing fail
     */
    @SuppressWarnings({"unchecked"})
    public <EntityT> @NotNull Iterable<EntityT> saveAll(@NotNull Iterable<EntityT> entities, @NotNull Class<?> clazz) throws IOException {
        EntityMetadata<?> entityMetadata = getEntityMetadata(clazz);
        ObjectMapper localMapper = mapper;
        log.debug("Bulk save of {} documents to database {}", LazyLog.of(() -> StreamSupport.stream(entities.spliterator(), false).count()),
                entityMetadata.getDatabaseName());
        for (EntityT e : entities) {
            String id = entityMetadata.getIdReader().read(e);
            if ("".equals(id) || id == null) {
                id = generateId(e, (Class<EntityT>) e.getClass());
                entityMetadata.getIdWriter().write(e, id);
                log.debug("New ID {} generated for bulk saved document", id);
            }
        }

        if (entityMetadata.isViewed()) {
            localMapper = new ObjectMapper();
            SimpleModule module = new SimpleModule();
            module.addSerializer(new ViewedDocumentSerializer<>(clazz, entityMetadata.getTypeField(), entityMetadata.getType()));
            localMapper.registerModule(module);
        }

        List<DocumentPutResponse> responses = post(getURI(baseURI, entityMetadata.getDatabaseName(), "_bulk_docs"),
                localMapper.writeValueAsString(new BulkRequest<>(entities)), r -> mapper.readValue(r.getEntity().getContent(),
                        mapper.getTypeFactory().constructCollectionType(List.class, DocumentPutResponse.class)));
        Map<String, DocumentPutResponse> indexed = responses.stream().collect(Collectors.toMap(DocumentPutResponse::getId, r -> r));
        for (EntityT e : entities) {
            DocumentPutResponse response = indexed.get(entityMetadata.getIdReader().read(e));
            if ("true".equals(response.getOk())) {
                entityMetadata.getRevisionWriter().write(e, response.getRev());
                entityMetadata.getIdWriter().write(e, response.getId());
            } else {
                log.warn("Document {} with id: {} and rev: {} saving failed with reason {}", e, response.getId(), response.getRev(), response.getError());
            }

        }
        return entities;
    }

    /**
     * Reads document with the given id into instance of the given class
     *
     * @param id        Id of wanted document. Must not be {@literal null}
     * @param clazz     of entity in which document will be read. Must not be {@literal null}
     * @param <EntityT> type of entity
     * @return Instance of the given class with data of document
     * @throws IOException if http request is not successful or json processing fail
     * @see DocumentBase
     */
    public <EntityT> @NotNull EntityT read(@NotNull String id, @NotNull Class<EntityT> clazz) throws IOException {
        String databaseName = getDatabaseName(clazz);
        log.debug("Read of document with ID {} from database {}", id, databaseName);
        return get(getURI(baseURI, databaseName, id), r -> mapper.readValue(r.getEntity().getContent(), clazz));
    }

    /**
     * Method to get design document of the given name from database which name is obtained from the given class.
     *
     * @param id    of wanted design document
     * @param clazz from which is obtained database name where design document should be stored
     * @return instance of the wanted {@link DesignDocument}.
     * @throws IOException if http request is not successful or json processing fail
     */
    public @NotNull DesignDocument readDesign(@NotNull String id, @NotNull Class<?> clazz) throws IOException {
        return readDesign(id, getDatabaseName(clazz));
    }

    /**
     * Method to get design document of the given name from the database of the given name.
     *
     * @param id           of wanted design document
     * @param databaseName of database where design document should be stored
     * @return instance of the wanted {@link DesignDocument}
     * @throws IOException if http request is not successful or json processing fail
     */
    public @NotNull DesignDocument readDesign(@NotNull String id, @NotNull String databaseName) throws IOException {
        log.debug("Read of design with ID {} from database {}", id, databaseName);
        return get(getURI(baseURI, databaseName, "_design", id), r -> mapper.readValue(r.getEntity().getContent(), DesignDocument.class));
    }

    /**
     * Method to get design document of the given name from database which name is obtained from the given class. Differently from
     * {@link #readDesign(String, Class)}, method do not throw IOException in case of {@link HttpStatus#SC_NOT_FOUND} status but returns empty Optional
     * instead.
     *
     * @param id    of wanted design document
     * @param clazz from which is obtained database name where design document should be stored
     * @return instance of the wanted {@link DesignDocument} wrapped in {@link Optional} or empty {@link Optional} is design not found.
     * @throws IOException if http request is not successful or json processing fail
     */
    public @NotNull Optional<DesignDocument> readDesignSafely(@NotNull String id, @NotNull Class<?> clazz) throws IOException {
        return readDesignSafely(id, getDatabaseName(clazz));
    }

    /**
     * Method to get design document of the given name from database which name is obtained from the given class. Differently from
     * {@link #readDesign(String, Class)}, method do not throw IOException in case of {@link HttpStatus#SC_NOT_FOUND} status but returns empty Optional
     * instead.
     *
     * @param id           of wanted design document
     * @param databaseName of database where design document should be stored
     * @return instance of the wanted {@link DesignDocument} wrapped in {@link Optional} or empty {@link Optional} is design not found.
     * @throws IOException if http request is not successful or json processing fail
     */
    public Optional<DesignDocument> readDesignSafely(@NotNull String id, @NotNull String databaseName) throws IOException {
        try {
            return Optional.of(readDesign(id, databaseName));
        } catch (CouchDbException couchDbException) {
            if (couchDbException.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                return Optional.empty();
            } else {
                throw couchDbException;
            }
        }
    }

    /**
     * Method for reading all documents of given ids. Read is done in a bulk request. If a id is not found, not entity is returned for the id. Count of ids
     * might not match with count of returned entities.
     *
     * @param ids       of wanted documents. Must not be {@literal null}
     * @param clazz     of documents. Must not be {@literal null}
     * @param <EntityT> type of documents. Must not be {@literal null}
     * @return {@link Iterable} of read documents. Can not be {@literal null}
     * @throws IOException if http request is not successful or json processing fail
     */
    public <EntityT> @NotNull List<EntityT> readAll(@NotNull Iterable<String> ids, @NotNull Class<EntityT> clazz) throws IOException {
        ObjectMapper localMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(List.class, new BulkGetDeserializer<>(clazz));
        localMapper.registerModule(module);
        log.debug("Bulk read of {} document from database {} with the following IDs: {}",
                LazyLog.of(() -> StreamSupport.stream(ids.spliterator(), false).count()),
                getDatabaseName(clazz),
                LazyLog.of(() -> String.join(", ", ids)));
        BulkGetResponse<EntityT> response = post(getURI(baseURI, getDatabaseName(clazz), "_bulk_get"), localMapper.writeValueAsString(new BulkGetRequest(ids)),
                r -> localMapper.readValue(r.getEntity().getContent(), localMapper.getTypeFactory().constructParametricType(BulkGetResponse.class, clazz)));
        log.info("Bulk read of {} ids result contains {} documents", LazyLog.of(() -> StreamSupport.stream(ids.spliterator(), false).count()),
                response.getDocs().size());
        return response.getDocs();
    }

    /**
     * Method using view to get all document ids. If entity {@link EntityMetadata#isViewed()} than the configured view for the configured design is used. If
     * entity is not viewed, the expected data view from the all design is used. Method supports pagination. If design documents are needed, use
     * {@link #readAllDesign(Class)}.
     * {@link #readAllDocsWithoutView(Class, Predicate)} can be used if no default view is present.
     *
     * @param clazz of wanted entity. Used to get database name {@link #getDatabaseName(Class)}. Must not be {@literal null}
     * @param skip  number of skipped documents. 0 means no document is skipped
     * @param limit of document in a result. Can be {@literal null} if no limitation is wanted.
     * @param sort  information for the result
     * @return all or limited result of documents from database depending on the given class
     * @throws IOException if http request is not successful or json processing fail
     */
    public @NotNull List<String> readAll(@NotNull Class<?> clazz, Long skip, @Nullable Integer limit, @NotNull Sort sort) throws IOException {
        String design = ALL_DESIGN;
        String view = ALL_DATA_VIEW;
        EntityMetadata<?> em = getEntityMetadata(clazz);
        if (sort.isSorted()) {
            Sort.Direction direction = null;
            for (Sort.Order order : sort) {
                direction = assertSameDirection(direction, order.getDirection());
            }
            Pair<String, String> designAndView = getSortedViewId(sort, em);
            design = designAndView.getFirst();
            view = designAndView.getSecond();
        } else if (em.isViewed()) {
            design = em.getDesign();
            view = em.getView();
        }

        return readFromView(em.getDatabaseName(), design, view, skip, limit, sort);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private @NotNull List<String> readFromView(@NotNull String database, @NotNull String design, @NotNull String view, Long skip, @Nullable Integer limit,
                                               @NotNull Sort sort) throws IOException {
        List<NameValuePair> parameters = new ArrayList<>(4);
        if (sort.isSorted()) {
            parameters.add(new BasicNameValuePair("descending", sort.stream().findFirst().get().getDirection() == Sort.Direction.DESC ? "true" : "false"));
        }
        if (skip != null) {
            parameters.add(new BasicNameValuePair(VIEW_SKIP_PARAMETER, skip + ""));
        }
        if (limit != null) {
            parameters.add(new BasicNameValuePair(VIEW_LIMIT_PARAMETER, limit + ""));
        }

        parameters.add(new BasicNameValuePair(VIEW_REDUCE_PARAMETER, Boolean.toString(false)));

        return get(getURI(baseURI, Arrays.asList(database, "_design", design, "_view", view), parameters),
                r -> mapper.readValue(r.getEntity().getContent(), AllDocumentResponse.class).getRows());
    }

    private Pair<String, String> getSortedViewId(Sort sort, EntityMetadata<?> em) throws IOException {
        String sortViewId = "sorted-by-" + (em.isViewed() ? em.getType() + "-" : "") +
                sort.stream().map(o -> o.getProperty().replace(".", "-")).collect(Collectors.joining(":"));
        String designId = em.isViewed() ? em.getDesign() : ALL_DESIGN;
        DesignDocument design = readDesign(designId, em.getDatabaseName());
        if (!knownSortedViews.contains(sortViewId)) {
            if (!design.getViews().containsKey(sortViewId)) {
                View view;
                String sortKey = sort.stream().map(o -> "doc." + o.getProperty()).collect(Collectors.joining(","));
                if (em.isViewed()) {
                    view = new View(sortViewId, String.format(SORTED_TYPED_VIEW_MAP, em.getTypeField(), em.getType(), sortKey), COUNT_REDUCE);
                } else {
                    view = new View(sortViewId, String.format(SORTED_VIEW_MAP, sortKey), COUNT_REDUCE);
                }
                design.addView(view);
                saveDesign(design, em.getDatabaseName());
            }
            knownSortedViews.add(sortViewId);
        }
        return Pair.of(designId, sortViewId);
    }

    /**
     * Method tests if the two given directions are the same. The test is necessary because CouchDB is not able to mix sort directions in Mango query.
     *
     * @param expected expected direction. Can be {@literal null} which means do not test the current
     * @param current  tested direction that must match the expected
     * @return everytime returns current. Can not be {@literal null}
     */
    public static @NotNull Sort.Direction assertSameDirection(@Nullable Sort.Direction expected, @NotNull Sort.Direction current) {
        if (expected != null) {
            if (expected != current) {
                throw new IllegalStateException("CouchDB is not able to mix sort directions");
            }
        }
        return current;
    }

    /**
     * Method using view to get document count. If entity {@link EntityMetadata#isViewed()} than the configured view for the configured design is used. If
     * entity is not viewed, the expected data view from the all design is used.
     *
     * @param clazz of wanted entity. Used to get database name {@link #getDatabaseName(Class)}. Must not be {@literal null}
     * @return {@literal non-null} count of documents of the given entity
     * @throws IOException if http request is not successful or json processing fail
     * @see com.groocraft.couchdb.slacker.annotation.Document
     */
    public long countAll(@NotNull Class<?> clazz) throws IOException {
        EntityMetadata<?> em = getEntityMetadata(clazz);
        String design = ALL_DESIGN;
        String view = ALL_DATA_VIEW;
        if (em.isViewed()) {
            design = em.getDesign();
            view = em.getView();
        }
        return get(getURI(baseURI, em.getDatabaseName(), "_design", design, "_view", view),
                r -> {
                    JsonNode rows = mapper.readValue(r.getEntity().getContent(), ObjectNode.class).get("rows");
                    if (rows.has(0)) {
                        return rows.get(0).get("value").asLong();
                    } else {
                        return 0L;
                    }
                });
    }

    /**
     * {@link #readAll(Class, Long, Integer, Sort)} where skip is 0 and limit null.
     *
     * @param clazz of wanted entity. Used to get database name {@link #getDatabaseName(Class)}. Must not be {@literal null}
     * @return ids of all entities
     * @throws IOException if http request is not successful or json processing fail
     * @see #readAll(Class, Long, Integer, Sort)
     * @see com.groocraft.couchdb.slacker.annotation.Document
     */
    public @NotNull List<String> readAll(@NotNull Class<?> clazz) throws IOException {
        return readAll(clazz, null, null, Sort.unsorted());
    }

    /**
     * Method to get result of _design_docs which contains only design documents (If you need non-design documents use {@link #readAll(Class)} or
     * {@link #readAllDocsWithoutView(Class, Predicate)} (Class, Predicate)} if you want both) to the database specified by
     * {@link com.groocraft.couchdb.slacker.annotation.Document} from the passed entity class. Result is returned as Stream of documents ids, no full data.
     *
     * @param clazz of wanted entity. Used to get database name {@link #getDatabaseName(Class)}. Must not be {@literal null}
     * @return {@literal non-null} document ids
     * @throws IOException if http request is not successful or json processing fail
     * @see #readAll(Class, Long, Integer, Sort)
     */
    public @NotNull List<String> readAllDesign(@NotNull Class<?> clazz) throws IOException {
        String databaseName = getDatabaseName(clazz);
        log.debug("Read of all design documents from database {}", databaseName);
        return get(getURI(baseURI, databaseName, "_design_docs"),
                r -> mapper.readValue(r.getEntity().getContent(), AllDocumentResponse.class).getRows());
    }

    /**
     * Method to get result of _all_docs with possibility of id filtering (If you need non-design documents use {@link #readAll(Class)} or
     * {@link #readAllDesign(Class)} if you want design document only) to the database specified by {@link com.groocraft.couchdb.slacker.annotation.Document}
     * from the passed entity class. Result is returned as Stream of documents ids, no full data.
     *
     * @param clazz             of wanted entity. Used to get database name {@link #getDatabaseName(Class)}. Must not be {@literal null}
     * @param idFilterPredicate with filtering rule to id. Must not be {@literal null}. Use {@code s -> true} to disable filtering.
     * @return Stream of {@link String} which contain id.
     * @throws IOException if http request is not successful or json processing fail
     * @see #readAll(Class)
     * @see #readAllDesign(Class)
     */
    public @NotNull List<String> readAllDocsWithoutView(@NotNull Class<?> clazz, @NotNull Predicate<String> idFilterPredicate) throws IOException {
        return get(getURI(baseURI, getDatabaseName(clazz), "_all_docs"),
                r -> mapper.readValue(r.getEntity().getContent(), AllDocumentResponse.class).getRows().stream().filter(idFilterPredicate).collect(Collectors.toList()));
    }

    /**
     * Deletes given entity. From entity id and revision is used.
     *
     * @param entity    to delete. Must not be {@literal null}
     * @param <EntityT> type of entity
     * @return deleted entity
     * @throws IOException if http request is not successful or json processing fail
     * @see DocumentBase
     */
    public <EntityT> @NotNull EntityT delete(@NotNull EntityT entity) throws IOException {
        EntityMetadata<?> entityMetadata = getEntityMetadata(entity.getClass());
        String id = entityMetadata.getIdReader().read(entity);
        String revision = entityMetadata.getRevisionReader().read(entity);
        log.debug("Delete of document with id {} and revision {} from database {}", id, revision, entityMetadata.getDatabaseName());
        return delete(getURI(baseURI, Arrays.asList(entityMetadata.getDatabaseName(), id), Collections.singletonList(new BasicNameValuePair("rev", revision))),
                r -> entity);
    }

    /**
     * Method to remove all documents from database. Method is implemented as read all ids and than delete by id. Both mentioned functions are bulk
     * operations. As a consequence of the mentioned approach, DB deletes only document existing in the time of call, not documents created after the request
     * . Name of the database in which delete is executed is read from given class.
     *
     * @param clazz     with {@link com.groocraft.couchdb.slacker.annotation.Document} annotation
     * @param <EntityT> Type of entity in the database
     * @return {@link List} of deleted documents
     * @throws IOException if http request is not successful or json processing fail
     */
    public <EntityT> @NotNull List<EntityT> deleteAll(@NotNull Class<EntityT> clazz) throws IOException {
        log.debug("Delete of all documents from database {}", getDatabaseName(clazz));
        return deleteAll(readAll(readAll(clazz), clazz), clazz);
    }

    /**
     * Method to delete given documents. A bulk operation is used.
     *
     * @param entities  {@link List} of entities to be erased
     * @param clazz     of given entities
     * @param <EntityT> type of entities
     * @return {@link Iterable} of deleted entities
     * @throws IOException if http request is not successful or json processing fail
     */
    @SuppressWarnings("DuplicatedCode")
    public <EntityT> @NotNull List<EntityT> deleteAll(@NotNull Iterable<EntityT> entities, @NotNull Class<?> clazz) throws IOException {
        EntityMetadata<?> entityMetadata = getEntityMetadata(clazz);
        log.debug("Bulk delete of {} documents from database {}", LazyLog.of(() -> StreamSupport.stream(entities.spliterator(), false).count()),
                entityMetadata.getDatabaseName());
        ObjectMapper localMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(entityMetadata.isViewed() ?
                new DeleteViewedDocumentSerializer<>(clazz, entityMetadata.getTypeField(), entityMetadata.getType()) :
                new DeleteDocumentSerializer<>(clazz));
        // parameters of type
        localMapper.registerModule(module);
        List<DocumentPutResponse> responses = post(getURI(baseURI, entityMetadata.getDatabaseName(), "_bulk_docs"),
                localMapper.writeValueAsString(new BulkRequest<>(entities)), r -> mapper.readValue(r.getEntity().getContent(),
                        mapper.getTypeFactory().constructCollectionType(List.class, DocumentPutResponse.class)));
        Map<String, DocumentPutResponse> indexed = responses.stream().collect(Collectors.toMap(DocumentPutResponse::getId, r -> r));
        List<EntityT> deleted = new LinkedList<>();
        for (EntityT e : entities) {
            DocumentPutResponse response = indexed.get(entityMetadata.getIdReader().read(e));
            if ("true".equals(response.getOk())) {
                entityMetadata.getRevisionWriter().write(e, response.getRev());
                deleted.add(e);
            } else {
                log.warn("Document {} with id: {} and rev: {} deleting failed with reason {}", e, response.getId(), response.getRev(), response.getError());
            }

        }
        return deleted;
    }

    /**
     * Deletes document from database based on {@link com.groocraft.couchdb.slacker.annotation.Document} annotation of given {@code clazz} with the given id.
     * The method deletes latest document, without any consistency check.
     *
     * @param id        of document which should be deleted
     * @param clazz     of entity to get database
     * @param <EntityT> type of entity
     * @return deleted entity if any
     * @throws IOException if http request is not successful or json processing fail
     */
    public <EntityT> @NotNull EntityT deleteById(@NotNull String id, @NotNull Class<EntityT> clazz) throws IOException {
        EntityMetadata<EntityT> entityMetadata = getEntityMetadata(clazz);
        EntityT entity = read(id, clazz);
        log.debug("Delete of document with id {} and revision {} from database {}", id, LazyLog.of(() -> entityMetadata.getRevisionReader().read(entity)),
                entityMetadata.getDatabaseName());
        return delete(getURI(baseURI, Arrays.asList(entityMetadata.getDatabaseName(), id),
                Collections.singletonList(new BasicNameValuePair("rev", entityMetadata.getRevisionReader().read(entity)))),
                r -> entity);
    }

    /**
     * Executes the given Mango query and maps a result into the given entity. To better performance index is needed. If mango query is created from generic
     * query method {@link com.groocraft.couchdb.slacker.annotation.Index} annotation can be used to specify the index(es).
     *
     * @param json      query of valid Mango query. Must not be {@literal null}
     * @param clazz     of entities expected as result. Must not be {@literal null}
     * @param <EntityT> type of entity
     * @return pair of bookmark of result and {@link List} of instances of the given class with result of the given class
     * @throws IOException if http request is not successful or json processing fail
     * @see DocumentBase
     */
    public <EntityT> @NotNull Pair<List<EntityT>, String> find(@NotNull String json, @NotNull Class<EntityT> clazz) throws IOException {
        ObjectMapper localMapper = new ObjectMapper();
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(List.class, new FoundDocumentDeserializer<>(clazz));
        log.debug("Executing Mango query {}", json);
        localMapper.registerModule(simpleModule);
        DocumentFindResponse<EntityT> response = post(getURI(baseURI, getDatabaseName(clazz), "_find"), json, r -> localMapper.readValue(r.getEntity().getContent(),
                localMapper.getTypeFactory().constructParametricType(DocumentFindResponse.class, clazz)));
        log.debug("Mango query executed with result of {} documents", response.getDocuments().size());
        response.getWarning().ifPresent(w -> log.info("{} for query {}", w, json));
        response.getExecutionStats().ifPresent(s -> log.info("{} for query {}", s, json));
        return Pair.of(response.getDocuments(), response.getBookmark());
    }

    /**
     * Executes the given request as mango or view lookup.
     *
     * @param request   that will be executed depending on query strategy configuration. Must not be {@literal null}
     * @param clazz     that will be used to as used to obtain database name. Must not be {@literal null}
     * @param <EntityT> type of entities that should be in a result of query
     * @return {@link FindResult} with entities matching the provided request and bookmarks if the configured strategy returns it.
     * @throws IOException if http request is not successful or json processing fail
     */
    public <EntityT> @NotNull FindResult<EntityT> find(@NotNull FindRequest request, @NotNull Class<EntityT> clazz) throws IOException {
        return find(request, clazz, null);
    }

    /**
     * Executes the given request as mango or view lookup. If configured query strategy provides bookmarks, the {@code bookmarkBy} parameter is used as size
     * of page for that bookmark will be generated.
     *
     * @param request    that will be executed depending on query strategy configuration. Must not be {@literal null}
     * @param clazz      that will be used to obtain database name. Must not be {@literal null}
     * @param bookmarkBy number of how many documents should be between two bookmarks. Affects performance, null turns of chunking to bookmarks
     * @param <EntityT>  type of entities that should be in a result of query
     * @return {@link FindResult} with entities matching the provided request and bookmarks if the configured strategy returns it.
     * @throws IOException if http request is not successful or json processing fail
     */
    public <EntityT> @NotNull FindResult<EntityT> find(@NotNull FindRequest request, @NotNull Class<EntityT> clazz,
                                                       @Nullable Integer bookmarkBy) throws IOException {
        if (queryStrategy == QueryStrategy.MANGO) {
            return findByMango(request, clazz, bookmarkBy);
        } else {
            return findByView(request, clazz);
        }

    }

    /**
     * Method used if {@link QueryStrategy#VIEW} is configured. Method uses {@link #ensureView(Sort, String, Class)} method to create (or obtain the existing
     * one) view where mapping  function is matching the provided request. The view is used as source of a result.
     *
     * @param request   that will be executed. Must not be {@literal null}
     * @param clazz     that will be used to obtain database name. Must not be {@literal null}
     * @param <EntityT> type of entities that should be in a result of query
     * @return {@link FindResult} with entities matching the provided request and bookmarks if the configured strategy returns it.
     * @throws IOException if http request is not successful or json processing fail
     */
    public <EntityT> @NotNull FindResult<EntityT> findByView(@NotNull FindRequest request, @NotNull Class<EntityT> clazz) throws IOException {
        String designId = ensureView(request.getSort(), request.getJavaScriptCondition(), clazz);
        List<EntityT> entities = readAll(readFromView(getDatabaseName(clazz), designId, ALL_DATA_VIEW, request.getSkip(), request.getLimit(),
                request.getSort()), clazz);
        return FindResult.of(entities, Collections.emptyMap());
    }

    /**
     * Method to obtain total amount of documents matching the given request. It depends on the configured query strategy if mango or view is used to process
     * the given request.
     *
     * @param request that will be executed. Must not be {@literal null}
     * @param clazz   that will be used to obtain database name. Must not be {@literal null}
     * @return total amount of entities matching the given request
     * @throws IOException if http request is not successful or json processing fail
     */
    public long count(@NotNull FindRequest request, @NotNull Class<?> clazz) throws IOException {
        if (queryStrategy == QueryStrategy.MANGO) {
            request.setLimit(null);
            request.setSkip(null);
            request.setBookmark(null);
            return findByMango(request, clazz, null).getEntities().size();
        } else {
            return countByView(request, clazz);
        }
    }

    /**
     * Method to obtain total amount of documents matching the given request. Method uses {@link #ensureView(Sort, String, Class)} method to create (or obtain the existing
     * one) view where mapping function is matching the provided request. The view is used in reduce mode with _count to get total.
     *
     * @param request that will be executed. Must not be {@literal null}
     * @param clazz   that will be used to obtain database name. Must not be {@literal null}
     * @return total amount of entities matching the given request
     * @throws IOException if http request is not successful or json processing fail
     */
    public long countByView(@NotNull FindRequest request, @NotNull Class<?> clazz) throws IOException {
        String designId = ensureView(request.getSort(), request.getJavaScriptCondition(), clazz);

        return get(getURI(baseURI, getDatabaseName(clazz), "_design", designId, "_view", ALL_DATA_VIEW),
                r -> {
                    JsonNode rows = mapper.readValue(r.getEntity().getContent(), ObjectNode.class).get("rows");
                    if (rows.has(0)) {
                        return rows.get(0).get("value").asLong();
                    } else {
                        return 0L;
                    }
                });
    }

    /**
     * Method to ensure that a view matching the given javascript condition and sort exists or will be created. Sort is used to determine the key of view.
     * If there is not sort, view emits null. The given javascript is used in mapping function of the view.
     *
     * @param sort                of the view. Must not be {@literal null}
     * @param javaScriptCondition of mapping function of the view. Must not be {@literal null}
     * @param clazz               that will be used to obtain database name. Must not be {@literal null}
     * @return name of the view with mapping function matching the given javascript condition and key(s) with the given sort.
     * @throws IOException if http request is not successful or json processing fail
     */
    private String ensureView(@NotNull Sort sort, @NotNull String javaScriptCondition, @NotNull Class<?> clazz) throws IOException {
        String mapFunction;
        if (sort.isSorted()) {
            Sort.Direction direction = null;
            for (Sort.Order order : sort) {
                direction = assertSameDirection(direction, order.getDirection());
            }
            String key = sort.stream().map(o -> "doc." + o.getProperty()).collect(Collectors.joining(","));
            mapFunction = String.format(SORTED_FIND_VIEW_MAP, javaScriptCondition, key);
        } else {
            mapFunction = String.format(FIND_VIEW_MAP, javaScriptCondition);
        }

        Optional<DesignDocument> design = readDesignSafely(mapFunction.hashCode() + "", clazz);
        if (!design.isPresent()) {
            View view = new View(ALL_DATA_VIEW, mapFunction, COUNT_REDUCE);
            DesignDocument newDesign = new DesignDocument(mapFunction.hashCode() + "", Collections.singleton(view));
            saveDesign(newDesign, clazz);
        }
        return mapFunction.hashCode() + "";
    }

    /**
     * Method used if {@link QueryStrategy#MANGO} is configured. Method uses translates the given request to mango json and executes it.
     *
     * @param request    that will be executed. Must not be {@literal null}
     * @param clazz      that will be used to obtain database name. Must not be {@literal null}
     * @param bookmarkBy number of how many documents should be between two bookmarks. Affects performance, null turns of chunking to bookmarks
     * @param <EntityT>  type of entities that should be in a result of query
     * @return {@link FindResult} with entities matching the provided request and bookmarks if the configured strategy returns it.
     * @throws IOException if http request is not successful or json processing fail
     */
    public <EntityT> @NotNull FindResult<EntityT> findByMango(@NotNull FindRequest request, @NotNull Class<EntityT> clazz,
                                                              @Nullable Integer bookmarkBy) throws IOException {
        Integer originalLimit = request.getLimit();
        Sort sort = request.getSort();

        if (sort.isSorted()) {
            createIndex(sort, clazz);
        }

        List<EntityT> result = new LinkedList<>();
        Pair<List<EntityT>, String> r;
        Map<Integer, String> bookmarks = new HashMap<>();
        int limit = bookmarkBy == null ? bulkMaxSize : bookmarkBy;
        if (originalLimit == null || bookmarkBy != null) {
            request.setLimit(limit);
        }
        do {
            //if we are limited by request and we can see that next request cause overflow of the limit, we request as less as needed.
            if (originalLimit != null && result.size() + limit > originalLimit) {
                limit = originalLimit - result.size();
                request.setLimit(limit);
            }
            String query = mapper.writeValueAsString(request);
            r = find(query, clazz);
            result.addAll(r.getFirst());
            bookmarks.put(result.size(), r.getSecond());
            request.setBookmark(r.getSecond());
            //We repeat whole process with next bookmark until we have all documents or as much document as limit from request
        } while ((originalLimit != null && result.size() < originalLimit && r.getFirst().size() == limit)
                || (originalLimit == null && r.getFirst().size() == limit));

        return FindResult.of(result, bookmarks);
    }

    /**
     * Method to create new index by the given parameters. All order rules must be in the same direction (it is a limitation of CouchDB)
     *
     * @param name        of the created index. Must not be {@literal null}
     * @param entityClass as definition of database in which index should be created. Must not be {@literal null}
     * @param fields      list of {@link org.springframework.data.domain.Sort.Order} which from index should be done
     * @throws IOException if http request is not successful or json processing fail
     */
    public void createIndex(@NotNull String name, @NotNull Class<?> entityClass, Sort.Order... fields) throws IOException {
        createIndex(name, entityClass, fields == null ? Collections.emptyList() : Arrays.asList(fields));
    }

    /**
     * Method to create new index by the given parameters. All order rules must be in the same direction (it is a limitation of CouchDB)
     *
     * @param name        of the created index. Must not be {@literal null}
     * @param entityClass as definition of database in which index should be created. Must not be {@literal null}
     * @param fields      list of {@link org.springframework.data.domain.Sort.Order} which from index should be done. Must not be {@literal null}
     * @throws IOException if http request is not successful or json processing fail
     */
    public void createIndex(@NotNull String name, @NotNull Class<?> entityClass, @NotNull Iterable<Sort.Order> fields) throws IOException {
        createIndex(name, getDatabaseName(entityClass), fields);
    }

    /**
     * Method to create new index by the given parameters. All order rules must be in the same direction (it is a limitation of CouchDB)
     *
     * @param name   of the created index. Must not be {@literal null}
     * @param dbName in which index should be created. Must not be {@literal null}
     * @param fields list of {@link org.springframework.data.domain.Sort.Order} which from index should be done. Must not be {@literal null}
     * @throws IOException if http request is not successful or json processing fail
     */
    public void createIndex(@NotNull String name, @NotNull String dbName, @NotNull Iterable<Sort.Order> fields) throws IOException {
        Sort.Direction direction = null;
        for (Sort.Order order : fields) {
            direction = CouchDbClient.assertSameDirection(direction, order.getDirection());
        }

        log.debug("Creating index with name {} in database {} and ordering {}", name, dbName,
                LazyLog.of(() -> StreamSupport.stream(fields.spliterator(), false).map(Sort.Order::toString).collect(Collectors.joining(", "))));
        post(getURI(baseURI, dbName, "_index"), mapper.writeValueAsString(new IndexCreateRequest(name, fields)), r -> null);
    }

    /**
     * Method to create new index by the given parameters. All order rules must be in the same direction (it is a limitation of CouchDB)
     *
     * @param sort  from that index should be done. Must not be {@literal null}
     * @param clazz as definition of database in which index should be created. Must not be {@literal null}
     * @throws IOException if http request is not successful or json processing fail
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private void createIndex(@NotNull Sort sort, @NotNull Class<?> clazz) throws IOException {
        Assert.isTrue(sort.isSorted(), "Sort must contain at leas one Order for creating index");
        String indexId = sort.stream().map(Sort.Order::getProperty).collect(Collectors.joining("-"))
                + "-" + sort.stream().findFirst().get().toString().toLowerCase();
        if (!knownIndexes.contains(indexId)) {
            createIndex(indexId, clazz, sort);
            knownIndexes.add(indexId);
        }
    }

    /**
     * Method tests if database with name obtained from the given class exists.
     *
     * @param clazz which from is obtained a database name. Must not be {@literal null}
     * @return true if database with the given name exists.
     * @throws IOException if http request is not successful
     */
    public boolean databaseExists(@NotNull Class<?> clazz) throws IOException {
        return databaseExists(getDatabaseName(clazz));
    }

    /**
     * Method tests if database with the given name exists.
     *
     * @param name of database. Must not be {@literal null}
     * @return true if database with the given name exists.
     * @throws IOException if http request is not successful
     */
    public boolean databaseExists(@NotNull String name) throws IOException {
        try {
            head(getURI(baseURI, name));
            return true;
        } catch (CouchDbException ex) {
            if (ex.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                return false;
            }
            throw ex;
        }
    }

    /**
     * Method to create new index by the given parameters.
     *
     * @param clazz from which database name is resolved. Must not be {@literal null}
     * @throws IOException if http request is not successful or json processing fail
     */
    public void createDatabase(@NotNull Class<?> clazz) throws IOException {
        createDatabase(getDatabaseName(clazz));
    }

    /**
     * Method to create new index by the given parameters.
     *
     * @param clazz         from which database name is resolved. Must not be {@literal null}
     * @param shardsCount   number of shards of the created database
     * @param replicasCount number of replicas of the created database
     * @param partitioned   flag if the created database should or not to be partitioned
     * @throws IOException if http request is not successful or json processing fail
     */
    public void createDatabase(@NotNull Class<?> clazz, int shardsCount, int replicasCount, boolean partitioned) throws IOException {
        createDatabase(getDatabaseName(clazz), shardsCount, replicasCount, partitioned);
    }

    /**
     * Method to create new index by the given parameters.
     *
     * @param name of database. Must not be {@literal null}
     * @throws IOException if http request is not successful or json processing fail
     */
    public void createDatabase(@NotNull String name) throws IOException {
        createDatabase(name, defaultShards, defaultReplicas, defaultPartitioned);
    }

    /**
     * Method to create new index by the given parameters.
     *
     * @param name          of database. Must not be {@literal null}
     * @param shardsCount   number of shards of the created database
     * @param replicasCount number of replicas of the created database
     * @param partitioned   flag if the created database should or not to be partitioned
     * @throws IOException if http request is not successful or json processing fail
     */
    public void createDatabase(@NotNull String name, int shardsCount, int replicasCount, boolean partitioned) throws IOException {
        log.debug("Creating {} database with name {}, {} shards, {} replicas", LazyLog.of(() -> partitioned ? "portioned" : "non-portioned"), name,
                shardsCount, replicasCount);
        put(getURI(baseURI, Collections.singletonList(name),
                Arrays.asList(new BasicNameValuePair("q", "" + shardsCount), new BasicNameValuePair("n", replicasCount + ""),
                        new BasicNameValuePair("partitioned", Boolean.toString(partitioned)))), "", r -> null);
    }

    /**
     * Deletes whole database with a name obtained from the given class.
     *
     * @param clazz from which is obtained the name of deleted database. Must not be {@literal null}
     * @throws IOException if http request is not successful
     */
    public void deleteDatabase(@NotNull Class<?> clazz) throws IOException {
        deleteDatabase(getDatabaseName(clazz));
    }

    /**
     * Deletes whole database with the given name.
     *
     * @param name of database which should be deleted. Must not be {@literal null}
     * @throws IOException if http request is not successful
     */
    public void deleteDatabase(@NotNull String name) throws IOException {
        delete(getURI(baseURI, name), r -> null);
        log.info("Database {} deleted", name);
    }

    /**
     * Method for storing a design document to the database which is obtained from the given class. ID of the given design document must not be {@literal
     * null} nor empty.
     *
     * @param designDocument which should be saved. Must not be {@literal null}
     * @param clazz          which is used as source of database name where to store the given document. Must not be {@literal null}
     * @return Stored {@link DesignDocument} with updated id and revision
     * @throws IOException if http request is not successful
     */
    public @NotNull DesignDocument saveDesign(DesignDocument designDocument, Class<?> clazz) throws IOException {
        return saveDesign(designDocument, getDatabaseName(clazz));
    }

    /**
     * Method for storing a design document to the database with the given name. ID of the given design document must not be {@literal
     * null} nor empty.
     *
     * @param designDocument which should be saved. Must not be {@literal null}
     * @param databaseName   where to store the given document. Must not be {@literal null}
     * @return Stored {@link DesignDocument} with updated id and revision
     * @throws IOException if http request is not successful
     */
    public @NotNull DesignDocument saveDesign(@NotNull DesignDocument designDocument, @NotNull String databaseName) throws IOException {
        Assert.hasText(designDocument.getId(), "DesignDocument.Id must not be null");
        log.debug("Saving design with id {} and revision {} to database {}", designDocument.getId(), designDocument.getRevision(), databaseName);
        DocumentPutResponse response = put(getURI(baseURI, databaseName, designDocument.getId()), mapper.writeValueAsString(designDocument),
                r -> mapper.readValue(r.getEntity().getContent(), DocumentPutResponse.class));
        designDocument.setRevision(response.getRev());
        log.debug("Saved design with id {} and revision {}", response.getId(), response.getRev());
        return designDocument;
    }

    /**
     * Method to send PUT request with the given body with possibility to safely process response. HTTP response is closed immediately after
     * {@code responseProcessor} is called. Body is expected as json, content type is hard coded as application/json.
     *
     * @param uri               of target. Must not be {@literal null}
     * @param json              body of the request. Must not be {@literal null}
     * @param responseProcessor {@link ThrowingFunction} to process response before the stream is closed. Must not be {@literal null}
     * @param <DataT>           type of returned data which are created by {@code responseProcessor}
     * @return data created in {@code responseProcessor} based on response of PUT request with the given {@code json} on the given {@code uri}
     * @throws IOException if http request is not successful or json processing fail
     */
    private <DataT> DataT put(@NotNull URI uri, @NotNull String json, @NotNull ThrowingFunction<HttpResponse, DataT, IOException> responseProcessor) throws IOException {
        try (AutoCloseableHttpResponse response = new AutoCloseableHttpResponse()) {
            final HttpPut put = new HttpPut(uri);
            StringEntity entity = new StringEntity(json, "UTF-8");
            entity.setContentType("application/json");
            put.setEntity(entity);
            response.set(execute(put));
            return responseProcessor.apply(response.get());
        }
    }

    /**
     * Method to send POST request with the given body with possibility to safely process response. HTTP response is closed immediately after
     * {@code responseProcessor} is called. Body is expected as json, content type is hard coded as application/json.
     *
     * @param uri               of target. Must not be {@literal null}
     * @param json              body of the request. Must not be {@literal null}
     * @param responseProcessor {@link ThrowingFunction} to process response before the stream is closed. Must not be {@literal null}
     * @param <DataT>           type of returned data which are created by {@code responseProcessor}
     * @return data created in {@code responseProcessor} based on response of POST request with the given {@code json} on the given {@code uri}
     * @throws IOException if http request is not successful or json processing fail
     */
    private <DataT> DataT post(@NotNull URI uri, @NotNull String json, @NotNull ThrowingFunction<HttpResponse, DataT, IOException> responseProcessor) throws IOException {
        try (AutoCloseableHttpResponse response = new AutoCloseableHttpResponse()) {
            final HttpPost post = new HttpPost(uri);
            StringEntity entity = new StringEntity(json, "UTF-8");
            entity.setContentType("application/json");
            post.setEntity(entity);
            response.set(execute(post));
            return responseProcessor.apply(response.get());
        }
    }

    /**
     * Method to send GET request with possibility to safely process response. HTTP response is closed immediately after
     * {@code responseProcessor} is called. Content type is hard coded as application/json.
     *
     * @param uri               of target. Must not be {@literal null}
     * @param responseProcessor {@link ThrowingFunction} to process response before the stream is closed. Must not be {@literal null}
     * @param <DataT>           type of returned data which are created by {@code responseProcessor}
     * @return data created in {@code responseProcessor} based on response of POST request with the given {@code json} on the given {@code uri}
     * @throws IOException if http request is not successful or json processing fail
     */
    private <DataT> DataT get(@NotNull URI uri, @NotNull ThrowingFunction<HttpResponse, DataT, IOException> responseProcessor) throws IOException {
        try (AutoCloseableHttpResponse response = new AutoCloseableHttpResponse()) {
            HttpGet get = new HttpGet(uri);
            get.addHeader(HttpHeaders.ACCEPT, "application/json");
            response.set(execute(get));
            return responseProcessor.apply(response.get());
        }
    }

    /**
     * Sending of {@link HttpDelete} to the given URI
     *
     * @param uri address where delete is send. Must not be {@literal null}
     * @throws IOException if http request is not successful or json processing fail
     * @see DocumentBase
     */
    private <DataT> DataT delete(@NotNull URI uri, @NotNull ThrowingFunction<HttpResponse, DataT, IOException> responseProcessor) throws IOException {
        try (AutoCloseableHttpResponse response = new AutoCloseableHttpResponse()) {
            final HttpDelete delete = new HttpDelete(uri);
            delete.addHeader(HttpHeaders.ACCEPT, "application/json");
            response.set(execute(delete));
            return responseProcessor.apply(response.get());
        }
    }

    private void head(@NotNull URI uri) throws IOException {
        try (AutoCloseableHttpResponse response = new AutoCloseableHttpResponse()) {
            final HttpHead head = new HttpHead(uri);
            response.set(execute(head));
        }
    }

    /**
     * Method to run any HTTP request.
     *
     * @param request which should be run. Must not be {@literal null}
     * @return {@link HttpResponse} to the given {@code request}
     * @throws IOException if http request is not successful or json processing fail
     */
    private @NotNull HttpResponse execute(@NotNull HttpRequestBase request) throws IOException {
        try {
            return httpClient.execute(httpHost, request, httpContext);
        } catch (IOException e) {
            request.abort();
            throw e;
        }
    }

    /**
     * Method to end the connection to the endpoint.
     */
    public void close() {
        HttpClientUtils.closeQuietly(this.httpClient);
    }

}
