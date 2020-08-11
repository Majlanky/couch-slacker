package com.groocraft.couchdb.slacker.http;

import org.apache.http.conn.ssl.TrustStrategy;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Implementation of {@link TrustStrategy} which is trusting all certificates.
 *
 * @author Majlanky
 */
public class TrustAllStrategy implements TrustStrategy {
    public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        return true;
    }
}
