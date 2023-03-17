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
    AUTO("", ""),
    STRING("string", "\n\t\t\n\t\t\n\uE000\uE001\uE002\n\t\t\t\t\n"),
    TEXT("text", "\n\t\t\n\t\t\n\uE000\uE001\uE002\n\t\t\t\t\n"),
    INT32("int32", Integer.MAX_VALUE),
    INT64("int64", Long.MAX_VALUE),
    UINT32("uint32", 4294967295L),
    UINT64("uint64", "18446744073709551615"),
    FLOAT("float", Float.MAX_VALUE),
    DOUBLE("double", Double.MAX_VALUE),
    DATETIME("datetime", "0-0-0 0:0:0.000000"),
    TIMESTAMP("timestamp", "1970-01-01 08:00:00");

    private final String mappedName;
    private final Object nullValue;

    PropertyType(String mappedName, Object nullValue) {
        this.mappedName = mappedName;
        this.nullValue = nullValue;
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
     * Returns the null value used to represent the type.
     *
     * @return the null value used to represent the type.
     */
    public Object getNullValue() {
        return nullValue;
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
