package com.groocraft.couchdb.slacker.repository;

import com.groocraft.couchdb.slacker.CouchDbClient;
import com.groocraft.couchdb.slacker.exception.CouchDbRuntimeException;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.repository.query.Parameter;
import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.RepositoryQuery;

import java.io.IOException;

/**
 * Abstract class for {@link RepositoryQuery} implementation adding support for specifying json query (filling actual parameter of the call into json). Both
 * ? and : replacement are supported, but can not be mixed.
 *
 * @author Majlanky
 * @see RepositoryQuery
 */
//TODO dynamic and other projection
public abstract class CouchDbQuery implements RepositoryQuery {

    private final QueryMethod queryMethod;
    private final CouchDbClient client;
    private final Class<?> entityClass;

    /**
     * @param client      must not be {@literal null}.
     * @param queryMethod on which is based the query. Must not be {@literal null}.
     * @param entityClass repository domain class. Must not be {@literal null}.
     */
    public CouchDbQuery(@NotNull CouchDbClient client, @NotNull QueryMethod queryMethod, @NotNull Class<?> entityClass) {
        this.client = client;
        this.queryMethod = queryMethod;
        this.entityClass = entityClass;
    }

    /**
     * Method implementation should return valid Mango query which should be executed. The query can contain replacements of values which should be passed as
     * parameters during {@link #execute(Object[])} method. The replacing of parameters should be done by {@link #specify(String, Object[])} method. The
     * replacement naming convention can be {@code :nameOfParameter} or {@code ?indexOfParameter}
     *
     * @param parameters parameters of actual call
     * @return Valid Mango query which can contain replacement. Must not be
     */
    protected abstract @NotNull String getQuery(Object[] parameters);

    /**
     * Method accessing {@link CouchDbClient}.
     * @return {@link CouchDbClient} passed thru constructor
     */
    protected @NotNull CouchDbClient getClient(){
        return client;
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
                specified = specified.replace("?" + parameter.getIndex(), "" + parameters[parameter.getIndex()]);
                specified = specified.replace(":" + parameter.getName().orElseThrow(), "" + parameters[parameter.getIndex()]);
            }
        }
        return specified;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(Object[] parameters) {
        String query = getQuery(parameters);
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
