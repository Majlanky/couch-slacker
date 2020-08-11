package com.groocraft.couchdb.slacker;

import com.groocraft.couchdb.slacker.http.ThrowingInterceptor;
import com.groocraft.couchdb.slacker.http.TrustAllStrategy;
import org.apache.http.Consts;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 * Basic configuration bean to get all necessary resources for {@link org.springframework.data.repository.Repository} implementations. Properties are read
 * automatically into {@link CouchDbProperties} used in this class.
 *
 * @author Majlanky
 */
//TODO check how this should be done in standard way and rework/extend it
@Configuration
@EnableConfigurationProperties(CouchDbProperties.class)
public class CouchSlackerConfiguration {

    private static final String HTTP_CLIENT_BEAN_NAME = "couchDbHttpClient";
    private static final String HTTP_HOST_BEAN_NAME = "couchDbHttpHost";
    private static final String HTTP_CONTEXT_BEAN_NAME = "couchDbHttpContext";
    private static final String BASE_URI_BEAN_NAME = "couchDbBaseURI";

    @Bean(destroyMethod = "close")
    public CouchDbClient dbClient(@Qualifier(HTTP_CLIENT_BEAN_NAME) HttpClient httpClient,
                                  @Qualifier(HTTP_HOST_BEAN_NAME) HttpHost httpHost, @Qualifier(HTTP_CONTEXT_BEAN_NAME) HttpContext httpContext,
                                  @Qualifier(BASE_URI_BEAN_NAME) URI uri) {
        return new CouchDbClient(httpClient, httpHost, httpContext, uri);
    }

    @Bean(name = HTTP_CLIENT_BEAN_NAME)
    public HttpClient httpClient() {
        try {
            PoolingHttpClientConnectionManager ccm = new PoolingHttpClientConnectionManager(getRegistry());
            HttpClientBuilder clientBuilder = HttpClients.custom()
                    .setConnectionManager(ccm)
                    .setDefaultConnectionConfig(ConnectionConfig.custom()
                            .setCharset(Consts.UTF_8).build())
                    .setDefaultRequestConfig(RequestConfig.DEFAULT)
                    .addInterceptorFirst(new ThrowingInterceptor())
                    .setRetryHandler(new DefaultHttpRequestRetryHandler(0, false));
            return clientBuilder.build();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to create HTTP client", e);
        }
    }

    @Bean(name = HTTP_HOST_BEAN_NAME)
    public HttpHost httpHost(@Qualifier(BASE_URI_BEAN_NAME) URI uri) {
        return new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
    }

    @Bean(name = HTTP_CONTEXT_BEAN_NAME)
    public HttpContext httpContext(@Qualifier(HTTP_HOST_BEAN_NAME) HttpHost httpHost, CouchDbProperties properties) {
        AuthCache authCache = new BasicAuthCache();
        authCache.put(httpHost, new BasicScheme());
        HttpContext context = new BasicHttpContext();
        context.setAttribute(HttpClientContext.AUTH_CACHE, authCache);
        properties.getUsername().ifPresent(u -> context.setAttribute(HttpClientContext.CREDS_PROVIDER, getCredentialProvider(u, properties.getPassword())));
        return context;
    }

    @Bean(name = BASE_URI_BEAN_NAME)
    public URI uri(CouchDbProperties properties) throws URISyntaxException {
        return new URI(properties.getUrl());
    }

    /**
     * Method to configure and get {@link Registry} of {@link ConnectionSocketFactory} for http and https. In case of https all certificated are trusted.
     *
     * @return {@link Registry}
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    private Registry<ConnectionSocketFactory> getRegistry() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        // @formatter:off
        return RegistryBuilder.<ConnectionSocketFactory>create()
                .register("https", new SSLConnectionSocketFactory(SSLContexts.custom().loadTrustMaterial(new TrustAllStrategy()).build(),
                        new NoopHostnameVerifier()))
                .register("http", PlainConnectionSocketFactory.INSTANCE).build();
        // @formatter:on
    }

    /**
     * Method to get configured {@link CredentialsProvider} which is using {@code username} and {@code password} read from {@link CouchDbProperties}
     *
     * @param username of a valid CouchDB user which is used to authenticate during every request. Must not be {@literal null}
     * @param password of a valid CouchDB user which is used to authenticate during every request
     * @return prepared {@link CredentialsProvider}
     */
    private CredentialsProvider getCredentialProvider(String username, String password) {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                new UsernamePasswordCredentials(username, password));
        return credentialsProvider;
    }

}
