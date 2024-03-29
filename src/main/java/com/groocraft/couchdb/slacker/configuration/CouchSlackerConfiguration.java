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

package com.groocraft.couchdb.slacker.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.groocraft.couchdb.slacker.CouchDbClient;
import com.groocraft.couchdb.slacker.IdGenerator;
import com.groocraft.couchdb.slacker.SpringCouchDbContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.util.Assert;

import java.util.List;

/**
 * Basic class which can be used as extension of {@link org.springframework.context.annotation.Configuration} class and enable reading Couch Slacker
 * configuration from yaml or properties configuration files.
 *
 * @author Majlanky
 */
@EnableConfigurationProperties(CouchDbProperties.class)
public class CouchSlackerConfiguration {

    /**
     * @param properties   of Couch Slacker. Must not be {@literal null}
     * @param idGenerators all configured beans of {@link IdGenerator} class. Can be {@literal null}
     * @param dbContext    must not be {@literal null}
     * @param objectMapper used for (de)serialization of all communication. Can be {@literal null}
     * @return {@link CouchDbClient} with the given properties
     */
    @Bean(destroyMethod = "close")
    public CouchDbClient dbClient(@NotNull CouchDbProperties properties,
                                  @Nullable @Autowired(required = false) List<IdGenerator<?>> idGenerators,
                                  @NotNull @Lazy SpringCouchDbContext dbContext,
                                  @Nullable @Autowired(required = false) ObjectMapper objectMapper) {
        Assert.notNull(properties, "Properties must not be null.");
        return CouchDbClient.builder().properties(properties).idGenerators(idGenerators).dbContext(dbContext).objectMapper(objectMapper).build();
    }

}
