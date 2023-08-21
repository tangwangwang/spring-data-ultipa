package org.springframework.data.ultipa.core.mapping.model;

import org.springframework.data.ultipa.annotation.PropertyType;

/**
 * Ultipa persists property entities
 *
 * @author Wangwang Tang
 * @since 1.0
 */
public class UltipaProperty {

    /**
     * Schema of schema property
     */
    private final UltipaSchema schema;

    /**
     * Name of schema property
     */
    private final String name;

    /**
     * Type of schema property
     */
    private final PropertyType type;

    /**
     * Description of schema
     */
    private final String description;

    UltipaProperty(UltipaSchema schema, String name, PropertyType type, String description) {
        this.schema = schema;
        this.name = name;
        this.type = type;
        this.description = description;
    }

    public UltipaSchema getSchema() {
        return schema;
    }

    public String getName() {
        return name;
    }

    public PropertyType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }
}
