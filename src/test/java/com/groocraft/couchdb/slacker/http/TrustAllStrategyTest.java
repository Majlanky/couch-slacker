package com.groocraft.couchdb.slacker.http;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrustAllStrategyTest {

    @Test
    public void test() {
        assertDoesNotThrow(() -> {
            TrustAllStrategy strategy = new TrustAllStrategy();
            assertTrue(strategy.isTrusted(null, null), "The strategy must return always true");
        }, "The strategy must return always true");
    }

}