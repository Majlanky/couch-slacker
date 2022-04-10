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

import com.groocraft.couchdb.slacker.CouchDbClient;
import com.groocraft.couchdb.slacker.exception.CouchDbException;
import com.groocraft.couchdb.slacker.exception.CouchDbRuntimeException;
import org.apache.http.HttpStatus;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of {@link CrudRepository} which is providing basing DRUD operation above CouchDB thru {@link CouchDbClient}
 *
 * @param <EntityT> Type of entity with which is repository able to work
 * @author Majlanky
 * @see CrudRepository
 * @see CouchDbClient
 */
public class SimpleCouchDbRepository<EntityT> implements PagingAndSortingRepository<EntityT, String> {

    private final CouchDbClient client;
    private final Class<EntityT> clazz;

    /**
     * @param client must not be {@literal null}
     * @param clazz  must not be {@literal null}
     */
    public SimpleCouchDbRepository(@NotNull CouchDbClient client, @NotNull Class<EntityT> clazz) {
        Assert.notNull(client, "Client must not be null.");
        Assert.notNull(clazz, "Clazz must not be null.");
        this.client = client;
        this.clazz = clazz;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <S extends EntityT> S save(S entity) {
        try {
            return client.save(entity);
        } catch (IOException e) {
            throw new CouchDbRuntimeException("Unable to save " + entity, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <S extends EntityT> Iterable<S> saveAll(Iterable<S> entities) {
        try {
            return client.saveAll(entities, clazz);
        } catch (IOException e) {
            throw new CouchDbRuntimeException("Unable to save all", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("ConstantConditions")
    @Override
    public Optional<EntityT> findById(String id) {
        try {
            return Optional.ofNullable(client.read(id, clazz));
        } catch (CouchDbException couchDbException) {
            if (couchDbException.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                return Optional.empty();
            } else {
                throw new CouchDbRuntimeException("Unable to find " + id, couchDbException);
            }
        } catch (IOException e) {
            throw new CouchDbRuntimeException("Unable to find " + id, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsById(String id) {
        return findById(id).isPresent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<EntityT> findAll() {
        try {
            return client.readAll(client.readAll(clazz), clazz);
        } catch (IOException e) {
            throw new CouchDbRuntimeException("Unable to list all " + clazz.getSimpleName(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<EntityT> findAllById(Iterable<String> ids) {
        try {
            return client.readAll(ids, clazz);
        } catch (IOException e) {
            throw new CouchDbRuntimeException("Unable to find all by ids", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long count() {
        try {
            return client.countAll(clazz);
        } catch (IOException e) {
            throw new CouchDbRuntimeException("Unable to read all " + clazz.getSimpleName() + " for counting", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteById(String id) {
        try {
            client.deleteById(id, clazz);
        } catch (IOException e) {
            throw new CouchDbRuntimeException("Unable to delete " + id + " of " + clazz.getSimpleName(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(EntityT entity) {
        try {
            client.delete(entity);
        } catch (IOException e) {
            throw new CouchDbRuntimeException("Unable to delete " + entity, e);
        }
    }

    @Override
    public void deleteAllById(Iterable<? extends String> ids) {
        try {
            client.deleteAll(client.readAll((Iterable<String>) ids, clazz), clazz);
        } catch (IOException e) {
            throw new CouchDbRuntimeException("Unable to delete all by id", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAll(Iterable<? extends EntityT> entities) {
        try {
            client.deleteAll(entities, clazz);
        } catch (IOException e) {
            throw new CouchDbRuntimeException("Unable to delete all given", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAll() {
        try {
            client.deleteAll(clazz);
        } catch (IOException ex) {
            throw new CouchDbRuntimeException("Unable to delete all", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<EntityT> findAll(Sort sort) {
        try {
            return client.readAll(client.readAll(clazz, null, null, sort), clazz);
        } catch (IOException ex) {
            throw new CouchDbRuntimeException("Unable to find all with sorting", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<EntityT> findAll(Pageable pageable) {
        try {
            List<EntityT> documents = client.readAll(client.readAll(clazz, pageable.getOffset(), pageable.getPageSize(), pageable.getSort()), clazz);
            long totalCount = client.countAll(clazz);
            return new PageImpl<>(documents, pageable, totalCount);
        } catch (IOException ex) {
            throw new CouchDbRuntimeException("Unable to find all with sorting", ex);
        }
    }

}
