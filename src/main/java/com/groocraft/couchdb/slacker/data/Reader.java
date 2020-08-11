package com.groocraft.couchdb.slacker.data;

/**
 * Interface providing reading functionality
 * @param <DataT> Type of return
 * @author Majlanky
 */
public interface Reader<DataT> {

    /**
     * Method which return data read from the given source.
     * @param o Object which from data should be read. The object can not be null.
     * @return read data
     */
    DataT read(Object o);

}
