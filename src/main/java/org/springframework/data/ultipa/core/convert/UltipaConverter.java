package org.springframework.data.ultipa.core.convert;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.convert.EntityConverter;
import org.springframework.data.convert.EntityReader;
import org.springframework.data.convert.EntityWriter;
import org.springframework.data.ultipa.core.mapping.UltipaPersistentEntity;
import org.springframework.data.ultipa.core.mapping.UltipaPersistentProperty;
import org.springframework.data.ultipa.core.schema.Schema;

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
}
