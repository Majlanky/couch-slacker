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

import org.jetbrains.annotations.NotNull;

/**
 * Interface providing reading functionality
 *
 * @param <DataT> Type of return
 * @author Majlanky
 */
public interface Reader<DataT> {

    /**
     * Method which return data read from the given source.
     *
     * @param o Object which from data should be read. Must not be {@literal null}
     * @return read data
     */
    DataT read(@NotNull Object o);

}
