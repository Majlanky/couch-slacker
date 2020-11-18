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

package com.groocraft.couchdb.slacker.exception;

import org.jetbrains.annotations.NotNull;

/**
 * Exception thrown when a rule of schema processing is not fulfilled
 *
 * @author Majlanky
 */
public class SchemaProcessingException extends Exception{

    public SchemaProcessingException(@NotNull String message) {
        super(message);
    }

}
