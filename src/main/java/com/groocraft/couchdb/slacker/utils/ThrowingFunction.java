/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.groocraft.couchdb.slacker.utils;

/**
 * {@link FunctionalInterface} which is providing ability to pass exceptional state from {@link #apply(Object)} method. It can be handful when we need to
 * pass some logic into method which is already prepared to handle exceptional state of the same type and avoid to using {@link RuntimeException}
 *
 * @param <ParameterT> type of parameter passed into the function
 * @param <ReturnedT>  type of return from the function
 * @param <ExceptionT> type of {@link Exception} which can be thrown from the function
 * @author Majlanky
 */
@FunctionalInterface
public interface ThrowingFunction<ParameterT, ReturnedT, ExceptionT extends Exception> {

    /**
     * Applies this function to the given argument.
     *
     * @param parameter the function argument
     * @return the function result
     * @throws ExceptionT if internal implementation throws a {@link Exception}
     */
    ReturnedT apply(ParameterT parameter) throws ExceptionT;

}
