package com.groocraft.couchdb.slacker.test.integration;

import com.groocraft.couchdb.slacker.TestDocument;
import com.groocraft.couchdb.slacker.annotation.Query;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestDocumentRepository extends CrudRepository<TestDocument, String> {

    @Query("{\"selector\": {\"value\": {\"$eq\": \":value\"}}}")
    List<TestDocument> queryBased(@Param("value") String value);

    @Query("{\"selector\": {\"value\": {\"$eq\": \":value\"}}}")
    List<TestDocument> queryBasedFailing();

    List<TestDocument> findByValueAndValue2(@Param("value") String value, @Param("value2") String value2);

    //Expecting the failure, missing parameter
    List<TestDocument> findByValue();

    boolean existsByValueAndValue2(@Param("value") String value, @Param("value2") String value2);

    //Expecting the failure, missing parameter
    boolean existsByValue();

    int countByValueAndValue2(@Param("value") String value, @Param("value2") String value2);

    //Expecting the failure, missing parameter
    int countByValue();

    void deleteByValueAndValue2(@Param("value") String value, @Param("value2") String value2);

    //Expecting the failure, missing parameter
    int deleteByValue();

}
