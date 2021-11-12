/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.groocraft.couchdb.slacker.annotation;

import com.groocraft.couchdb.slacker.CouchDbInitializer;
import com.groocraft.couchdb.slacker.SpringCouchDbContext;
import com.groocraft.couchdb.slacker.repository.CouchDBSchemaProcessor;
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
 *
 * @author Majlanky
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({CouchDbRepositoriesRegistrar.class, CouchDbInitializer.class, SpringCouchDbContext.class, CouchDBSchemaProcessor.class})
@SuppressWarnings("unsused")
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
