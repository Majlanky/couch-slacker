package com.groocraft.couchdb.slacker.data;

import com.groocraft.couchdb.slacker.exception.AccessException;
import lombok.AllArgsConstructor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Class providing reading functionality by calling a method.
 * @param <DataT> Type of return from a method
 * @author Majlanky
 */
public class MethodReader<DataT> implements Reader<DataT>{

    private final Method method;

    public MethodReader(Method method) {
        this.method = method;
    }

    @Override
    public DataT read(Object o) {
        try {
            return (DataT)method.invoke(o);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new AccessException(e);
        }
    }
}
