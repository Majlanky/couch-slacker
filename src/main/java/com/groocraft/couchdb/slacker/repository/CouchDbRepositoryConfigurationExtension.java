package com.groocraft.couchdb.slacker.repository;

import org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport;

/**
 * Implementation of {@link RepositoryConfigurationExtensionSupport} to inject named queries from {@literal couchDb-named-queries.properties} placed in
 * META-INF (happens in {@link RepositoryConfigurationExtensionSupport#getDefaultNamedQueryLocation()}) and register {@link CouchDbRepositoryFactoryBean}
 * which is providing factory of CouchDB repositories.
 *
 * @author Majlanky
 * @see RepositoryConfigurationExtensionSupport
 */
public class CouchDbRepositoryConfigurationExtension extends RepositoryConfigurationExtensionSupport {

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getModulePrefix() {
        return "couchDb";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRepositoryFactoryBeanClassName() {
        return CouchDbRepositoryFactoryBean.class.getName();
    }
}
