package com.groocraft.couchdb.slacker.repository;

import com.groocraft.couchdb.slacker.EntityMetadata;
import com.groocraft.couchdb.slacker.data.Reader;
import com.groocraft.couchdb.slacker.test.integration.TestDocument;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CouchDbEntityInformationTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testFind(){
        Reader<String> idReader = Mockito.mock(Reader.class);
        Reader<String> revisionReader = Mockito.mock(Reader.class);
        EntityMetadata metadata = Mockito.mock(EntityMetadata.class);
        Mockito.when(metadata.getIdReader()).thenReturn(idReader);
        Mockito.when(metadata.getRevisionReader()).thenReturn(revisionReader);

        CouchDbEntityInformation<TestDocument, String> entityInformation = new CouchDbEntityInformation<>(TestDocument.class, metadata);
        assertEquals(TestDocument.class, entityInformation.getJavaType(), "Reporting wrong java type, it must match metadata information");
        Mockito.when(revisionReader.read(Mockito.any())).thenReturn("");
        assertTrue(entityInformation.isNew(new TestDocument()), "If there is no revision in the entity instance, it has to be reported as new");
        Mockito.when(revisionReader.read(Mockito.any())).thenReturn("revision");
        assertFalse(entityInformation.isNew(new TestDocument()), "If there is a revision in the entity instance, it must not be reported as new");
        Mockito.when(idReader.read(Mockito.any())).thenReturn("unique");
        assertEquals("unique", entityInformation.getId(new TestDocument()), "Returned id is not matching the one in the entity instance");
        assertEquals(String.class, entityInformation.getIdType(), "CouchDB is able to work with String id only");
    }

}