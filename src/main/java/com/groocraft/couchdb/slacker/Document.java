package com.groocraft.couchdb.slacker;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Ancestor of all CouchDB document pojo classes. There is not need to use this class as an ancestor for every pojo class, but it is more easier than define
 * {@code _id} and {@code _rev} to every pojo class.
 *
 * @author Majlanky
 */
public class Document {

    @JsonProperty("_id")
    @JsonInclude(Include.NON_NULL)
    private String id;

    @JsonProperty("_rev")
    @JsonInclude(Include.NON_NULL)
    private String revision;

    public Document() {
    }

    public Document(String id, String revision) {
        this.id = id;
        this.revision = revision;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRevision() {
        return revision;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Document document = (Document) o;
        return Objects.equals(id, document.id) &&
                Objects.equals(revision, document.revision);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, revision);
    }
}
