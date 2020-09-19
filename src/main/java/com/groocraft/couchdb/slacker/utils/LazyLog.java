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

package com.groocraft.couchdb.slacker.utils;

import org.jetbrains.annotations.NotNull;
import org.springframework.util.Assert;

import java.util.function.Supplier;

/**
 * Help class to prevent useless computation of log parameters in case the requested level is turned off. It is not ideal because for every log parameter
 * instance of {@link LazyLog} is created so it consumes more heap but it can provide some boost of performance if computation of the parameter is not
 * trivial.
 *
 * @author Majlanky
 */
public class LazyLog {

    private final Supplier<Object> supplier;

    /**
     * @param supplier of data, which should be logged when needed. Must not be {@literal null}
     */
    private LazyLog(Supplier<Object> supplier) {
        Assert.notNull(supplier, "Supplier must not be null.");
        this.supplier = supplier;
    }

    /**
     * More fluent way to create instance of the class.
     *
     * @param supplier providing log parameter as result of a computation with which {@link String#valueOf(Object)} is called to provide textual version of
     *                 the parameter. Must not be {@literal null}
     * @return instance of {@link LazyLog} prepared to provide textual version of computed parameter if requested thru {@link #toString()}
     */
    public static @NotNull LazyLog of(@NotNull Supplier<Object> supplier) {
        return new LazyLog(supplier);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.valueOf(supplier.get());
    }
}
