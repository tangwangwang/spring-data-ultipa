package com.tangwangwang.spring.data.ultipa.annotation;

import com.ultipa.Ultipa;

import java.util.Objects;

/**
 * Enumeration of property value types that can be used to represent a {@link Ultipa.PropertyType} property value.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
public enum PropertyType {

    /**
     * Auto type that is derived from the property value.
     */
    AUTO(""),
    STRING("string"),
    TEXT("text"),
    INT32("int32"),
    INT64("int64"),
    UINT32("uint32"),
    UINT64("uint64"),
    FLOAT("float"),
    DOUBLE("double"),
    DATETIME("datetime"),
    TIMESTAMP("timestamp"),
    POINT("point"),

    // Array type

    STRING_ARRAY("string[]"),
    TEXT_ARRAY("text[]"),
    INT32_ARRAY("int32[]"),
    INT64_ARRAY("int64[]"),
    UINT32_ARRAY("uint32[]"),
    UINT64_ARRAY("uint64[]"),
    FLOAT_ARRAY("float[]"),
    DOUBLE_ARRAY("double[]"),
    DATETIME_ARRAY("datetime[]"),
    TIMESTAMP_ARRAY("timestamp[]"),
    ;

    private final String mappedName;

    PropertyType(String mappedName) {
        this.mappedName = mappedName;
    }

    /**
     * Returns the mapped name used to represent the type.
     *
     * @return the mapped name used to represent the type.
     */
    public String getMappedName() {
        return mappedName;
    }

    public static PropertyType resolve(String mappedName) {
        for (PropertyType type : values()) {
            if (Objects.equals(type.mappedName, mappedName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No matching constant for [" + mappedName + "]");
    }

}
