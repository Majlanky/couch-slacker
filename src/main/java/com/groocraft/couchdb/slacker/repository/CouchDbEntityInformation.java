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

package com.groocraft.couchdb.slacker.repository;

import com.groocraft.couchdb.slacker.DocumentBase;
import com.groocraft.couchdb.slacker.EntityMetadata;
import com.groocraft.couchdb.slacker.data.Reader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.util.Assert;

/**
 * Implementation of {@link EntityInformation} for resolving CouchDB {@link DocumentBase} and other properly annotated objects
 *
 * @param <EntityT> Type of entity
 * @param <IdT>     Should be string, because CouchDB is indexed by UUID in String type, generic is here for better usage of the class in
 *                  {@link RepositoryFactorySupport#getEntityInformation(Class)} method
 * @author Majlanky
 * @see EntityInformation
 */
public class CouchDbEntityInformation<EntityT, IdT> implements EntityInformation<EntityT, IdT> {

    private final Class<EntityT> clazz;
    private final Reader<String> idReader;
    private final Reader<String> revisionReader;

    /**
     * @param clazz          of entity type about which information is needed. Must not be {@literal null}
     * @param entityMetadata {@link EntityMetadata} for the entity class. Used to get {@link Reader} for revision and id. Must not be {@literal null}.
     */
    public CouchDbEntityInformation(@NotNull Class<EntityT> clazz, @NotNull EntityMetadata entityMetadata) {
        Assert.notNull(entityMetadata, "EntityMetadata must not be null.");
        this.clazz = clazz;
        idReader = entityMetadata.getIdReader();
        revisionReader = entityMetadata.getRevisionReader();
    }

    /**
     * {@inheritDoc}
     *
     * @return true if revision is empty
     */
    @Override
    public boolean isNew(EntityT entity) {
        String revision = revisionReader.read(entity);
        return ("".equals(revision) || revision == null);
    }

    /**
     * {@inheritDoc}
     *
     * @return documents UUID
     */
    @Override
    @SuppressWarnings("unchecked")
    public @Nullable IdT getId(@NotNull EntityT entity) {
        return (IdT) idReader.read(entity);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link String} class, because CouchDB _id must be string
     */
    @Override
    @SuppressWarnings("unchecked")
    public @NotNull Class<IdT> getIdType() {
        return (Class<IdT>) String.class;
    }

    /**
     * {@inheritDoc}
     *
     * @return class of Entity
     */
    @Override
    public @NotNull Class<EntityT> getJavaType() {
        return clazz;
    }
}
