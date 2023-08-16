package org.springframework.data.ultipa.annotation;

import com.ultipa.Ultipa;
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
    TIMESTAMP("timestamp");

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
     * Resolves the default Ultipa property type according to the given Java type
     *
     * @param type The given Java type
     * @return The ultipa property type
     */
    public static PropertyType resolverAutoJavaType(Class<?> type) {
        Class<?> actualType = ClassUtils.resolvePrimitiveIfNecessary(type);
        if (Arrays.asList(Boolean.class, Byte.class, Short.class, Integer.class).contains(type)) {
            return INT32;
        }
        if (type == Long.class) {
            return INT64;
        }
        if (type == BigInteger.class) {
            return UINT64;
        }
        if (type == Double.class || type == BigDecimal.class) {
            return DOUBLE;
        }
        if (type == Float.class) {
            return FLOAT;
        }
        if (type == Timestamp.class || type == Instant.class) {
            return TIMESTAMP;
        }
        if (Arrays.asList(Date.class, LocalDate.class, LocalTime.class, LocalDateTime.class).contains(actualType)) {
            return DATETIME;
        }
        return STRING;
    }
}
