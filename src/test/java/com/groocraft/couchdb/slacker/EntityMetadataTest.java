package com.groocraft.couchdb.slacker;

import com.groocraft.couchdb.slacker.annotation.Document;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntityMetadataTest {

    @Test
    void testParsingFieldBased() {
        FieldTestDocument testDocument = new FieldTestDocument();
        EntityMetadata entityMetadata = new EntityMetadata(FieldTestDocument.class);
        assertFalse(entityMetadata.isViewed(), "FieldTestDocument is not annotated as view accessed document");
        assertEquals("test", entityMetadata.getDatabaseName(), "Wrongly parsed Database annotation");

        entityMetadata.getIdWriter().write(testDocument, "idTest");
        assertEquals("idTest", testDocument.a, "Id field writer is not properly parsed");
        assertEquals("idTest", entityMetadata.getIdReader().read(testDocument), "Id field reader is not properly parsed");

        entityMetadata.getRevisionWriter().write(testDocument, "revisionTest");
        assertEquals("revisionTest", testDocument.b, "Revision field writer is not properly parsed");
        assertEquals("revisionTest", entityMetadata.getRevisionReader().read(testDocument), "Revision field reader is not properly parsed");
    }

    @Test
    void testParsingMethodBased() {
        MethodTestDocument testDocument = new MethodTestDocument();
        EntityMetadata entityMetadata = new EntityMetadata(MethodTestDocument.class);
        assertFalse(entityMetadata.isViewed(), "MethodTestDocument is not annotated as view accessed document");
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
    void testParsingFieldWithMethodBased() {
        FieldWithMethodTestDocument testDocument = new FieldWithMethodTestDocument();
        EntityMetadata entityMetadata = new EntityMetadata(FieldWithMethodTestDocument.class);
        assertFalse(entityMetadata.isViewed(), "FieldWithMethodTestDocument is not annotated as view accessed document");
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
    void testParsingMixedBased() {
        MixedTestDocument testDocument = new MixedTestDocument();
        EntityMetadata entityMetadata = new EntityMetadata(MixedTestDocument.class);
        assertFalse(entityMetadata.isViewed(), "MixedTestDocument is not annotated as view accessed document");
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
    void testParsingInheritance() {
        InheritedTestDocument testDocument = new InheritedTestDocument();
        EntityMetadata entityMetadata = new EntityMetadata(InheritedTestDocument.class);
        assertFalse(entityMetadata.isViewed(), "InheritedTestDocument is not annotated as view accessed document");
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
    void testMissingId() {
        assertThrows(IllegalStateException.class, () -> new EntityMetadata(NoIdTestDocument.class), "Absence of _id attribute must be reported by exception");
    }

    @Test()
    void testMissingRevision() {
        assertThrows(IllegalStateException.class, () -> new EntityMetadata(NoRevisionTestDocument.class), "Absence of _rev attribute must be reported by " +
                "exception");
    }

    @Test
    void testMissingDatabase() {
        assertThrows(IllegalArgumentException.class, () -> new EntityMetadata(NoDatabaseTestDocument.class));
    }

    @Test
    void testDefaultViewed() {
        EntityMetadata em = new EntityMetadata(DefaultViewedDocument.class);
        assertTrue(em.isViewed(), "DefaultViewedDocument is annotated as view accessed document");
        assertEquals(Document.DEFAULT_DESIGN_NAME, em.getDesign(), "");
        assertEquals(Document.DEFAULT_TYPE_FIELD, em.getTypeField(), "");
        assertEquals("defaultvieweddocument", em.getType(), "");
        assertEquals("defaultvieweddocument", em.getView(), "");
    }

    @Test
    void testViewed() {
        EntityMetadata em = new EntityMetadata(ViewedDocument.class);
        assertTrue(em.isViewed(), "ViewedDocument is annotated as view accessed document");
        assertEquals(Document.DEFAULT_DESIGN_NAME, em.getDesign(), "");
        assertEquals(Document.DEFAULT_TYPE_FIELD, em.getTypeField(), "");
        assertEquals("entity", em.getType(), "");
        assertEquals("entity", em.getView(), "");
    }

}