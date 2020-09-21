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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.groocraft.couchdb.slacker.http.AutoCloseableHttpResponse;
import com.groocraft.couchdb.slacker.repository.CouchDbEntityInformation;
import com.groocraft.couchdb.slacker.structure.AllDocumentResponse;
import com.groocraft.couchdb.slacker.structure.BulkGetResponse;
import com.groocraft.couchdb.slacker.structure.BulkRequest;
import com.groocraft.couchdb.slacker.structure.DocumentFindResponse;
import com.groocraft.couchdb.slacker.structure.DocumentPutResponse;
import com.groocraft.couchdb.slacker.structure.IndexCreateRequest;
import com.groocraft.couchdb.slacker.utils.BulkGetDeserializer;
import com.groocraft.couchdb.slacker.utils.BulkGetIdSerializer;
import com.groocraft.couchdb.slacker.utils.DeleteDocumentSerializer;
import com.groocraft.couchdb.slacker.utils.FoundDocumentDeserializer;
import com.groocraft.couchdb.slacker.utils.LazyLog;
import com.groocraft.couchdb.slacker.utils.ThrowingFunction;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Sort;
import org.springframework.util.Assert;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

    private final HttpClient httpClient;
    private final HttpHost httpHost;
    private final HttpContext httpContext;
    @SuppressWarnings({"rawtypes"})
    private final Map<Class, EntityMetadata> entityMetadataCache;
    private final URI baseURI;
    private final ObjectMapper mapper;
    @SuppressWarnings({"rawtypes"})
    private final Map<Class, IdGenerator> idGenerators;
    private final IdGenerator<?> defaultIdGenerator;

    /**
     * @param httpClient   must not be {@literal null}
     * @param httpHost     must not be {@literal null}
     * @param httpContext  must not be {@literal null}
     * @param baseURI      where CouchDB is accessible without database specification. Must not be {@literal null}
     * @param idGenerators {@link Iterable} of available {@link IdGenerator}. If empty, default generator {@link IdGeneratorUUID} is used. Must not be {@literal
     *                     null}
     */
    CouchDbClient(@NotNull HttpClient httpClient, @NotNull HttpHost httpHost, @NotNull HttpContext httpContext, @NotNull URI baseURI,
                  @NotNull Iterable<IdGenerator<?>> idGenerators) {
        Assert.notNull(httpClient, "HttpClient must not be null.");
        Assert.notNull(httpHost, "HttpHost must not be null.");
        Assert.notNull(httpContext, "HttpContext must not be null.");
        Assert.notNull(baseURI, "BaseURI must not be null.");
        Assert.notNull(idGenerators, "IdGenerators must not be null.");
        this.httpClient = httpClient;
        this.baseURI = baseURI;
        this.httpHost = httpHost;
        this.httpContext = httpContext;
        entityMetadataCache = new HashMap<>();
        this.mapper = new ObjectMapper();
        this.idGenerators = new HashMap<>();
        this.defaultIdGenerator = new IdGeneratorUUID();
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
     * @param clazz about which metadata is needed. Must not be {@link null}
     * @param <T>   type of class
     * @return {@link EntityMetadata} about passed class
     */
    @SuppressWarnings("unchecked")
    private <T> @NotNull EntityMetadata<T> getEntityMetadata(@NotNull Class<T> clazz) {
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
     * @see #CouchDbClient(HttpClient, HttpHost, HttpContext, URI, Iterable)
     */
    @SuppressWarnings("unchecked")
    private <EntityT> @NotNull String generateId(@NotNull EntityT entity, Class<EntityT> clazz) {
        return idGenerators.computeIfAbsent(clazz, c -> defaultIdGenerator).generate(entity);
    }

    /**
     * @param clazz class of entity for which database name is needed. Must not be {@link null}
     * @return Name of database for given class
     * @see EntityMetadata
     */
    private @NotNull String getDatabaseName(@NotNull Class<?> clazz) {
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
     * @see Document
     */
    public <EntityT> @NotNull EntityT save(@NotNull EntityT entity) throws IOException {
        EntityMetadata<?> entityMetadata = getEntityMetadata(entity.getClass());
        String id = entityMetadata.getIdReader().read(entity);
        log.debug("Saving document {} with id {} and revision {} to database {}", entity, id,
                LazyLog.of(() -> entityMetadata.getRevisionReader().read(entity)), entityMetadata.getDatabaseName());
        if ("".equals(id) || id == null) {
            id = generateId(entity, (Class<EntityT>) entity.getClass());
            log.debug("New ID {} generated for saved document", id);
        }
        DocumentPutResponse response = put(getURI(baseURI, entityMetadata.getDatabaseName(), id), mapper.writeValueAsString(entity), r -> mapper.readValue(r.getEntity().getContent(),
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
    @SuppressWarnings("DuplicatedCode")
    public <EntityT> @NotNull Iterable<EntityT> saveAll(@NotNull Iterable<EntityT> entities, @NotNull Class<?> clazz) throws IOException {
        EntityMetadata<?> entityMetadata = getEntityMetadata(clazz);
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
        List<DocumentPutResponse> responses = post(getURI(baseURI, entityMetadata.getDatabaseName(), "_bulk_docs"),
                mapper.writeValueAsString(new BulkRequest<>(entities)), r -> mapper.readValue(r.getEntity().getContent(),
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
     * @see Document
     */
    public <EntityT> EntityT read(@NotNull String id, @NotNull Class<EntityT> clazz) throws IOException {
        String databaseName = getDatabaseName(clazz);
        log.debug("Read of document with ID {} from database {}", id, databaseName);
        return get(getURI(baseURI, databaseName, id), r -> mapper.readValue(r.getEntity().getContent(), clazz));
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
    public <EntityT> @NotNull Iterable<EntityT> readAll(@NotNull Iterable<String> ids, @NotNull Class<EntityT> clazz) throws IOException {
        ObjectMapper localMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(new BulkGetIdSerializer<>(Document.class, new EntityMetadata<>(Document.class).getIdReader()));
        module.addDeserializer(List.class, new BulkGetDeserializer<>(clazz));
        localMapper.registerModule(module);
        List<Document> docs = new LinkedList<>();
        ids.forEach(id -> docs.add(new Document(id, null)));
        log.debug("Bulk read of {} document from database {} with the following IDs: {}",
                LazyLog.of(() -> StreamSupport.stream(ids.spliterator(), false).count()),
                getDatabaseName(clazz),
                LazyLog.of(() -> String.join(", ", ids)));
        BulkGetResponse<EntityT> response = post(getURI(baseURI, getDatabaseName(clazz), "_bulk_get"), localMapper.writeValueAsString(new BulkRequest<>(docs)),
                r -> localMapper.readValue(r.getEntity().getContent(), localMapper.getTypeFactory().constructParametricType(BulkGetResponse.class, clazz)));
        log.info("Bulk read of {} ids result contains {} documents", LazyLog.of(() -> StreamSupport.stream(ids.spliterator(), false).count()),
                response.getDocs().size());
        return response.getDocs();
    }

    /**
     * Method to get result of _all_docs with ignoring design documents (If you need design documents use {@link #readAllDesign(Class)} or
     * {@link #readAllDocs(Class, Predicate)} if you want both) to the database  specified by {@link com.groocraft.couchdb.slacker.annotation.Database} from
     * the passed entity class. Result is returned as Stream of documents ids, no full data.
     *
     * @param clazz of wanted entity. Used to get database name {@link #getDatabaseName(Class)}. Must not be {@literal null}
     * @return Stream of {@link String} which contain id
     * @throws IOException if http request is not successful or json processing fail
     * @see com.groocraft.couchdb.slacker.annotation.Database
     * @see #readAllDesign(Class)
     * @see #readAllDocs(Class, Predicate)
     */
    public @NotNull Iterable<String> readAll(@NotNull Class<?> clazz) throws IOException {
        log.debug("Read of all non design documents from database {}", getDatabaseName(clazz));
        return readAllDocs(clazz, Predicate.not(s -> s.startsWith("_design")));
    }

    /**
     * Method to get result of _design_docs which contains only design documents (If you need non-design documents use {@link #readAll(Class)} or
     * {@link #readAllDocs(Class, Predicate)} if you want both) to the database specified by {@link com.groocraft.couchdb.slacker.annotation.Database} from
     * the passed entity class. Result is returned as Stream of documents ids, no full data.
     *
     * @param clazz of wanted entity. Used to get database name {@link #getDatabaseName(Class)}. Must not be {@literal null}
     * @return Stream of {@link String} which contain id
     * @throws IOException if http request is not successful or json processing fail
     * @see com.groocraft.couchdb.slacker.annotation.Database
     * @see #readAll(Class)
     * @see #readAllDocs(Class, Predicate)
     */
    public @NotNull Iterable<String> readAllDesign(@NotNull Class<?> clazz) throws IOException {
        String databaseName = getDatabaseName(clazz);
        log.debug("Read of all design documents from database {}", databaseName);
        return get(getURI(baseURI, databaseName, "_design_docs"),
                r -> mapper.readValue(r.getEntity().getContent(), AllDocumentResponse.class).getRows());
    }

    /**
     * Method to get result of _all_docs with possibility of id filtering (If you need non-design documents use {@link #readAll(Class)} or
     * {@link #readAllDesign(Class)} if you want design document only) to the database specified by {@link com.groocraft.couchdb.slacker.annotation.Database}
     * from the passed entity class. Result is returned as Stream of documents ids, no full data.
     *
     * @param clazz             of wanted entity. Used to get database name {@link #getDatabaseName(Class)}. Must not be {@literal null}
     * @param idFilterPredicate with filtering rule to id. Must not be {@literal null}. Use {@code s -> true} to disable filtering.
     * @return Stream of {@link String} which contain id.
     * @throws IOException if http request is not successful or json processing fail
     * @see #readAll(Class)
     * @see #readAllDesign(Class)
     */
    public @NotNull Iterable<String> readAllDocs(@NotNull Class<?> clazz, @NotNull Predicate<String> idFilterPredicate) throws IOException {
        String databaseName = getDatabaseName(clazz);
        return get(getURI(baseURI, databaseName, "_all_docs"),
                r -> mapper.readValue(r.getEntity().getContent(), AllDocumentResponse.class).getRows().stream().filter(idFilterPredicate).collect(Collectors.toList()));
    }

    /**
     * Deletes given entity. From entity id and revision is used.
     *
     * @param entity    to delete. Must not be {@literal null}
     * @param <EntityT> type of entity
     * @return deleted entity
     * @throws IOException if http request is not successful or json processing fail
     * @see Document
     */
    public <EntityT> @NotNull EntityT delete(@NotNull EntityT entity) throws IOException {
        EntityMetadata<?> entityMetadata = getEntityMetadata(entity.getClass());
        String id = entityMetadata.getIdReader().read(entity);
        String revision = entityMetadata.getRevisionReader().read(entity);
        log.debug("Delete of document with id {} and revision {} from database {}", id, revision, entityMetadata.getDatabaseName());
        return delete(getURI(baseURI, List.of(entityMetadata.getDatabaseName(), id), List.of(new BasicNameValuePair("rev", revision))), r -> entity);
    }

    /**
     * Method to remove all documents from database. Method is implemented as read all ids and than delete by id. Both mentioned functions are bulk
     * operations. As a consequence of the mentioned approach, DB deletes only document existing in the time of call, not documents created after the request
     * . Name of the database in which delete is executed is read from given class.
     *
     * @param clazz     with {@link com.groocraft.couchdb.slacker.annotation.Database} annotation
     * @param <EntityT> Type of entity in the database
     * @return {@link Iterable} of deleted documents
     * @throws IOException if http request is not successful or json processing fail
     */
    public <EntityT> @NotNull Iterable<EntityT> deleteAll(@NotNull Class<EntityT> clazz) throws IOException {
        log.debug("Delete of all documents from database {}", getDatabaseName(clazz));
        return deleteAll(readAll(readAll(clazz), clazz), clazz);
    }

    /**
     * Method to delete given documents. A bulk operation is used.
     *
     * @param entities  {@link Iterable} of entities to be erased
     * @param clazz     of given entities
     * @param <EntityT> type of entities
     * @return {@link Iterable} of deleted entities
     * @throws IOException if http request is not successful or json processing fail
     */
    @SuppressWarnings("DuplicatedCode")
    public <EntityT> @NotNull Iterable<EntityT> deleteAll(@NotNull Iterable<EntityT> entities, @NotNull Class<?> clazz) throws IOException {
        EntityMetadata<?> entityMetadata = getEntityMetadata(clazz);
        log.debug("Bulk delete of {} documents from database {}", LazyLog.of(() -> StreamSupport.stream(entities.spliterator(), false).count()),
                entityMetadata.getDatabaseName());
        ObjectMapper localMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(new DeleteDocumentSerializer<>(clazz));
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
     * Deletes document from database based on {@link com.groocraft.couchdb.slacker.annotation.Database} annotation of given {@code clazz} with the given id.
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
        return delete(getURI(baseURI, List.of(entityMetadata.getDatabaseName(), id), List.of(new BasicNameValuePair("rev", entityMetadata.getRevisionReader()
                .read(entity)))), r -> entity);
    }

    /**
     * Executes the given Mango query and maps a result into the given entity. To better performance index is needed. If mango query is created from generic
     * query method {@link com.groocraft.couchdb.slacker.annotation.Index} annotation can be used to specify the index(es).
     *
     * @param json      query of valid Mango query. Must not be {@literal null}
     * @param clazz     of entities expected as result. Must not be {@literal null}
     * @param <EntityT> type of entity
     * @return {@link List} of instances of the given class with result of the given class
     * @throws IOException if http request is not successful or json processing fail
     * @see Document
     */
    public <EntityT> @NotNull List<EntityT> find(@NotNull String json, @NotNull Class<EntityT> clazz) throws IOException {
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
        return response.getDocuments();
    }

    /**
     * Method to create new index by the given parameters.
     *
     * @param name        of the created index. Must not be {@literal null}
     * @param entityClass as definition of database in which index should be created. Must not be {@literal null}
     * @param fields      list of {@link org.springframework.data.domain.Sort.Order} which from index should be done
     * @throws IOException if http request is not successful or json processing fail
     */
    public void createIndex(@NotNull String name, @NotNull Class<?> entityClass, Sort.Order... fields) throws IOException {
        createIndex(name, entityClass, fields == null ? List.of() : Arrays.asList(fields));
    }

    /**
     * Method to create new index by the given parameters.
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
     * Method to create new index by the given parameters.
     *
     * @param name   of the created index. Must not be {@literal null}
     * @param dbName in which index should be created. Must not be {@literal null}
     * @param fields list of {@link org.springframework.data.domain.Sort.Order} which from index should be done. Must not be {@literal null}
     * @throws IOException if http request is not successful or json processing fail
     */
    public void createIndex(@NotNull String name, @NotNull String dbName, @NotNull Iterable<Sort.Order> fields) throws IOException {
        log.debug("Creating index with name {} in database {} and ordering {}", name, dbName,
                LazyLog.of(() -> StreamSupport.stream(fields.spliterator(), false).map(Sort.Order::toString).collect(Collectors.joining(", "))));
        post(getURI(baseURI, dbName, "_index"), mapper.writeValueAsString(new IndexCreateRequest(name, fields)), r -> null);
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
        //Default values get from CouchDb documentation
        createDatabase(name, 8, 3, false);
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
        put(getURI(baseURI, List.of(name), List.of(new BasicNameValuePair("q", "" + shardsCount), new BasicNameValuePair("n", replicasCount + ""),
                new BasicNameValuePair("partitioned", Boolean.toString(partitioned)))),
                "", r -> null);
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
     * @see Document
     */
    private <DataT> DataT delete(@NotNull URI uri, @NotNull ThrowingFunction<HttpResponse, DataT, IOException> responseProcessor) throws IOException {
        try (AutoCloseableHttpResponse response = new AutoCloseableHttpResponse()) {
            final HttpDelete delete = new HttpDelete(uri);
            delete.addHeader(HttpHeaders.ACCEPT, "application/json");
            response.set(execute(delete));
            return responseProcessor.apply(response.get());
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
