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

package com.groocraft.couchdb.slacker.http;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;

import java.io.IOException;

/**
 * Because {@link HttpResponse} does not implements {@link AutoCloseable}, it is not as easy as possible to use it in try-catch statement with a resource.
 * This class is wrapper to add auto-closeable feature for {@link HttpResponse}.
 *
 * @author Majlanky
 * @see HttpResponse
 */
@Slf4j
public class AutoCloseableHttpResponse implements AutoCloseable {

    private HttpResponse response;

    public void set(HttpResponse response) {
        this.response = response;
    }

    public HttpResponse get() {
        return response;
    }

    @Override
    public void close() {
        if(response != null){
            try {
                response.getEntity().getContent().close();
            } catch (IOException e) {
                log.error("Unable to close input stream");
            }
        }
    }

}
