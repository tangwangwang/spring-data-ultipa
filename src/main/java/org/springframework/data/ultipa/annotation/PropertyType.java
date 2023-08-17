package org.springframework.data.ultipa.annotation;

import com.ultipa.Ultipa;
import com.ultipa.sdk.data.Point;
import org.springframework.data.ultipa.core.mapping.UltipaPersistentProperty;
import org.springframework.util.ClassUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Date;

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

    /**
     * Resolves the default ultipa property type according to the given ultipa persistent property
     *
     * @param property the given ultipa persistent property
     * @return The ultipa property type
     */
    public static PropertyType resolverAutoJavaType(UltipaPersistentProperty property) {
        if (property.isJson()) {
            return TEXT;
        }
        boolean isArray = property.isCollectionLike();
        Class<?> actualType = ClassUtils.resolvePrimitiveIfNecessary(property.getActualType());

        if (actualType == String.class) {
            return isArray ? STRING_ARRAY : STRING;
        }
        if (Arrays.asList(Boolean.class, Byte.class, Short.class, Integer.class).contains(actualType)) {
            return isArray ? INT32_ARRAY : INT32;
        }
        if (actualType == Long.class) {
            return isArray ? INT64_ARRAY : INT64;
        }
        if (actualType == BigInteger.class) {
            return isArray ? UINT64_ARRAY : UINT64;
        }
        if (actualType == Double.class || actualType == BigDecimal.class) {
            return isArray ? DOUBLE_ARRAY : DOUBLE;
        }
        if (actualType == Float.class) {
            return isArray ? FLOAT_ARRAY : FLOAT;
        }
        if (actualType == Timestamp.class || actualType == Instant.class) {
            return isArray ? TIMESTAMP_ARRAY : TIMESTAMP;
        }
        if (Arrays.asList(Date.class, LocalDate.class, LocalTime.class, LocalDateTime.class).contains(actualType)) {
            return isArray ? DATETIME_ARRAY : DATETIME;
        }
        if (!isArray && actualType == Point.class) {
            return POINT;
        }

        // Unable to resolve
        throw new IllegalArgumentException("Unable to resolve type: " + property.getTypeInformation());
    }
}
