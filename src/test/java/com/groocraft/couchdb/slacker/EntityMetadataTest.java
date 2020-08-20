package com.groocraft.couchdb.slacker;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EntityMetadataTest {

    @Test
    public void testParsingFieldBased(){
        FieldTestDocument testDocument = new FieldTestDocument();
        EntityMetadata<FieldTestDocument> entityMetadata = new EntityMetadata<>(FieldTestDocument.class);
        assertEquals("test", entityMetadata.getDatabaseName(), "Wrongly parsed Database annotation");

        entityMetadata.getIdWriter().write(testDocument, "idTest");
        assertEquals("idTest", testDocument.a, "Id field writer is not properly parsed");
        assertEquals("idTest", entityMetadata.getIdReader().read(testDocument), "Id field reader is not properly parsed");

        entityMetadata.getRevisionWriter().write(testDocument, "revisionTest");
        assertEquals("revisionTest", testDocument.b, "Revision field writer is not properly parsed");
        assertEquals("revisionTest", entityMetadata.getRevisionReader().read(testDocument), "Revision field reader is not properly parsed");
    }

    @Test
    public void testParsingMethodBased(){
        MethodTestDocument testDocument = new MethodTestDocument();
        EntityMetadata<MethodTestDocument> entityMetadata = new EntityMetadata<>(MethodTestDocument.class);
        assertEquals("test", entityMetadata.getDatabaseName(), "Wrongly parsed Database annotation");

        entityMetadata.getIdWriter().write(testDocument, "idTest");
        assertTrue(testDocument.aCalled, "Id setter not used");
        assertEquals("idTest", testDocument.a, "Id method writer is not properly parsed");
        testDocument.aCalled = false;
        assertEquals("idTest", entityMetadata.getIdReader().read(testDocument), "Id method reader is not properly parsed");
        assertTrue(testDocument.aCalled, "Id getter not used ");

        entityMetadata.getRevisionWriter().write(testDocument, "revisionTest");
        assertTrue(testDocument.bCalled, "Revision setter not used");
        assertEquals("revisionTest", testDocument.b, "Revision method writer is not properly parsed");
        testDocument.bCalled = false;
        assertEquals("revisionTest", entityMetadata.getRevisionReader().read(testDocument), "Revision method reader is not properly parsed");
        assertTrue(testDocument.bCalled, "Revision getter not used");
    }

    @Test
    public void testParsingFieldWithMethodBased(){
        FieldWithMethodTestDocument testDocument = new FieldWithMethodTestDocument();
        EntityMetadata<FieldWithMethodTestDocument> entityMetadata = new EntityMetadata<>(FieldWithMethodTestDocument.class);
        assertEquals("test", entityMetadata.getDatabaseName(), "Wrongly parsed Database annotation");

        entityMetadata.getIdWriter().write(testDocument, "idTest");
        assertTrue(testDocument.aCalled, "Id setter not used");
        assertEquals("idTest", testDocument.a, "Id method writer is not properly parsed");
        testDocument.aCalled = false;
        assertEquals("idTest", entityMetadata.getIdReader().read(testDocument), "Id method reader is not properly parsed");
        assertTrue(testDocument.aCalled, "Id getter not used ");

        entityMetadata.getRevisionWriter().write(testDocument, "revisionTest");
        assertTrue(testDocument.bCalled, "Revision setter not used");
        assertEquals("revisionTest", testDocument.b, "Revision method writer is not properly parsed");
        testDocument.bCalled = false;
        assertEquals("revisionTest", entityMetadata.getRevisionReader().read(testDocument), "Revision method reader is not properly parsed");
        assertTrue(testDocument.bCalled, "Revision getter not used");
    }

    @Test
    public void testParsingMixedBased(){
        MixedTestDocument testDocument = new MixedTestDocument();
        EntityMetadata<MixedTestDocument> entityMetadata = new EntityMetadata<>(MixedTestDocument.class);
        assertEquals("test", entityMetadata.getDatabaseName(), "Wrongly parsed Database annotation");

        entityMetadata.getIdWriter().write(testDocument, "idTest");
        assertFalse(testDocument.aCalled, "Getter called instead of setter");
        assertEquals("idTest", testDocument.a, "Id method writer is not properly parsed");
        assertEquals("idTest", entityMetadata.getIdReader().read(testDocument), "Id method reader is not properly parsed");
        assertTrue(testDocument.aCalled, "Id getter not used ");

        entityMetadata.getRevisionWriter().write(testDocument, "revisionTest");
        assertTrue(testDocument.bCalled, "Revision setter not used");
        assertEquals("revisionTest", testDocument.b, "Revision method writer is not properly parsed");
        testDocument.bCalled = false;
        assertEquals("revisionTest", entityMetadata.getRevisionReader().read(testDocument), "Revision method reader is not properly parsed");
        assertFalse(testDocument.bCalled, "Revision setter called instead of getter");
    }

    @Test
    public void testParsingInheritance(){
        InheritedTestDocument testDocument = new InheritedTestDocument();
        EntityMetadata<InheritedTestDocument> entityMetadata = new EntityMetadata<>(InheritedTestDocument.class);
        assertEquals("test", entityMetadata.getDatabaseName(), "Wrongly parsed Database annotation");

        entityMetadata.getIdWriter().write(testDocument, "idTest");
        assertTrue(testDocument.aCalled, "Id setter not used");
        assertEquals("idTest", testDocument.a, "Id method writer is not properly parsed");
        assertEquals("idTest", entityMetadata.getIdReader().read(testDocument), "Id method reader is not properly parsed");
        assertTrue(testDocument.aCalled, "Id getter not used ");

        entityMetadata.getRevisionWriter().write(testDocument, "revisionTest");
        assertTrue(testDocument.bCalled, "Revision setter not used");
        assertEquals("revisionTest", testDocument.b, "Revision method writer is not properly parsed");
        testDocument.bCalled = false;
        assertEquals("revisionTest", entityMetadata.getRevisionReader().read(testDocument), "Revision method reader is not properly parsed");
        assertFalse(testDocument.bCalled, "Revision setter called instead of getter");
    }

    @Test()
    public void testMissingId(){
        assertThrows(IllegalStateException.class, () -> new EntityMetadata<>(NoIdTestDocument.class), "Absence of _id attribute must be reported by exception");
    }

    @Test()
    public void testMissingRevision(){
        assertThrows(IllegalStateException.class, () -> new EntityMetadata<>(NoRevisionTestDocument.class), "Absence of _rev attribute must be reported by " +
                "exception");
    }

    @Test
    public void testMissingDatabase(){
        assertDoesNotThrow(() -> {
            NoDatabaseTestDocument testDocument = new NoDatabaseTestDocument();
            EntityMetadata<NoDatabaseTestDocument> entityMetadata = new EntityMetadata<>(NoDatabaseTestDocument.class);
            assertEquals("nodatabasetestdocument", entityMetadata.getDatabaseName(), "Wrongly created database name based on class name");

            entityMetadata.getIdWriter().write(testDocument, "idTest");
            assertEquals("idTest", testDocument.a, "Id field writer is not properly parsed");
            assertEquals("idTest", entityMetadata.getIdReader().read(testDocument), "Id field reader is not properly parsed");

            entityMetadata.getRevisionWriter().write(testDocument, "revisionTest");
            assertEquals("revisionTest", testDocument.b, "Revision field writer is not properly parsed");
            assertEquals("revisionTest", entityMetadata.getRevisionReader().read(testDocument), "Revision field reader is not properly parsed");
        });
    }

}