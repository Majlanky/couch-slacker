package com.groocraft.couchdb.slacker.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.groocraft.couchdb.slacker.CouchDbClient;
import com.groocraft.couchdb.slacker.annotation.Index;
import com.groocraft.couchdb.slacker.exception.CouchDbRuntimeException;
import com.groocraft.couchdb.slacker.exception.QueryException;
import com.groocraft.couchdb.slacker.structure.DocumentFindRequest;
import com.groocraft.couchdb.slacker.utils.PartTreeWithParameters;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Parameter;
import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.parser.PartTree;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Implementation of {@link RepositoryQuery} used to process query methods of {@link org.springframework.data.repository.Repository} implementation.
 * <p>
 * Implementation support named parameters with {@link org.springframework.data.repository.query.Param} or name of a parameter is used to specify parsed query.
 * <p>
 * Implementation supports all find queries.
 *
 * @author Majlanky
 * @see RepositoryQuery
 */
public class CouchDbParsingQuery implements RepositoryQuery {

    private final QueryMethod queryMethod;
    private final CouchDbClient client;
    private final Class<?> entityClass;
    private final ObjectMapper mapper;
    private final Function<Object, Object> postProcessor;
    private final PartTree partTree;
    private final Index index;

    /**
     * @param client      must not be {@literal null}.
     * @param method      must not be {@literal null}.
     * @param queryMethod on which is based the query. Must not be {@literal null}.
     * @param entityClass repository domain class. Must not be {@literal null}.
     */
    public CouchDbParsingQuery(@NotNull CouchDbClient client, @NotNull Method method, @NotNull QueryMethod queryMethod, @NotNull Class<?> entityClass) {
        this.client = client;
        this.queryMethod = queryMethod;
        this.entityClass = entityClass;
        this.mapper = new ObjectMapper();
        partTree = new PartTree(queryMethod.getName(), queryMethod.getResultProcessor().getReturnedType().getDomainType());
        index = method.getAnnotation(Index.class);
        this.postProcessor = getPostProcessor(partTree, queryMethod.getResultProcessor().getReturnedType().getDomainType());
    }

    /**
     * Method to obtain sorting information from parameters if present. If there is no parameter with sorting information
     * {@link Sort#unsorted()} is used.
     *
     * @param parameters Actual parameter of a call. Must not be {@literal null}
     * @return {@link Sort} from parameters if present or {@link Sort#unsorted()}. Can not be {@literal null}
     */
    private @NotNull Sort getSortFrom(@NotNull Object[] parameters) {
        int sortIndex = getQueryMethod().getParameters().getSortIndex();
        if (sortIndex != -1) {
            return (Sort) parameters[sortIndex];
        }
        return Sort.unsorted();
    }

    /**
     * Method to obtain pagination information from parameters if present. If there is no parameter with pagination information
     * {@link Pageable#unpaged()} is used.
     *
     * @param parameters Actual parameter of a call. Must not be {@literal null}
     * @return {@link Pageable} from parameters if present or {@link Pageable#unpaged()}. Can not be {@literal null}
     */
    private @NotNull Pageable getPageableFrom(@NotNull Object[] parameters) {
        int pageableIndex = getQueryMethod().getParameters().getPageableIndex();
        if (pageableIndex != -1) {
            return (Pageable) parameters[pageableIndex];
        }
        return Pageable.unpaged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(Object[] parameters) {
        DocumentFindRequest request = new DocumentFindRequest(new PartTreeWithParameters(partTree, initializeParameters(parameters)), index,
                getPageableFrom(parameters),
                getSortFrom(parameters));
        String query = "non-existing";
        try {
            query = mapper.writeValueAsString(request);
            Object result = client.find(query, entityClass);
            return postProcessor.apply(result);
        } catch (IOException e) {
            throw new CouchDbRuntimeException("Unable to query " + query, e);
        }
    }

    /**
     * Method to create map of named parameters with actual value of a call. Actual implementation works only with named parameters, so
     * {@link QueryException} can be thrown if one or more parameters are not named.
     *
     * @param parameters Actual parameter of a call. Must not be {@literal null}
     * @return {@link Map} of named parameters of a call. Can not be {@literal null}
     */
    private @NotNull Map<String, Object> initializeParameters(@NotNull Object[] parameters) {
        Map<String, Object> initialized = new HashMap<>();
        Parameters<?, ?> methodParameters = getQueryMethod().getParameters();
        for (Parameter parameter : methodParameters) {
            if (!parameter.isDynamicProjectionParameter() && !parameter.isSpecialParameter()) {
                initialized.put(parameter.getName().orElseThrow(() -> new QueryException("Dynamic query can work only with named parameters")),
                        parameters[parameter.getIndex()]);
            }
        }
        return initialized;
    }

    /**
     * Method to create post processor for find result. Spring data provides delete, count, exists and distinct operation above result of find query.
     *
     * @param partTree  {@link PartTree} created from generic query method.  Must not be {@literal null}
     * @param clazz     of entities.  Must not be {@literal null}
     * @param <EntityT> type of entities
     * @return {@link Function} processing result of find query in the requested way. Can not be {@literal null}
     */
    @SuppressWarnings("unchecked")
    private <EntityT> @NotNull Function<Object, Object> getPostProcessor(@NotNull PartTree partTree, @NotNull Class<EntityT> clazz) {
        if (partTree.isDelete()) {
            return o -> delete(o, clazz);
        }
        if (partTree.isCountProjection()) {
            return o -> ((List<EntityT>) o).size();
        }
        if (partTree.isDistinct()) {
            //TODO only unique values :-O (create new index and work with it?)
            return o -> o;
        }
        if (partTree.isExistsProjection()) {
            return o -> ((List<EntityT>) o).size() > 0;
        }
        return o -> o;
    }

    /**
     * Wrapper for calling delete. Method deletes all given entities from DB.
     *
     * @param entities  which should be erased.  Must not be {@literal null}
     * @param clazz     of entities.  Must not be {@literal null}
     * @param <EntityT> type of entities
     * @return {@link Iterable} of deleted entities. Can not be {@literal null}
     */
    @SuppressWarnings("unchecked")
    private <EntityT> @NotNull Iterable<EntityT> delete(@NotNull Object entities, @NotNull Class<EntityT> clazz) {
        try {
            return client.deleteAll((Iterable<EntityT>) entities, clazz);
        } catch (IOException e) {
            throw new CouchDbRuntimeException("Unable to query post process (delete) query ", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueryMethod getQueryMethod() {
        return queryMethod;
    }

}
