package com.tangwangwang.spring.data.ultipa.core.mapping.model;

import com.tangwangwang.spring.data.ultipa.annotation.PropertyType;
import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Ultipa persists schema entities
 *
 * @author Wangwang Tang
 * @since 1.0
 */
public class UltipaSchema {

    private static final String DEFAULT_DESCRIPTION = "";

    /**
     * Name of schema
     */
    private final String name;

    /**
     * Description of schema
     */

    private final String description;

    /**
     * All properties of the schema
     */
    private final Map<String, UltipaProperty> properties = new HashMap<>();

    UltipaSchema(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public static UltipaSchema of(String name) {
        return new UltipaSchema(name, DEFAULT_DESCRIPTION);
    }

    public static UltipaSchema of(String name, String description) {
        return new UltipaSchema(name, description);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public UltipaProperty addProperty(String name, PropertyType type, String description) {
        UltipaProperty property = new UltipaProperty(this, name, type, description);
        properties.put(name, property);
        return property;
    }

    @Nullable
    public UltipaProperty getProperty(String name) {
        return properties.get(name);
    }
}
