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

package com.groocraft.couchdb.slacker;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class CouchDbClientBuilderTest {

    @Mock
    CouchDbContext dbContext;

    @Test
    public void testInvalid() {
        CouchDbClientBuilder builder = new CouchDbClientBuilder();
        assertThrows(IllegalArgumentException.class, builder::build, "Non-configured URL must be reported");
        builder.url("http://localhost:5984");
        assertThrows(IllegalArgumentException.class, builder::build, "Non-configured username must be reported");
        builder.username("admin");
        assertThrows(IllegalArgumentException.class, builder::build, "Non-configured password must be reposted");
        builder.password("password");
        assertThrows(IllegalArgumentException.class, builder::build, "Non-configured database context must be reposted");
        builder.dbContext(dbContext);
        assertDoesNotThrow(builder::build, "URL, username, password are configured, the rest of configuration has default values");
    }

}