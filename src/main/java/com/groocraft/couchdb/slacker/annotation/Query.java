package com.groocraft.couchdb.slacker.annotation;

import org.springframework.data.annotation.QueryAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify query for query method in repository.
 * @author Majlanky
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@QueryAnnotation
public @interface Query {

    String value() default "";

}
