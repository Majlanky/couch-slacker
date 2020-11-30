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

package com.groocraft.couchdb.slacker.utils;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LazyLogTest {

    @Test
    void test() {
        AtomicBoolean called = new AtomicBoolean(false);
        LazyLog lazyLog = LazyLog.of(() -> {
            called.set(true);
            return "test";
        });
        assertFalse(called.get(), "Supplier passed into LazyLog must be called only in case of toString call. It prevents useless counting of string");
        String value = lazyLog.toString();
        assertTrue(called.get(), "Supplier passed into LazyLog must be called and used as source of toString value");
        assertEquals("test", value, "Supplier passed into LazyLog must be called and used as source of toString value");
    }

}