/*
 * Copyright 2020-2022 the original author or authors.
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

package com.groocraft.couchdb.slacker;

import com.groocraft.couchdb.slacker.annotation.Document;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;

import java.lang.annotation.Annotation;

/**
 * Implementation of the {@link Document} annotation for contextual overriding an annotation.
 *
 * @author Majlanky
 */
@SuppressWarnings("ClassExplicitlyAnnotation")
public class DocumentDescriptor implements Document {

    private final String database;
    private final boolean accessedByView;
    private final String design;
    private final String view;
    private final String typeField;
    private final String type;
    private final Class<?> entityClass;

    /**
     * @param entityClass    for what description is done. Must not be {@literal null}
     * @param database       value of {@link Document#database}. Must not be {@literal null}
     * @param accessedByView value of {@link Document#accessByView()}
     * @param design         value of {@link Document#design()} ()}. Must not be {@literal null}
     * @param view           value of {@link Document#view()}. Must not be {@literal null}
     * @param typeField      value of {@link Document#typeField()}. Must not be {@literal null}
     * @param type           value of {@link Document#type()}. Must not be {@literal null}
     */
    private DocumentDescriptor(@NotNull Class<?> entityClass,
                               @NotNull String database,
                               boolean accessedByView,
                               @NotNull String design,
                               @NotNull String view,
                               @NotNull String typeField,
                               @NotNull String type) {
        this.entityClass = entityClass;
        this.database = database;
        this.accessedByView = accessedByView;
        this.design = design;
        this.view = view;
        this.typeField = typeField;
        this.type = type;
    }

    /**
     * @param entityClass for what description is done. Must not be {@literal null}
     * @return {@link DocumentDescriptor} of the given entityClass with values from its annotation
     */
    public static DocumentDescriptor of(Class<?> entityClass) {
        Document document = entityClass.getAnnotation(Document.class);
        Assert.notNull(document, "EntityClass must be annotated by Document");
        document = AnnotationUtils.synthesizeAnnotation(document, Document.class);
        return new DocumentDescriptor(entityClass, document.database(), document.accessByView(), document.design(), document.view(), document.typeField(),
                document.type());
    }

    /**
     * @param entityClass for what description is done. Must not be {@literal null}
     * @param database    value of {@link Document#database}. Must not be {@literal null}
     * @return {@link DocumentDescriptor} of the given entityClass with overridden database.
     */
    public static DocumentDescriptor of(Class<?> entityClass, String database) {
        Document document = entityClass.getAnnotation(Document.class);
        Assert.notNull(document, "EntityClass must be annotated by Document");
        document = AnnotationUtils.synthesizeAnnotation(document, Document.class);
        return new DocumentDescriptor(entityClass, database, document.accessByView(), document.design(), document.view(), document.typeField(),
                document.type());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value() {
        return database;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String database() {
        return database;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean accessByView() {
        return accessedByView;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String design() {
        return design;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String view() {
        return view;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String typeField() {
        return typeField;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String type() {
        return type;
    }

    /**
     * {@inheritDoc}
     */
    public Class<? extends Annotation> annotationType() {
        return Document.class;
    }

    /**
     * {@inheritDoc}
     */
    public Class<?> getEntityClass() {
        return entityClass;
    }
}
