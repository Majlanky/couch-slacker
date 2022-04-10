/*
 * Copyright 2022 the original author or authors.
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

package com.groocraft.couchdb.slacker.repository;

import com.groocraft.couchdb.slacker.CouchDbClient;
import com.groocraft.couchdb.slacker.configuration.CouchDbProperties;
import org.junit.jupiter.api.Test;
import org.springframework.data.repository.CrudRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CouchDbRepositoryFactoryBeanTest {

    @Test
    void test() {
        CouchDbClient client = mock(CouchDbClient.class);
        CouchDbProperties properties = mock(CouchDbProperties.class);
        when(properties.getBulkMaxSize()).thenReturn(100);
        CouchDbRepositoryFactoryBean<?, ?, ?> factoryBean = new CouchDbRepositoryFactoryBean<>(CrudRepository.class, client, properties);
        assertEquals(CouchDbRepositoryFactory.class, factoryBean.createRepositoryFactory().getClass(),
                CouchDbRepositoryFactoryBean.class + "must produce " + CouchDbRepositoryFactory.class);
    }

}