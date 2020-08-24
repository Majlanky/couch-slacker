/*
 * Copyright 2014-2020 the original author or authors.
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
import org.springframework.data.repository.CrudRepository;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.StreamSupport;

/**
 * Implementation of {@link CrudRepository} which is providing basing DRUD operation above CouchDB thru {@link CouchDbClient}
 *
 * @param <EntityT> Type of entity with which is repository able to work
 * @author Majlanky
 * @see CrudRepository
 * @see CouchDbClient
 */
//TODO paging and sorting interface instead of crud one
public class SimpleCouchDbRepository<EntityT> implements CrudRepository<EntityT, String> {

    private final CouchDbClient client;
    private final Class<EntityT> clazz;

    /**
     * @param client must not be {@literal null}
     * @param clazz  must not be {@literal null}
     */
    public SimpleCouchDbRepository(CouchDbClient client, Class<EntityT> clazz) {
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
    @Override
    public Optional<EntityT> findById(String id) {
        try {
            return Optional.ofNullable(client.read(id, clazz));
        } catch(CouchDbException couchDbException){
            if(couchDbException.getStatusCode() == HttpStatus.SC_NOT_FOUND){
                return Optional.empty();
            } else {
                throw new CouchDbRuntimeException("Unable to find " + id, couchDbException);
            }
        }
        catch (IOException e) {
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
     * This implementation is done in lazy way. No actual request is send until somebody wants a result.
     *
     * {@inheritDoc}
     */
    @Override
    public Iterable<EntityT> findAll() {
        try {
            return client.readAll(client.readAll(clazz), clazz);
        } catch (IOException e){
            throw new CouchDbRuntimeException("Unable to list all " + clazz.getSimpleName(), e);
        }
    }

    /**
     * This implementation is done in lazy way. No actual request is send until somebody wants a result.
     *
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
        try{
            return StreamSupport.stream(client.readAll(clazz).spliterator(), false).count();
        } catch (IOException e){
            throw new CouchDbRuntimeException("Unable to read all " + clazz.getSimpleName() + " for counting", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteById(String id) {
        try{
            client.deleteById(id, clazz);
        } catch (IOException e){
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
        try{
            client.deleteAll(clazz);
        } catch (IOException ex){
            throw new CouchDbRuntimeException("Unable to delete all", ex);
        }
    }
}
