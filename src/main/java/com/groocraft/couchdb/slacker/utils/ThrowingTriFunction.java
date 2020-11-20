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

import java.util.function.Function;

/**
 * Represents a function that accepts two arguments and produces a result or throws an exception.
 * This is the tri-arity specialization of {@link Function}.
 *
 * @param <FirstT>     the type of the first argument to the function
 * @param <SecondT>    the type of the second argument to the function
 * @param <ThirdT>     the type of the third argument to the function
 * @param <ReturnT>    the type of the result of the function
 * @param <ExceptionT> the type of possible exception
 * @author Majlanky
 */
@FunctionalInterface
public interface ThrowingTriFunction<FirstT, SecondT, ThirdT, ReturnT, ExceptionT extends Exception> {

    /**
     * Applies this function to the given arguments.
     *
     * @param first  the first function argument
     * @param second the second function argument
     * @param third  the third function argument
     * @return the function result
     * @throws ExceptionT if any problem during processing
     */
    ReturnT apply(FirstT first, SecondT second, ThirdT third) throws ExceptionT;

}
