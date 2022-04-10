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
import com.groocraft.couchdb.slacker.test.integration.TestDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpringCouchDbContextTest {

    @Mock
    CouchDbProperties properties;

    @Mock
    ApplicationContext applicationContext;

    @Mock
    CouchDbClient client;

    CouchDbContext context;

    @Test
    void testDefault() throws ClassNotFoundException {
        when(properties.getMapping()).thenReturn(Collections.emptyMap());
        Map<String, Object> beans = new HashMap<>();
        beans.put("testDocumentBean", new TestDocument());
        when(applicationContext.getBeansWithAnnotation(any())).thenReturn(beans);
        CouchDbContext context = new SpringCouchDbContext(properties, applicationContext, null);
        assertEquals("test", context.get(TestDocument.class).getDatabaseName(),
                "In the default context without any overlaying configuration values from Document annotation must be returned");
    }

    @Test
    void testSwitching() throws ClassNotFoundException {
        Map<String, List<CouchDbProperties.Document>> mapping = new HashMap<>();
        mapping.put("test", Collections.singletonList(new CouchDbProperties.Document(TestDocument.class.getName(), "anotherTest")));
        when(properties.getMapping()).thenReturn(mapping);
        Map<String, Object> beans = new HashMap<>();
        beans.put("testDocumentBean", new TestDocument());
        when(applicationContext.getBeansWithAnnotation(any())).thenReturn(beans);
        CouchDbContext context = new SpringCouchDbContext(properties, applicationContext, null);
        assertEquals("test", context.get(TestDocument.class).getDatabaseName(),
                "In the default context without any overlaying configuration values from Document annotation must be returned");
        context.set("test");
        assertEquals("anotherTest", context.get(TestDocument.class).getDatabaseName(),
                "In the test context must be returned the configured value");
        context.remove();
    }

    @Test
    void testDefaultOverriding() throws ClassNotFoundException {
        Map<String, List<CouchDbProperties.Document>> mapping = new HashMap<>();
        mapping.put("default", Collections.singletonList(new CouchDbProperties.Document(TestDocument.class.getName(), "anotherTest")));
        when(properties.getMapping()).thenReturn(mapping);
        Map<String, Object> beans = new HashMap<>();
        beans.put("testDocumentBean", new TestDocument());
        when(applicationContext.getBeansWithAnnotation(any())).thenReturn(beans);
        CouchDbContext context = new SpringCouchDbContext(properties, applicationContext, null);
        assertEquals("anotherTest", context.get(TestDocument.class).getDatabaseName(),
                "If the default context is override by overlaying configuration, the configured value must be returned");
    }

    @Test
    void testDoInSwitching() throws ClassNotFoundException {
        Map<String, List<CouchDbProperties.Document>> mapping = new HashMap<>();
        mapping.put("test", Collections.singletonList(new CouchDbProperties.Document(TestDocument.class.getName(), "anotherTest")));
        when(properties.getMapping()).thenReturn(mapping);
        Map<String, Object> beans = new HashMap<>();
        beans.put("testDocumentBean", new TestDocument());
        when(applicationContext.getBeansWithAnnotation(any())).thenReturn(beans);
        CouchDbContext context = new SpringCouchDbContext(properties, applicationContext, null);
        assertEquals("test", context.get(TestDocument.class).getDatabaseName(),
                "In the default context without any overlaying configuration values from Document annotation must be returned");
        AtomicReference<String> contextualDbName = new AtomicReference<>();
        context.doIn("test", () -> contextualDbName.set(context.get(TestDocument.class).getDatabaseName()));
        assertEquals("anotherTest", contextualDbName.get(),
                "doIn method is not working properly because it should do the passed action inside test context, hence the configured value must be returned");
        assertEquals("test", context.get(TestDocument.class).getDatabaseName(),
                "doIn method must remove the context setting in the end. Because non-annotation value was returned, remove was not probably called");
    }

}