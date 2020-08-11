package com.groocraft.couchdb.slacker.repository;

import com.groocraft.couchdb.slacker.CouchDbClient;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import java.io.Serializable;

/**
 * Implementation of {@link RepositoryFactoryBeanSupport}. The implementation registers {@link CouchDbRepositoryFactory}
 *
 * @author Majlanky
 * @see RepositoryFactoryBeanSupport
 */
public class CouchDbRepositoryFactoryBean<T extends Repository<S, ID>, S, ID extends Serializable>
        extends RepositoryFactoryBeanSupport<T, S, ID> {

    private final CouchDbClient client;

    /**
     * @param repositoryInterface must not be {@literal null}.
     */
    protected CouchDbRepositoryFactoryBean(Class<? extends T> repositoryInterface, CouchDbClient client) {
        super(repositoryInterface);
        this.client = client;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected RepositoryFactorySupport createRepositoryFactory() {
        return new CouchDbRepositoryFactory(client);
    }
}
