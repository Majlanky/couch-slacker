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

package com.groocraft.couchdb.slacker.exception;

import org.jetbrains.annotations.NotNull;

/**
 * Exception used during states caused by wrong access implementation/configuration in {@link com.groocraft.couchdb.slacker.data.Reader} and
 * {@link com.groocraft.couchdb.slacker.data.Writer} implementations. The likeliest use-cases are tries to access non-existing field, method or accessing
 * method with wrong number or type parameters.
 *
 * @author Majlanky
 */
public class AccessException extends RuntimeException {

    public AccessException(@NotNull Throwable cause) {
        super(cause);
    }
}
