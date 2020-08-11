package com.groocraft.couchdb.slacker.repository;

import com.groocraft.couchdb.slacker.annotation.EnableCouchDbRepositories;
import org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

import java.lang.annotation.Annotation;

/**
 * Implementation of {@link RepositoryBeanDefinitionRegistrarSupport} to register {@link CouchDbRepositoryConfigurationExtension}. The implementation is
 * imported by {@link EnableCouchDbRepositories} annotation on a configuration bean.
 *
 * @author Majlanky
 * @see RepositoryBeanDefinitionRegistrarSupport
 */
public class CouchDbRepositoriesRegistrar extends RepositoryBeanDefinitionRegistrarSupport {

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<? extends Annotation> getAnnotation() {
        return EnableCouchDbRepositories.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected RepositoryConfigurationExtension getExtension() {
        return new CouchDbRepositoryConfigurationExtension();
    }
}
