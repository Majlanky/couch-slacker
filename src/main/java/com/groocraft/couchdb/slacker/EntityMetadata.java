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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.groocraft.couchdb.slacker.annotation.Document;
import com.groocraft.couchdb.slacker.configuration.CouchDbProperties;
import com.groocraft.couchdb.slacker.data.FieldAccessor;
import com.groocraft.couchdb.slacker.data.MethodReader;
import com.groocraft.couchdb.slacker.data.MethodWriter;
import com.groocraft.couchdb.slacker.data.Reader;
import com.groocraft.couchdb.slacker.data.Writer;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Class providing access to attributes where to store id and revision, both mandatory for work with CouchDB documents.
 * Database name is parsed based on the value of {@link Document} annotation or lower cased simple name of passed class.
 * For parsing access to storage attributes {@link JsonProperty} annotation is used with _id and _rev values.
 * {@link EntityMetadata} can provide direct access to the field or setter and getter. Logic is to used method if possible.
 * If there is field with {@link JsonProperty} annotation, setters and getter for attribute are looked for (standard naming convention is used)
 * and used if present. If there are no setter/getters, field is accessed directly. If there are methods with {@link JsonProperty}
 * annotation, it is used in preference. Lookup is done in all ancestors of the the given class if in the actual class is no _id and _rev access.
 *
 * @author Majlanky
 */
@Slf4j
public class EntityMetadata {

    private final String databaseName;
    private final Writer<String> idWriter;
    private final Reader<String> idReader;
    private final Writer<String> revisionWriter;
    private final Reader<String> revisionReader;
    private final String design;
    private final String view;
    private final String type;
    private final String typeField;
    private final boolean isViewed;

    /**
     * @param descriptor of parsed document. Must not be {@literal null}
     * @throws IllegalStateException if there is missing _id or _rev access
     */
    public EntityMetadata(@NotNull DocumentDescriptor descriptor) {
        Assert.notNull(descriptor, "Descriptor must not be nul");
        databaseName = "".equals(descriptor.value()) ? descriptor.getEntityClass().getSimpleName().toLowerCase() : descriptor.value();
        log.debug("Database with name {} will be used for all documents of class {}", databaseName, descriptor.getEntityClass().getName());
        idWriter = getWriter(CouchDbProperties.COUCH_ID_NAME, descriptor.getEntityClass());
        idReader = getReader(CouchDbProperties.COUCH_ID_NAME, descriptor.getEntityClass());
        revisionWriter = getWriter(CouchDbProperties.COUCH_REVISION_NAME, descriptor.getEntityClass());
        revisionReader = getReader(CouchDbProperties.COUCH_REVISION_NAME, descriptor.getEntityClass());
        isViewed = descriptor.accessByView()
                || !Document.DEFAULT_DESIGN_NAME.equals(descriptor.design())
                || !Document.DEFAULT_TYPE_FIELD.equals(descriptor.typeField())
                || !"".equals(descriptor.view())
                || !"".equals(descriptor.type());
        design = descriptor.design();
        view = "".equals(descriptor.view()) ? descriptor.getEntityClass().getSimpleName().toLowerCase() : descriptor.view();
        type = "".equals(descriptor.type()) ? descriptor.getEntityClass().getSimpleName().toLowerCase() : descriptor.type();
        typeField = descriptor.typeField();
        if (isViewed) {
            log.debug("Documents of class {} will be processed by view ({}) and type ({}) where design is {} and typeField is {}",
                    descriptor.getEntityClass().getSimpleName(), view, type, design, typeField);
        } else {
            log.debug("Documents of class {} will be stored in exclusive database", descriptor.getEntityClass().getSimpleName());
        }
    }

