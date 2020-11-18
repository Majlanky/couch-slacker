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

package com.groocraft.couchdb.slacker.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.groocraft.couchdb.slacker.exception.CouchDbException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpStatus;
import org.apache.http.RequestLine;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.util.Map;

/**
 * {@link HttpResponseInterceptor} which throws {@link CouchDbException} in case that response code is not OK, CREATED, ACCEPTED or NOT_MODIFIED. If there is
 * a body in request, it is checked for error and reason and it is returned as reason part of the exception. If body is not present, common reason phrase is
 * returned instead.
 *
 * @author Majlanky
 */
public class ThrowingInterceptor implements HttpResponseInterceptor {

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(HttpResponse response, HttpContext context) throws IOException {
        switch (response.getStatusLine().getStatusCode()) {
            case HttpStatus.SC_OK:
            case HttpStatus.SC_CREATED:
            case HttpStatus.SC_ACCEPTED:
            case HttpStatus.SC_NOT_MODIFIED:
                break;
            //Report all unknown states
            default:
                HttpClientContext clientContext = HttpClientContext.adapt(context);
                RequestLine requestLine = clientContext.getRequest().getRequestLine();
                String reason = response.getStatusLine().getReasonPhrase();
                if (response.getEntity() != null) {
                    ObjectMapper mapper = new ObjectMapper();
                    Map<String, String> wholeBody = mapper.readValue(response.getEntity().getContent(), mapper.getTypeFactory().constructMapType(Map.class,
                            String.class, String.class));
                    reason = wholeBody.get("error") + " : " + wholeBody.get("reason");
                }
                throw new CouchDbException(response.getStatusLine().getStatusCode(), requestLine.getMethod(), requestLine.getUri(), reason);
        }
    }
}
