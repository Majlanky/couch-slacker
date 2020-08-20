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
