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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Client for CouchDB REST API. It is using Jackson library to work with json.
 *
 * @author Majlanky
 */
//TODO add API for managing replication and users?
//TODO integration tests with fabric8io/docker-maven-plugin
//TODO logging everywhere
//TODO views API
//TODO attachments
//TODO process error nodes for bulk operations somehow
//TODO auditing
//TODO parsers for xml config
@Slf4j
public class CouchDbClient {

    private final HttpClient httpClient;
    private final HttpHost httpHost;
    private final HttpContext httpContext;
    @SuppressWarnings({"rawtypes"})
    private final Map<Class, EntityMetadata> entityMetadataCache;
    private final URI baseURI;
    private final ObjectMapper mapper;
    //TODO rework it to function and provide saved entity
    private final Supplier<String> uidGenerator;

    /**
     * This constructor setting ID generator as {@link UUID#randomUUID()#toString()} method. If custom implementation of ID is needed, use
     * {@link CouchDbClient#CouchDbClient(HttpClient, HttpHost, HttpContext, URI, Supplier)}
     *
     * @param httpClient  must not be {@literal null}
     * @param httpHost    must not be {@literal null}
     * @param httpContext must not be {@literal null}
     * @param baseURI     where CouchDB is accessible without database specification. Must not be {@literal null}
     */
    public CouchDbClient(@NotNull HttpClient httpClient, @NotNull HttpHost httpHost, @NotNull HttpContext httpContext, @NotNull URI baseURI) {
        this(httpClient, httpHost, httpContext, baseURI, () -> UUID.randomUUID().toString());
    }

    /**
     * @param httpClient   must not be {@literal null}
     * @param httpHost     must not be {@literal null}
     * @param httpContext  must not be {@literal null}
     * @param baseURI      where CouchDB is accessible without database specification. Must not be {@literal null}
     * @param uidGenerator {@link Supplier} for getting UID used for saving new entities
     */
    public CouchDbClient(@NotNull HttpClient httpClient, @NotNull HttpHost httpHost, @NotNull HttpContext httpContext, @NotNull URI baseURI,
                         @NotNull Supplier<String> uidGenerator) {
        this.httpClient = httpClient;
        this.baseURI = baseURI;
        this.httpHost = httpHost;
        this.httpContext = httpContext;
        entityMetadataCache = new HashMap<>();
        this.mapper = new ObjectMapper();
        this.uidGenerator = uidGenerator;
    }

