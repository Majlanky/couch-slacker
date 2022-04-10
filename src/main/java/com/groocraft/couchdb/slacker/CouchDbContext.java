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

import com.groocraft.couchdb.slacker.configuration.CouchDbProperties;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.Assert;

import javax.annotation.concurrent.ThreadSafe;
import java.util.HashMap;
import java.util.Map;


/**
 * Inteface which provides feature of database name context switching
 *
 * @author Majlanky
 * @see com.groocraft.couchdb.slacker.configuration.CouchDbProperties
 */
@Slf4j
@ThreadSafe
public abstract class CouchDbContext {

    public static final String DEFAULT = "default";

    private static final ThreadLocal<String> contextName = ThreadLocal.withInitial(() -> CouchDbContext.DEFAULT);
    private final Map<String, Map<Class<?>, EntityMetadata>> entityMetadata = new HashMap<>();
    private final CouchDbProperties couchDbProperties;

    /**
     * @param couchDbProperties must not be {@literal null}
     */
    protected CouchDbContext(@NotNull CouchDbProperties couchDbProperties) {
        Assert.notNull(couchDbProperties, "CouchDbProperties must not be null");
        this.couchDbProperties = couchDbProperties;
        entityMetadata.put(DEFAULT, new HashMap<>());
        couchDbProperties.getMapping().keySet().forEach(s -> entityMetadata.put(s, new HashMap<>()));
    }

    /**
     * @return {@link CouchDbProperties}
     */
    protected @NotNull CouchDbProperties getCouchDbProperties() {
        return couchDbProperties;
    }

    /**
     * @return non-null map of all known contexts with the pertinent configuration.
     */
    public @NotNull Map<String, Map<Class<?>, EntityMetadata>> getAll() {
        Map<String, Map<Class<?>, EntityMetadata>> copy = new HashMap<>();
        entityMetadata.forEach((k, v) -> copy.put(k, new HashMap<>(v)));
        return copy;
    }

    /**
     * Method adds the given description to the {@link CouchDbContext#DEFAULT} context.
     *
     * @param descriptor of entity to be added to {@link CouchDbContext#DEFAULT} context.
     * @return created {@link EntityMetadata} instance of the given {@code descriptor}
     */
    protected @NotNull EntityMetadata add(@NotNull DocumentDescriptor descriptor) {
        return add(CouchDbContext.DEFAULT, descriptor);
    }

    /**
     * Method adds the given description to a context of the given name.
     *
     * @param descriptor of entity to be added to a context of the given name.
     * @param name       of context to what description is added to
     * @return created {@link EntityMetadata} instance of the given {@code descriptor}
     */
    protected EntityMetadata add(String name, DocumentDescriptor descriptor) {
        EntityMetadata metadata = new EntityMetadata(descriptor);
        entityMetadata.computeIfAbsent(name, k -> new HashMap<>()).put(descriptor.getEntityClass(), metadata);
        return metadata;
    }

    /**
     * Sets name of context that should be used. The method is proxy call to {@link ThreadLocal#set(Object)} so it must be ended by {@link #remove()} call or
     * to simplify whole operation {@link #doIn(String, Runnable)} can be used.
     *
     * @param name of wanted context. Must not be {@literal null}
     * @see ThreadLocal
     */
    public void set(@NotNull String name) {
        contextName.set(name);
    }

    /**
     * Methods returns {@link EntityMetadata} for the given class.
     *
     * @param clazz of wanted entity. Must not be {@literal null}
     * @return non-null {@link EntityMetadata} for the given class. If the entity is not extended in the actual context, result from
     * {@link CouchDbContext#DEFAULT} context is used.
     */
    public @NotNull EntityMetadata get(@NotNull Class<?> clazz) {
        return entityMetadata.get(contextName.get()).get(clazz);
    }

    /**
     * Tests if the context contains {@link EntityMetadata} for the given class.
     *
     * @param clazz must not be {@literal null}
     * @return true if the context contains metadata for the given class, false otherwise.
     */
    public boolean contains(@NotNull Class<?> clazz) {
        return entityMetadata.get(contextName.get()).containsKey(clazz);
    }

    /**
     * Removes the actual setting of the used context. The method is proxy call to {@link ThreadLocal#remove()}.
     */
    public void remove() {
        contextName.remove();
    }

    /**
     * Method executes the given {@code action} in the given context. It is safe wrapper for {@link #set(String)} and {@link #remove()}.
     *
     * @param contextName in what the given action will be executed. Must not be {@literal null}
     * @param action      the will be executed in a context with the given name. Must not be {@literal null}
     */
    public void doIn(String contextName, Runnable action) {
        set(contextName);
        action.run();
        remove();
    }

}
