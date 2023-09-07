package com.tangwangwang.spring.data.ultipa.core.mapping.model;

import java.util.Objects;

/**
 * Ultipa System reserved properties.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
public enum UltipaSystemProperty {

    ID("_id"),
    UUID("_uuid"),
    FROM("_from"),
    TO("_to"),
    FROM_UUID("_from_uuid"),
    TO_UUID("_to_uuid"),
    ;
    /**
     * Represents a null value for a property
     */
    public static final String NULL_VALUE = "null";

    private final String mappedName;

    UltipaSystemProperty(String mappedName) {
        this.mappedName = mappedName;
    }

    public String getMappedName() {
        return mappedName;
    }

    /**
     * Whether the property is reserved for the system
     *
     * @param propertyName The name of the persisted property
     * @return System properties return true, otherwise return false
     */
    public static boolean isSystemProperty(String propertyName) {
        for (UltipaSystemProperty property : values()) {
            if (Objects.equals(property.getMappedName(), propertyName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Resolves a mapped name to an ultipa system property
     *
     * @param mappedName The name of mapped property
     * @return The system property of ultipa
     * @throws IllegalArgumentException When resolving a mapped name that is not a system property
     */
    public static UltipaSystemProperty resolve(String mappedName) {
        for (UltipaSystemProperty type : values()) {
            if (Objects.equals(type.mappedName, mappedName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No matching constant for [" + mappedName + "]");
    }

    /**
     * Whether it is an identifier
     *
     * @return The identifier returns true, otherwise it returns false
     */
    public boolean isUniqueIdentifier() {
        return this == ID || this == UUID;
    }
}