    /**
     * Method to find out the best access for writing data into an instance. The access by setter is preferred. It can be the easiest way, when the setter is
     * annotated by {@link JsonProperty} with value set to the {@code annotationValue}. The annotated method is used for writing. The other way is when an
     * attribute is annotated by {@link JsonProperty} and a setter method is present but unannotated. The setter method is found based on the attribute name
     * and used. The last but not least way is direct access to the attribute annotated by {@link JsonProperty}
     *
     * @param annotationValue value which looked for in all attributes and methods annotated by {@link JsonProperty} in the given {@code clazz}. Must not be {@literal null}
     * @param clazz           which is processed. Must not be {@literal null}
     * @return {@link Writer} implementation based on the rules described above.
     * @throws IllegalStateException if there is no field nor method
     * @see Method
     * @see Field
     */
    private Writer<String> getWriter(String annotationValue, Class<?> clazz) {
        return getAccess(Collections.singletonList("set"), annotationValue, clazz, MethodWriter::new, FieldAccessor::new);
    }

    /**
     * Method to find out the best access for reading data from an instance. The access by getter is preferred. It can be the easiest way, when the getter is
     * annotated by {@link JsonProperty} with value set to the {@code annotationValue}. The annotated method is used for reading. The other way is when an
     * attribute is annotated by {@link JsonProperty} and a getter method is present but unannotated. The getter method is found based on the attribute name
     * and used. The last but not least way is direct access to the attribute annotated by {@link JsonProperty}
     *
     * @param annotationValue value which looked for in all attributes and methods annotated by {@link JsonProperty} in the given {@code clazz}. Must not be {@literal null}
     * @param clazz           which is processed. Must not be {@literal null}
     * @return {@link Writer} implementation based on the rules described above
     * @throws IllegalStateException if there is no field nor method
     * @see Method
     * @see Field
     */
    private Reader<String> getReader(String annotationValue, Class<?> clazz) {
        return getAccess(Arrays.asList("get", "is"), annotationValue, clazz, MethodReader::new, FieldAccessor::new);
    }

    /**
     * Method to find out the best access for accessing (reading or writing, depends on {@code type}) data of an instance. The access by getter/setter is
     * preferred. It can be the easiest way, when the getter/setter is annotated by {@link JsonProperty} with value set to the {@code annotationValue}. The
     * annotated method is used for access. The other way is when an attribute is annotated by {@link JsonProperty} and a getter/setter method is present but
     * unannotated. The getter/setter method is found based on the attribute name (and {@code type}) and used. The last but not least way is direct access to
     * the attribute annotated by {@link JsonProperty}
     *
     * @param types                possible prefix of getter/setter method of attribute. Based on java naming convention it can be set/get/is. Must not be
     *                             {@literal null}
     * @param annotationValue      value which looked for in all attributes and methods annotated by {@link JsonProperty} in the given {@code clazz}. Must not be
     *                             {@literal null}
     * @param clazz                which is processed. Must not be {@literal null}
     * @param methodAccessProducer if setter/getter method is found (as described above), the producer is called with the method to get reader/writer. Must not be {@literal null}
     * @param fieldAccessProducer  if there is no setter/getter method, only field found, the producer is called with the field to get reader/writer. Must not be {@literal null}
     * @param <AccessT>            Type of reader/writer expected as return
     * @return Reader/writer created with one of {@code methodAccessProducer} or {@code fieldAccessProducer}
     * @throws IllegalStateException if there is no field nor method
     */
    private <AccessT> AccessT getAccess(List<String> types, String annotationValue, Class<?> clazz, Function<Method, AccessT> methodAccessProducer,
                                        Function<Field, AccessT> fieldAccessProducer) {
        Optional<AccessT> access = Optional.ofNullable(getAnnotatedMethod(types, annotationValue, clazz)).map(methodAccessProducer);
        if (!access.isPresent()) {
            log.debug("CLass {} does not contain JsonProperty annotated methods for {}, trying find field", clazz.getName(), annotationValue);
            Optional<Field> field = Optional.ofNullable(resolveAnnotatedField(annotationValue, clazz));
            access = field.map(fieldAccessProducer);
            field.ifPresent(f -> log.debug("JsonProperty annotated field for {} found, trying find methods to accessing it", annotationValue));
            Optional<AccessT> methodReader = field.map(f -> getMethodForField(types, f, clazz)).map(methodAccessProducer);
            if (methodReader.isPresent()) {
                log.debug("Methods for accessing JsonProperty annotated field for {} found and will be used to getting value", annotationValue);
                access = methodReader;
            }
        }
        return access.orElseThrow(() -> new IllegalStateException("Class " + clazz.getName() + " does not contain " + annotationValue + " " + String.join(" or ", types) + " access"));
    }

