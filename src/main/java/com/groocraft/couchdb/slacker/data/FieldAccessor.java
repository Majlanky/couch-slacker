package com.groocraft.couchdb.slacker.data;

import com.groocraft.couchdb.slacker.exception.AccessException;

import java.lang.reflect.Field;

/**
 * Class providing writing and reading functionality to any {@link Field}
 * @param <DataT> Type of accessed data
 * @author Majlanky
 */
public class FieldAccessor<DataT> implements Writer<DataT>, Reader<DataT> {

    private final Field field;

    public FieldAccessor(Field field) {
        this.field = field;
        this.field.setAccessible(true);
    }

    @Override
    @SuppressWarnings("unchecked")
    public DataT read(Object o) {
        try {
            return (DataT)field.get(o);
        } catch (IllegalAccessException e) {
            throw new AccessException(e);
        }
    }

    @Override
    public void write(Object o, DataT data) {
        try {
            field.set(o, data);
        } catch (IllegalAccessException e) {
            throw new AccessException(e);
        }
    }
}
