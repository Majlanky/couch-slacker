package com.groocraft.couchdb.slacker.http;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.util.Locale;
import java.util.Optional;

/**
 * Because {@link HttpResponse} does not implements {@link AutoCloseable}, it is not as easy as possible to use it in try-catch statement with a resource.
 * This class is wrapper to add auto-closeable feature for {@link HttpResponse}.
 *
 * @author Majlanky
 * @see HttpResponse
 */
@Slf4j
public class AutoCloseableHttpResponse implements AutoCloseable {

    private Optional<HttpResponse> response = Optional.empty();

    public void set(HttpResponse response) {
        this.response = Optional.of(response);
    }

    public HttpResponse get() {
        return response.orElseThrow();
    }

    @Override
    public void close() {
        response.ifPresent(r -> {
            try {
                r.getEntity().getContent().close();
            } catch (IOException e) {
                log.error("Unable to close input stream");
            }
        });
    }

}
