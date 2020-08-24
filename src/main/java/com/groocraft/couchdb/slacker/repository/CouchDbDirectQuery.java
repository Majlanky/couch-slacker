package com.groocraft.couchdb.slacker.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.groocraft.couchdb.slacker.CouchDbClient;
import com.groocraft.couchdb.slacker.exception.CouchDbRuntimeException;
import com.groocraft.couchdb.slacker.exception.QueryException;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.repository.query.Parameter;
import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.RepositoryQuery;

import java.io.IOException;

/**
 * Implementation of {@link RepositoryQuery} used to process {@link com.groocraft.couchdb.slacker.annotation.Query} annotated method of
 * {@link org.springframework.data.repository.Repository} implementation.
 * <p>
 * Implementation support both ways of passing parameters (? with number of parameter and : with name of parameter). In the other case,
 * {@link org.springframework.data.repository.query.Param} can be used.
 *
 * @author Majlanky
 * @see RepositoryQuery
 */
public class CouchDbDirectQuery implements RepositoryQuery {

    private final String query;
    private final CouchDbClient client;
    private final Class<?> entityClass;
    private final ObjectMapper mapper;
    private final QueryMethod queryMethod;

    /**
     * @param query Json query created or read from method. Must not be {@literal null}.
     * @param client must not be {@literal null}.
     * @param queryMethod on which is based the query. Must not be {@literal null}.
     */
    public CouchDbDirectQuery(@NotNull String query, @NotNull CouchDbClient client, @NotNull QueryMethod queryMethod, @NotNull Class<?> entityClass) {
        this.client = client;
        this.queryMethod = queryMethod;
        this.entityClass = entityClass;
        this.mapper = new ObjectMapper();
        this.query = query;
    }

    /**
     * Using parsed parameters from {@link QueryMethod} to find which name is which parameter and replacing all parameters found.
     *
     * @param query      must not be {@literal null}.
     * @param parameters must not be {@literal null}.
     * @return Specified query, it means all tokens are replaced by actual values of passed parameters
     */
    protected @NotNull String specify(String query, Object[] parameters) {
        String specified = query;
        Parameters<?, ?> methodParameters = getQueryMethod().getParameters();
        for (Parameter parameter : methodParameters) {
            if (!parameter.isDynamicProjectionParameter() && !parameter.isSpecialParameter()) {
                try {
                    specified = specified.replace("?" + (parameter.getIndex() + 1), mapper.writeValueAsString(parameters[parameter.getIndex()]));
                    if(parameter.getName().isPresent()){
                        specified = specified.replace(":" + parameter.getName().get(), mapper.writeValueAsString(parameters[parameter.getIndex()]));
                    }
                } catch (JsonProcessingException ex){
                    throw new QueryException(String.format("Unable to create json representation %s. parameter", parameter.getIndex()), ex);
                }
            }
        }
        return specified;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(Object[] parameters) {
        String currentQuery = parameters.length > 0 ? specify(query, parameters) : query;
        try {
            return client.find(currentQuery, entityClass);
        } catch (IOException e) {
            throw new CouchDbRuntimeException("Unable to query " + query, e);
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
