/*
 * Copyright 2020-2022 the original author or authors.
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

package com.groocraft.couchdb.slacker;

/**
 * Enum of strategies how to do queries.
 */
public enum QueryStrategy {

    /**
     * This way all query methods are translated to mango queries and executed thru _find endpoint.
     */
    MANGO,

    /**
     * This way all query methods are translated to javascript conditions to create a matching mapping function. If query result should be sorted, the key is
     * created for emitted data. Performance of querying this way is much better than indexed mango but it can be really tricky because of possibly high
     * number of views and high resources to keep the alive.
     */
    VIEW

}
