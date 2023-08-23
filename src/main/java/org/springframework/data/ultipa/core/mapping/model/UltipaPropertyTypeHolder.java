package org.springframework.data.ultipa.core.mapping.model;

import org.springframework.data.geo.Point;
import org.springframework.data.ultipa.annotation.PropertyType;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;

import static org.springframework.data.ultipa.annotation.PropertyType.*;

/**
 * Responsible for parsing default persistent property types for Java type mappings.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
public class UltipaPropertyTypeHolder {

    private static final Map<PropertyType, PropertyType> ARRAY_MAPPED_TYPES;
    private static final Map<Class<?>, PropertyType> DEFAULT_MAPPED_TYPES;

    static {
        // array type mapped
        Map<PropertyType, PropertyType> arrayMappedTypes = new HashMap<>();
        arrayMappedTypes.put(STRING, STRING_ARRAY);
        arrayMappedTypes.put(TEXT, TEXT_ARRAY);
        arrayMappedTypes.put(INT32, INT32_ARRAY);
        arrayMappedTypes.put(INT64, INT64_ARRAY);
        arrayMappedTypes.put(UINT32, UINT32_ARRAY);
        arrayMappedTypes.put(UINT64, UINT64_ARRAY);
        arrayMappedTypes.put(FLOAT, FLOAT_ARRAY);
        arrayMappedTypes.put(DOUBLE, DOUBLE_ARRAY);
        arrayMappedTypes.put(DATETIME, DATETIME_ARRAY);
        arrayMappedTypes.put(TIMESTAMP, TIMESTAMP_ARRAY);

        ARRAY_MAPPED_TYPES = Collections.unmodifiableMap(arrayMappedTypes);

        // java type mapped
        Map<Class<?>, PropertyType> defaultMappedTypes = new HashMap<>();
        defaultMappedTypes.put(Boolean.class, STRING);
        defaultMappedTypes.put(Character.class, STRING);
        defaultMappedTypes.put(Byte.class, INT32);
        defaultMappedTypes.put(Short.class, INT32);
        defaultMappedTypes.put(Integer.class, INT32);
        defaultMappedTypes.put(Long.class, INT64);
        defaultMappedTypes.put(Float.class, FLOAT);
        defaultMappedTypes.put(Double.class, DOUBLE);

        defaultMappedTypes.put(String.class, STRING);
        defaultMappedTypes.put(BigInteger.class, UINT64);
        defaultMappedTypes.put(BigDecimal.class, DOUBLE);

        defaultMappedTypes.put(com.ultipa.sdk.data.Point.class, POINT);
        defaultMappedTypes.put(Point.class, POINT);

        defaultMappedTypes.put(Period.class, STRING);
        defaultMappedTypes.put(Duration.class, STRING);
        defaultMappedTypes.put(Instant.class, TIMESTAMP);
        defaultMappedTypes.put(Timestamp.class, TIMESTAMP);
        defaultMappedTypes.put(Date.class, TIMESTAMP);
        defaultMappedTypes.put(ZonedDateTime.class, TIMESTAMP);

        defaultMappedTypes.put(LocalDate.class, DATETIME);
        defaultMappedTypes.put(LocalTime.class, DATETIME);
        defaultMappedTypes.put(LocalDateTime.class, DATETIME);

        defaultMappedTypes.put(UUID.class, STRING);
        defaultMappedTypes.put(Locale.class, STRING);
        defaultMappedTypes.put(Class.class, STRING);

        DEFAULT_MAPPED_TYPES = Collections.unmodifiableMap(defaultMappedTypes);
    }

    /**
     * Returns the mapped default persistence type.
     *
     * @param type must not be {@literal null}.
     * @return persistence simple type
     */
    @Nullable
    public static PropertyType getPropertyType(Class<?> type) {
        Class<?> actualType = ClassUtils.resolvePrimitiveIfNecessary(type);
        return DEFAULT_MAPPED_TYPES.get(actualType);
    }

    /**
     * Returns the mapped persistence array type.
     *
     * @param type must not be {@literal null}.
     * @return persistence array type
     */
    @Nullable
    public static PropertyType getArrayType(PropertyType type) {
        return ARRAY_MAPPED_TYPES.get(type);
    }
}
