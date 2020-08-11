package com.groocraft.couchdb.slacker.repository;

import com.groocraft.couchdb.slacker.CouchDbClient;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.lang.Nullable;

import java.util.Optional;

/**
 * Implementation of factory for CouchDB repositories. CouchDB repositories are based on {@link SimpleCouchDbRepository}. Factory provides
 * {@link QueryLookupStrategy} to enable (generic) query methods in CouchDB repositories (managed by {@link CouchDbQueryLookupStrategy}).
 *
 * @author Majlanky
 * @see RepositoryFactorySupport
 * @see SimpleCouchDbRepository
 * @see CouchDbQueryLookupStrategy
 */
public class CouchDbRepositoryFactory extends RepositoryFactorySupport {

    private final CouchDbClient client;

    /**
     * @param client must not be {@literal null}
     */
    public CouchDbRepositoryFactory(CouchDbClient client) {
        this.client = client;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T, ID> EntityInformation<T, ID> getEntityInformation(Class<T> domainClass) {
        return client.getEntityInformation(domainClass);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object getTargetRepository(RepositoryInformation metadata) {
        return getTargetRepositoryViaReflection(metadata, client, metadata.getDomainType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
        return SimpleCouchDbRepository.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Optional<QueryLookupStrategy> getQueryLookupStrategy(@Nullable QueryLookupStrategy.Key key,
                                                                   QueryMethodEvaluationContextProvider evaluationContextProvider) {
        return Optional.of(new CouchDbQueryLookupStrategy(client));
    }
}
