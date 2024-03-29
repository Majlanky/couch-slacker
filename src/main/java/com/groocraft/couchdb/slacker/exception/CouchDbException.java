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

package com.groocraft.couchdb.slacker.exception;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Exception which is thrown in case of non-OK response from server. For details see {@link com.groocraft.couchdb.slacker.http.ThrowingInterceptor}.
 * Exception holds status code, method of HTTP request, URI on which request was called and reason of failure.
 *
 * @author Majlanky
 */
public class CouchDbException extends IOException {

    private final int statusCode;

    /**
     * @param statusCode A valid HTTP status code that caused the exceptional state
     * @param method     A valid HTTP method of the executed request
     * @param uri        on which method was executed
     * @param reason     of the given {@code statusCode}
     */
    public CouchDbException(int statusCode, @NotNull String method, @NotNull String uri, @NotNull String reason) {
        super(String.format("%s when trying %s on %s with reason: %s", statusCode, method, uri, reason));
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

}
