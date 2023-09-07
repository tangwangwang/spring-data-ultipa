package com.tangwangwang.spring.data.ultipa.core.mapping.model;

import com.tangwangwang.spring.data.ultipa.annotation.PropertyType;

import java.util.HashMap;

/**
 * Ultipa persists schema entities
 *
 * @author Wangwang Tang
 * @since 1.0
 */
public class UltipaSchema extends HashMap<String, UltipaProperty> {

    private static final String DEFAULT_DESCRIPTION = "";

    /**
     * Name of schema
     */
    private final String name;

    /**
     * Description of schema
     */

    private final String description;

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

    public UltipaProperty addProperty(String name, PropertyType type) {
        return addProperty(name, type, DEFAULT_DESCRIPTION);
    }

    public UltipaProperty addProperty(String name, PropertyType type, String description) {
        UltipaProperty property = new UltipaProperty(this, name, type, description);
        this.put(name, property);
        return property;
    }
}
