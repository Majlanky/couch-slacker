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
 * Class providing consumer functionality for two values
 *
 * @param <FirstParameterT>    Type of first parameter consumed
 * @param <TheOtherParameterT> Type of the other parameter consumed
 * @param <ExceptionT>         Type of {@link Exception} which can be thrown during consummation
 * @author Majlanky
 */
@FunctionalInterface
public interface ThrowingBiConsumer<FirstParameterT, TheOtherParameterT, ExceptionT extends Exception> {

    /**
     * Performs this operation on the given argument.
     *
     * @param first    parameter of consumer
     * @param theOther parameter of consumer
     * @throws ExceptionT if implementation inside throw exception
     */
    void accept(FirstParameterT first, TheOtherParameterT theOther) throws ExceptionT;

}
