package com.groocraft.couchdb.slacker.data;

/**
 * Interface providing writing functionality
 * @param <DataT> Type of the written data
 * @author Majlanky
 */
public interface Writer<DataT> {

    /**
     * Method which writes data to the given destination.
     * @param o Object which data should be written to. The object can not be null.
     * @param data Data which will be written in to the given destination
     */
    void write(Object o, DataT data);

}
