package com.tangwangwang.spring.data.ultipa.core.convert;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tangwangwang.spring.data.ultipa.annotation.CascadeType;
import com.tangwangwang.spring.data.ultipa.core.mapping.UltipaPersistentEntity;
import com.tangwangwang.spring.data.ultipa.core.mapping.UltipaPersistentProperty;
import com.tangwangwang.spring.data.ultipa.core.schema.PersistSchema;
import com.tangwangwang.spring.data.ultipa.core.schema.Schema;
import org.springframework.data.convert.EntityConverter;
import org.springframework.data.convert.EntityReader;
import org.springframework.data.convert.EntityWriter;

import java.util.List;
import java.util.Map;

/**
 * Central Ultipa specific converter interface which combines {@link EntityWriter} and {@link EntityReader}.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
public interface UltipaConverter extends EntityConverter<UltipaPersistentEntity<?>, UltipaPersistentProperty, Object, Schema> {
    /**
     * Returns {@link ObjectMapper} for serializes the object.
     */
    ObjectMapper getObjectMapper();

    /**
     * Read the Map that contains key-value pairs.
     */
    Map<String, Object> readMap(Schema source);

    /**
     * Read the List that contains elements.
     */
    List<Object> readArray(Schema source);

    /**
     * Write properties from the object to the schema
     *
     * @param source An object that provides properties
     * @param sink   The schema of the received properties
     */
    void write(Object source, Schema sink);

    /**
     * Write cascading relationships from the object to the schema
     *
     * @param source An object that provides cascading relationships
     * @param sink   The schema of the received cascading relationships
     */
    void write(Object source, PersistSchema sink, CascadeType cascade);
}
