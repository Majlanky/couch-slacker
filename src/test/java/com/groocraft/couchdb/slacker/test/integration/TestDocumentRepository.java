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

package com.groocraft.couchdb.slacker.test.integration;

import com.groocraft.couchdb.slacker.annotation.Query;
import com.groocraft.couchdb.slacker.annotation.ViewQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestDocumentRepository extends PagingAndSortingRepository<TestDocument, String> {

    @Query("{\"selector\": {\"value\": {\"$eq\": ?1}}}")
    List<TestDocument> queryBased(String value);

    @Query("{\"selector\": {\"value\": {\"$eq\": :value}}}")
    List<TestDocument> queryBasedWithNamed(@Param("value") String value);

    @ViewQuery(design = "view-query-test", view = "sum")
    List<TestDocument> viewQueryBased(Pageable p);

    @ViewQuery(design = "view-query-test", view = "sum", reducing = true)
    int viewQueryBasedWithReducing();

    List<TestDocument> findByValueAndValue2(@Param("value") String value, @Param("value2") String value2);

    List<TestDocument> findByValueIsNot(@Param("value") String value);

    boolean existsByValueAndValue2(@Param("value") String value, @Param("value2") String value2);

    int countByValueAndValue2(@Param("value") String value, @Param("value2") String value2);

    void deleteByValueAndValue2(@Param("value") String value, @Param("value2") String value2);

    List<TestDocument> findByValue3LessThan(@Param("value3") int value3);

    List<TestDocument> findByValue3LessThanEqual(@Param("value3") int value3);

    List<TestDocument> findByValue3GreaterThan(@Param("value3") int value3);

    List<TestDocument> findByValue3GreaterThanEqual(@Param("value3") int value3);

    List<TestDocument> findByValueRegex(@Param("value") String value);

    List<TestDocument> findByValue2Null();

    List<TestDocument> findByValue2NotNull();

    List<TestDocument> findByValue3Before(@Param("value3") int value3);

    List<TestDocument> findByValue3After(@Param("value3") int value3);

    List<TestDocument> findByValueStartingWith(@Param("value") String value);

    List<TestDocument> findByValueEndingWith(@Param("value") String value);

    List<TestDocument> findByValue4Empty();

    @SuppressWarnings("SpringDataMethodInconsistencyInspection")
    List<TestDocument> findByValue4NotEmpty();

    List<TestDocument> findByValueContaining(@Param("value") String value);

    List<TestDocument> findByValueNotContaining(@Param("value") String value);

    List<TestDocument> findByValueLike(@Param("value") String value);

    List<TestDocument> findByValueNotLike(@Param("value") String value);

    List<TestDocument> findByValueIn(@Param("value") List<String> value);

    List<TestDocument> findByValueNotIn(@Param("value") List<String> value);

    List<TestDocument> findByValue5True();

    List<TestDocument> findByValue5False();

    Page<TestDocument> findByValue(@Param("value") String value, Pageable pageable);

    Slice<TestDocument> findByValue2(@Param("value2") String value2, Pageable pageable);

    List<TestDocument> findTop80ByValue(@Param("value") String value);

    List<TestDocument> findByAddressStreet(@Param("street") String street);

    List<TestDocument> findByValueAndAddressStreet(String noName1, String noName2);

}