    /**
     * @param clazz     of entity type about which information is needed. Must not be {@link null}
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
     * Saving given entity. If there is no ID for given instance, {@link UUID#randomUUID()} is used to create unique id (creates new document in DB).
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
        if ("".equals(id) || id == null) {
            id = uidGenerator.get();
        }
        DocumentPutResponse response = put(getURI(baseURI, entityMetadata.getDatabaseName(), id), mapper.writeValueAsString(entity), r -> mapper.readValue(r.getEntity().getContent(),
                DocumentPutResponse.class));
        entityMetadata.getRevisionWriter().write(entity, response.getRev());
        entityMetadata.getIdWriter().write(entity, response.getId());
        return entity;
    }

    /**
     * Saving all given entities in one POST request. If there is no ID for given instance, {@link UUID#randomUUID()} is used to create unique id (creates
     * new document in DB)
     *
     * @param entities  {@link Iterable} of entities to save. Must not be {@literal null}
     * @param clazz     Class of entities passed to save. Must not be {@literal null}
     * @param <EntityT> type of entities passed to save
     * @return {@link Iterable} of all passed entities with updated revisions and ids. Can not be {@literal null}
     * @throws IOException if http request is not successful or json processing fail
     */
    public <EntityT> @NotNull Iterable<EntityT> saveAll(@NotNull Iterable<EntityT> entities, @NotNull Class<?> clazz) throws IOException {
        //TODO split to max of 10000 row in one request. Or better make it configurable
        EntityMetadata<?> entityMetadata = getEntityMetadata(clazz);
        StreamSupport.stream(entities.spliterator(), false).forEach(e -> {
            String id = entityMetadata.getIdReader().read(e);
            if ("".equals(id) || id == null) {
                entityMetadata.getIdWriter().write(e, uidGenerator.get());
            }
        });
        List<DocumentPutResponse> responses = post(getURI(baseURI, entityMetadata.getDatabaseName(), "_bulk_docs"),
                mapper.writeValueAsString(new BulkRequest<>(entities)), r -> mapper.readValue(r.getEntity().getContent(),
                        mapper.getTypeFactory().constructCollectionType(List.class, DocumentPutResponse.class)));
        Map<String, DocumentPutResponse> indexed = responses.stream().collect(Collectors.toMap(DocumentPutResponse::getId, r -> r));
        //TODO recognized failed ones
        StreamSupport.stream(entities.spliterator(), false).forEach(e -> entityMetadata.getRevisionWriter().write(e, indexed.get(entityMetadata.getIdReader().read(e)).getRev()));
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
    //TODO if document do not exists, 404 is thrown. Fix
    //TODO rework for Optional?
    public <EntityT> EntityT read(@NotNull String id, @NotNull Class<EntityT> clazz) throws IOException {
        String databaseName = getDatabaseName(clazz);
        return get(getURI(baseURI, databaseName, id), r -> mapper.readValue(r.getEntity().getContent(), clazz));
    }

    /**
     * Method for reading all documents of given ids. Read is done in a bulk request.
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
        BulkGetResponse<EntityT> response = post(getURI(baseURI, getDatabaseName(clazz), "_bulk_get"), localMapper.writeValueAsString(new BulkRequest<>(docs)),
                r -> localMapper.readValue(r.getEntity().getContent(), localMapper.getTypeFactory().constructParametricType(BulkGetResponse.class, clazz)));
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
        return readAllDocs(clazz, Predicate.not(s -> s.startsWith("_design")));
    }

    /**
     * Method to get result of _all_docs filtering only design documents (If you need non-design documents use {@link #readAll(Class)} or
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
    //TODO rework to _design_docs view
    public @NotNull Iterable<String> readAllDesign(@NotNull Class<?> clazz) throws IOException {
        return readAllDocs(clazz, s -> s.startsWith("_design"));
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
     * @param entity to delete. Must not be {@literal null}
     * @throws IOException if http request is not successful or json processing fail
     * @see Document
     */
    public <EntityT> @NotNull EntityT delete(@NotNull EntityT entity) throws IOException {
        EntityMetadata<?> entityMetadata = getEntityMetadata(entity.getClass());
        String id = entityMetadata.getIdReader().read(entity);
        String revision = entityMetadata.getRevisionReader().read(entity);
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
    public <EntityT> @NotNull Iterable<EntityT> deleteAll(@NotNull Iterable<EntityT> entities, @NotNull Class<?> clazz) throws IOException {
        EntityMetadata<?> entityMetadata = getEntityMetadata(clazz);
        ObjectMapper localMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(new DeleteDocumentSerializer<>(clazz));
        localMapper.registerModule(module);
        //TODO recognized failed ones
        return post(getURI(baseURI, entityMetadata.getDatabaseName(), "_bulk_docs"), localMapper.writeValueAsString(new BulkRequest<>(entities)), r -> entities);
    }

    /**
     * Deletes document from database based on {@link com.groocraft.couchdb.slacker.annotation.Database} annotation of given {@code clazz} with the given id.
     * The method deletes latest document, without any consistency check.
     *
     * @param id        of document which should be deleted
     * @param clazz     of entity to get database
     * @param <EntityT> type of entity
     * @throws IOException if http request is not successful or json processing fail
     */
    public <EntityT> @NotNull EntityT deleteById(@NotNull String id, @NotNull Class<EntityT> clazz) throws IOException {
        EntityMetadata<EntityT> entityMetadata = getEntityMetadata(clazz);
        EntityT entity = read(id, clazz);
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
    public <EntityT> @NotNull Iterable<EntityT> find(@NotNull String json, @NotNull Class<EntityT> clazz) throws IOException {
        ObjectMapper localMapper = new ObjectMapper();
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(List.class, new FoundDocumentDeserializer<>(clazz));
        localMapper.registerModule(simpleModule);
        DocumentFindResponse<EntityT> response = post(getURI(baseURI, getDatabaseName(clazz), "_find"), json, r -> localMapper.readValue(r.getEntity().getContent(),
                localMapper.getTypeFactory().constructParametricType(DocumentFindResponse.class, clazz)));
        log.warn(response.getWarning());
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
