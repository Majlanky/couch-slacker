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

package com.groocraft.couchdb.slacker.data;

import com.groocraft.couchdb.slacker.exception.AccessException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.Assert;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Class providing writing functionality by calling a method.
 *
 * @param <DataT> Type of method parameter
 * @author Majlanky
 */
public class MethodWriter<DataT> implements Writer<DataT> {

    private final Method method;

    /**
     * @param method which is called with the data
     */
    public MethodWriter(@NotNull Method method) {
        Assert.notNull(method, "Method must not be null");
        this.method = method;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(@NotNull Object o, @Nullable DataT data) {
        Assert.notNull(o, "Object must not be null");
        try {
            method.invoke(o, data);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new AccessException(e);
        }
    }
}
