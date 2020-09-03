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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Class providing reading functionality by calling a method.
 *
 * @param <DataT> Type of return from a method
 * @author Majlanky
 */
public class MethodReader<DataT> implements Reader<DataT> {

    private final Method method;

    public MethodReader(Method method) {
        this.method = method;
    }

    @Override
    @SuppressWarnings("unchecked")
    public DataT read(Object o) {
        try {
            return (DataT) method.invoke(o);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new AccessException(e);
        }
    }
}
