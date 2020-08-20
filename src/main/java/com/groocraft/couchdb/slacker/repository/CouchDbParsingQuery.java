package com.groocraft.couchdb.slacker.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.groocraft.couchdb.slacker.CouchDbClient;
import com.groocraft.couchdb.slacker.annotation.Index;
import com.groocraft.couchdb.slacker.exception.CouchDbRuntimeException;
import com.groocraft.couchdb.slacker.exception.QueryException;
import com.groocraft.couchdb.slacker.structure.DocumentFindRequest;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.parser.PartTree;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
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
public class CouchDbParsingQuery extends CouchDbQuery {

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
        super(client, queryMethod, entityClass);
        partTree = new PartTree(queryMethod.getName(), queryMethod.getResultProcessor().getReturnedType().getDomainType());
        index = method.getAnnotation(Index.class);
        this.postProcessor = getPostProcessor(partTree, queryMethod.getReturnedObjectType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected @NotNull String getQuery(Object[] parameters) {
        try {
            DocumentFindRequest request = new DocumentFindRequest(partTree, index, getPageableFrom(parameters), getSortFrom(parameters));
            String query = new ObjectMapper().writeValueAsString(request);
            return parameters.length > 0 ? specify(query, parameters) : query;
        } catch (JsonProcessingException e) {
            throw new QueryException("Unable to create Mango query", e);
        }
    }

    private Sort getSortFrom(Object[] parameters) {
        int sortIndex = getQueryMethod().getParameters().getSortIndex();
        if (sortIndex != -1) {
            return (Sort) parameters[sortIndex];
        }
        return Sort.unsorted();
    }

    private Pageable getPageableFrom(Object[] parameters) {
        int pageableIndex = getQueryMethod().getParameters().getPageableIndex();
        if (pageableIndex != -1) {
            return (Pageable) parameters[pageableIndex];
        }
        return Pageable.unpaged();
    }

    @Override
    public Object execute(Object[] parameters) {
        return postProcessor.apply(super.execute(parameters));
    }

    /**
     * Method to create post processor for find result. Spring data provides delete, count, exists and distinct operation above result of find query.
     * @param partTree {@link PartTree} created from generic query method
     * @param clazz of entities
     * @param <EntityT> type of entities
     * @return {@link Function} processing result of find query in the requested way
     */
    @SuppressWarnings("unchecked")
    private <EntityT> Function<Object, Object> getPostProcessor(PartTree partTree, Class<EntityT> clazz) {
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
     * @param entities which should be erased
     * @param clazz of entities
     * @param <EntityT> type of entities
     * @return {@link Iterable} of deleted entities
     */
    @SuppressWarnings("unchecked")
    private <EntityT> Iterable<EntityT> delete(Object entities, Class<EntityT> clazz) {
        try {
            return getClient().deleteAll((Iterable<EntityT>)entities, clazz);
        } catch (IOException e) {
            throw new CouchDbRuntimeException("Unable to query post process (delete) query ", e);
        }
    }

}
