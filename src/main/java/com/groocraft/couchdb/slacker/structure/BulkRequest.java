package com.groocraft.couchdb.slacker.structure;

@SuppressWarnings("unused")
public class BulkRequest<EntityT> {

    private Iterable<EntityT> docs;

    public BulkRequest(Iterable<EntityT> docs) {
        this.docs = docs;
    }

    public Iterable<EntityT> getDocs() {
        return docs;
    }

    public void setDocs(Iterable<EntityT> docs) {
        this.docs = docs;
    }
}
