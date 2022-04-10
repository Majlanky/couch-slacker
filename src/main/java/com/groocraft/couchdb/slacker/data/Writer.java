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
import org.jetbrains.annotations.Nullable;

/**
 * Interface providing writing functionality
 *
 * @param <DataT> Type of the written data
 * @author Majlanky
 */
public interface Writer<DataT> {

    /**
     * Method which writes data to the given destination.
     *
     * @param o    Object which data should be written to. Must not be {@literal null}
     * @param data Data which will be written in to the given destination
     */
    void write(@NotNull Object o, @Nullable DataT data);

}
