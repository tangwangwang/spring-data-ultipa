package org.springframework.data.ultipa.core.mapping.model;

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
}
