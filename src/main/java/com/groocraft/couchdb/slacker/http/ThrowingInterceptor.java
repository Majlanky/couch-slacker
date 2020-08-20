package com.groocraft.couchdb.slacker.http;

import com.groocraft.couchdb.slacker.exception.CouchDbException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpStatus;
import org.apache.http.RequestLine;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;

public class ThrowingInterceptor implements HttpResponseInterceptor {

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
                throw new CouchDbException(response.getStatusLine().getStatusCode(), requestLine.getMethod(), requestLine.getUri(), response.getStatusLine().getReasonPhrase());
        }
    }
}
