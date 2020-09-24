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

package com.groocraft.couchdb.slacker.data;

import com.groocraft.couchdb.slacker.exception.AccessException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.Assert;

import java.lang.reflect.Field;

/**
 * Class providing writing and reading functionality to any {@link Field}
 *
 * @param <DataT> Type of accessed data
 * @author Majlanky
 */
public class FieldAccessor<DataT> implements Writer<DataT>, Reader<DataT> {

    private final Field field;

    /**
     * @param field which is accessed. Must not be {@literal null}
     */
    public FieldAccessor(@NotNull Field field) {
        Assert.notNull(field, "Field must not be null");
        this.field = field;
        this.field.setAccessible(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public DataT read(@NotNull Object o) {
        Assert.notNull(o, "Object must not be null");
        try {
            return (DataT) field.get(o);
        } catch (IllegalAccessException e) {
            throw new AccessException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(@NotNull Object o, @Nullable DataT data) {
        Assert.notNull(o, "Object must not be null");
        try {
            field.set(o, data);
        } catch (IllegalAccessException e) {
            throw new AccessException(e);
        }
    }
}
