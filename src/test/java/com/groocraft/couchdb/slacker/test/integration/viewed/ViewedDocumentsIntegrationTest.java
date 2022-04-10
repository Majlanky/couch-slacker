/*
 * Copyright 2020 the original author or authors.
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

package com.groocraft.couchdb.slacker.test.integration.viewed;

import com.groocraft.couchdb.slacker.CouchDbInitializer;
import com.groocraft.couchdb.slacker.annotation.EnableCouchDbRepositories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ViewedTestConfiguration.class, ViewedCatRepository.class, ViewedDogRepository.class,
        ViewedOverviewRepository.class, CouchDbInitializer.class},
        initializers = ConfigDataApplicationContextInitializer.class)
@ActiveProfiles("viewed-test")
@EntityScan({"com.groocraft.couchdb.slacker.test.integration.viewed"})
@EnableCouchDbRepositories
class ViewedDocumentsIntegrationTest {

    @Autowired
    ViewedDogRepository dogRepository;

    @Autowired
    ViewedCatRepository catRepository;

    @Autowired
    ViewedOverviewRepository overviewRepository;

    @BeforeEach
    void delete() {
        dogRepository.deleteAll();
        catRepository.deleteAll();
    }

    @Test
    void testSaveAndViewedFindAll() {
        List<ViewedDog> dogs = new LinkedList<>();
        List<ViewedCat> cats = new LinkedList<>();
        IntStream.range(1, 21).forEach(i -> {
            dogRepository.save(new ViewedDog("dog" + i));
            catRepository.save(new ViewedCat("cat" + i));
        });
        dogRepository.saveAll(dogs);
        catRepository.saveAll(cats);
        assertEquals(40, overviewRepository.count(), "There should be 40 animals in the database (20 dogs + 20 cats)");

        assertEquals(20, dogRepository.count(), "There should be exactly 20 dogs in database. Maybe there is a mismatch in data");
        assertEquals(20, catRepository.count(), "There should be exactly 20 cats in database. Maybe there is a mismatch in data");
    }

    @Test
    void testSaveAllAndViewedFindAll() {
        List<ViewedDog> dogs = new LinkedList<>();
        List<ViewedCat> cats = new LinkedList<>();
        IntStream.range(1, 21).forEach(i -> {
            dogs.add(new ViewedDog("dog" + i));
            cats.add(new ViewedCat("cat" + i));
        });
        dogRepository.saveAll(dogs);
        catRepository.saveAll(cats);
        assertEquals(40, overviewRepository.count(), "There should be 40 animals in the database (20 dogs + 20 cats)");

        assertEquals(20, dogRepository.count(), "There should be exactly 20 dogs in database. Maybe there is a mismatch in data");
        assertEquals(20, catRepository.count(), "There should be exactly 20 cats in database. Maybe there is a mismatch in data");
    }

}
