package com.groocraft.couchdb.slacker.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify a database name for entity. If the annotation is not present for entity, lower-cased {@link Class#getSimpleName()} is used.
 *
 * @author Majlanky
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Database {

    String value() default "";

}
