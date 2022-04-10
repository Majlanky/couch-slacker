/*
 * Copyright 2020-2022 the original author or authors.
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

import com.groocraft.couchdb.slacker.configuration.CouchDbProperties;
import org.jetbrains.annotations.NotNull;

/**
 * Bean provides automated way how to initialize the connected CouchDB on of the {@link CouchDbInitializationStrategy}.
 * Bean should be discovered and execute the configured initialization strategy.
 *
 * @author Majlanky
 */
public class CouchDbInitializer {

    /**
     * @param client     must not be {@literal null}
     * @param properties must not be {@literal null}
     * @throws Exception if the initialization of the selected strategy fail
     */
    public CouchDbInitializer(@NotNull CouchDbClient client, @NotNull CouchDbProperties properties) throws Exception {
        properties.getInitializationStrategy().initialize(client, properties);
    }

}
