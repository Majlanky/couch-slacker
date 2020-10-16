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

package com.groocraft.couchdb.slacker.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify a document context (name of database). Annotation must be present for all entities. This annotation designated if standard access
 * (i.e. one entity per database) or view access is used. View access is based on an idea, that there are more entities in one database (e.g. database animal
 * contains dog, cat, birds and etc.). The other assumption is that every entity has kind of compound key made from _id and another field
 * which name is set by {@link #typeField()}, it means value of the field is condition used in mapping function of view which is used as access to entities of
 * the same type. More information visit https://github.com/Majlanky/couch-slacker/wiki/CouchDB-layout-with-Couch-Slacker
 *
 * @author Majlanky
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Document {

    String DEFAULT_TYPE_FIELD = "type";
    String DEFAULT_DESIGN_NAME = "byType";

    /**
     * @return Name of documents database
     */
    @AliasFor("database")
    String value() default "";

    /**
     * @return Name of documents database
     */
    @AliasFor("value")
    String database() default "";

    /**
     * Attributes turns on/off "view access" during work with instances of the annotated class. Because all "view access" attributes has default value (to
     * ease the usage of the annotation), this attribute is needed as recognition. If at least one of the following attributes {@link Document#design()},
     * {@link Document#view()}, {@link Document#typeField()}, {@link Document#type()} contains non-default value, the value this of this attribute is ignored
     * and considered as true (again to ease the usage of the annotation)
     * @return false by default
     */
    boolean accessByView() default false;

    /**
     * If "view access" should be used during work with instances od annotated class, this attribute returns the name of a design in which is stored the view.
     * @return {@link #DEFAULT_DESIGN_NAME} by default
     */
    String design() default DEFAULT_DESIGN_NAME;

    /**
     * If "view access" should be used during work with instances od annotated class, this attribute returns the name of view from the configured
     * {@link #design()}. If the returned value is empty {@link String}, lower-cased name of class is used as name of the view.
     * @return empty {@link String} by default.
     */
    String view() default "";

    /**
     * If "view access" should be used during work with instances od annotated class, this attribute returns the name of documents field which is used as
     * part of compound key.
     * @return {@link #DEFAULT_TYPE_FIELD} as default.
     * @see Document
     */
    String typeField() default DEFAULT_TYPE_FIELD;

    /**
     * If "view access" should be used during work with instances od annotated class, this attribute returns the value of {@link #typeField()} which has all
     * instance of the annotated class. If the returned value is empty {@link String}, lower-cased name of class is used as name of the view.
     * @return empty {@link String} by default.
     */
    String type() default "";

}
