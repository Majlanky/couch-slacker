package com.groocraft.couchdb.slacker.data;

import com.groocraft.couchdb.slacker.exception.AccessException;
import lombok.AllArgsConstructor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Class providing writing functionality by calling a method.
 * @param <DataT> Type of method parameter
 * @author Majlanky
 */
public class MethodWriter<DataT> implements Writer<DataT> {

    private final Method method;

    public MethodWriter(Method method) {
        this.method = method;
    }

    @Override
    public void write(Object o, DataT data) {
        try {
            method.invoke(o, data);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new AccessException(e);
        }
    }
}
