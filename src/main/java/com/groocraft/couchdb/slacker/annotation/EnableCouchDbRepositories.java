package com.groocraft.couchdb.slacker.annotation;

import com.groocraft.couchdb.slacker.repository.CouchDbRepositoriesRegistrar;
import com.groocraft.couchdb.slacker.repository.CouchDbRepositoryFactoryBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.repository.config.DefaultRepositoryBaseClass;
import org.springframework.data.repository.query.QueryLookupStrategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to enable CouchDB repositories. Will scan the package of the annotated configuration class for Spring Data repositories by default.
 * @author Majlanky
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(CouchDbRepositoriesRegistrar.class)
public @interface EnableCouchDbRepositories {

    String[] value() default {};

    String[] basePackages() default {};

    Class<?>[] basePackageClasses() default {};

    ComponentScan.Filter[] includeFilters() default {};

    ComponentScan.Filter[] excludeFilters() default {};

    String repositoryImplementationPostfix() default "Impl";

    String namedQueriesLocation() default "";

    QueryLookupStrategy.Key queryLookupStrategy() default QueryLookupStrategy.Key.CREATE_IF_NOT_FOUND;

    Class<?> repositoryFactoryBeanClass() default CouchDbRepositoryFactoryBean.class;

    Class<?> repositoryBaseClass() default DefaultRepositoryBaseClass.class;

    boolean considerNestedRepositories() default false;

}