    /**
     * Method to discover field annotated with {@link JsonProperty} with the given {@code annotationValue}. Lookup is done in all ancestors of the the given
     * class if in the actual class is no field with {@link JsonProperty} annotation with the given {@code annotationValue}.
     *
     * @param annotationValue value which looked for in all attributes annotations of {@link JsonProperty} in the given {@code clazz}. Must not be {@literal
     *                        null}
     * @param clazz           which is processed. Must not be {@literal null}
     * @return {@link Field} which is annotated by {@link JsonProperty} if any, null otherwise
     */
    private Field resolveAnnotatedField(String annotationValue, Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            JsonProperty jsonProperty = field.getAnnotation(JsonProperty.class);
            if (jsonProperty != null && annotationValue.equals(jsonProperty.value())) {
                return field;
            }
        }
        return clazz.getSuperclass() == null ? null : resolveAnnotatedField(annotationValue, clazz.getSuperclass());
    }

    /**
     * Method to discover method based on the field name and given prefixes. Lookup is done in all ancestors of the the given class if in the actual class is
     * no method of proper name.
     *
     * @param prefixes possible prefixes of getter/setter method of attribute. Based on java naming convention it can be set/get/is. Must not be {@literal null}
     * @param field    which name is used to get name of method (with connection of one of {@code prefixes}). Must not be {@literal null}
     * @param clazz    which is processed. Must not be {@literal null}
     * @return {@link Method} for accessing field if any
     */
    private Method getMethodForField(List<String> prefixes, Field field, Class<?> clazz) {
        String nameFragment = field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
        Optional<Method> method =
                Arrays.stream(clazz.getDeclaredMethods()).filter(m -> prefixes.stream().anyMatch(p -> (p + nameFragment).equals(m.getName()))).findFirst();
        return method.orElseGet(() -> clazz.getSuperclass() == null ? null : getMethodForField(prefixes, field, clazz.getSuperclass()));
    }

    /**
     * Method to discover method annotated with {@link JsonProperty} with the given {@code annotationValue}. Lookup is done in all ancestors of the the given
     * class if in the actual class is no method with {@link JsonProperty} annotation with the given {@code annotationValue}.
     *
     * @param annotationValue value which looked for in all methods annotations of {@link JsonProperty} in the given {@code clazz}. Must not be {@literal
     *                        null}
     * @param clazz           which is processed. Must not be {@literal null}
     * @return {@link Method} which is annotated by {@link JsonProperty} if any, null otherwise
     */
    private Method getAnnotatedMethod(List<String> prefixes, String annotationValue, Class<?> clazz) {
        for (Method method : clazz.getDeclaredMethods()) {
            JsonProperty jsonProperty = method.getAnnotation(JsonProperty.class);
            if (jsonProperty != null && annotationValue.equals(jsonProperty.value()) && prefixes.stream().anyMatch(p -> method.getName().startsWith(p))) {
                return method;
            }
        }
        return clazz.getSuperclass() == null ? null : getAnnotatedMethod(prefixes, annotationValue, clazz.getSuperclass());
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public Writer<String> getIdWriter() {
        return idWriter;
    }

    public Reader<String> getIdReader() {
        return idReader;
    }

    public Writer<String> getRevisionWriter() {
        return revisionWriter;
    }

    public Reader<String> getRevisionReader() {
        return revisionReader;
    }

    /**
     * Method which informs about access type for the instances of the metadata class.
     *
     * @return true if {@link Document} annotation has set {@link Document#accessByView()} to true, or one of the following attributes return non-default
     * value: {@link Document#design()}, {@link Document#view()}, {@link Document#typeField()}, {@link Document#type()}
     */
    public boolean isViewed() {
        return isViewed;
    }

    public String getDesign() {
        return design;
    }

    public String getView() {
        return view;
    }

    public String getType() {
        return type;
    }

    public String getTypeField() {
        return typeField;
    }
}
