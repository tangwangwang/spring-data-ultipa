package com.tangwangwang.spring.data.ultipa.core.mapping.model;

import com.tangwangwang.spring.data.ultipa.annotation.PropertyType;
import org.springframework.data.geo.Point;
import org.springframework.data.util.Pair;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;

/**
 * Responsible for parsing default persistent property types for Java type mappings.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
public class UltipaPropertyTypeHolder {

    private static final List<Pair<PropertyType, PropertyType>> ARRAY_MAPPED_TYPES;
    private static final Map<Class<?>, PropertyType> DEFAULT_MAPPED_TYPES;

    static {
        // array type mapped
        List<Pair<PropertyType, PropertyType>> arrayMappedTypes = new ArrayList<>();
        arrayMappedTypes.add(Pair.of(PropertyType.STRING, PropertyType.STRING_ARRAY));
        arrayMappedTypes.add(Pair.of(PropertyType.TEXT, PropertyType.TEXT_ARRAY));
        arrayMappedTypes.add(Pair.of(PropertyType.INT32, PropertyType.INT32_ARRAY));
        arrayMappedTypes.add(Pair.of(PropertyType.INT64, PropertyType.INT64_ARRAY));
        arrayMappedTypes.add(Pair.of(PropertyType.UINT32, PropertyType.UINT32_ARRAY));
        arrayMappedTypes.add(Pair.of(PropertyType.UINT64, PropertyType.UINT64_ARRAY));
        arrayMappedTypes.add(Pair.of(PropertyType.FLOAT, PropertyType.FLOAT_ARRAY));
        arrayMappedTypes.add(Pair.of(PropertyType.DOUBLE, PropertyType.DOUBLE_ARRAY));
        arrayMappedTypes.add(Pair.of(PropertyType.DATETIME, PropertyType.DATETIME_ARRAY));
        arrayMappedTypes.add(Pair.of(PropertyType.TIMESTAMP, PropertyType.TIMESTAMP_ARRAY));

        ARRAY_MAPPED_TYPES = Collections.unmodifiableList(arrayMappedTypes);

        // java type mapped
        Map<Class<?>, PropertyType> defaultMappedTypes = new HashMap<>();
        defaultMappedTypes.put(Boolean.class, PropertyType.STRING);
        defaultMappedTypes.put(Character.class, PropertyType.STRING);
        defaultMappedTypes.put(Byte.class, PropertyType.INT32);
        defaultMappedTypes.put(Short.class, PropertyType.INT32);
        defaultMappedTypes.put(Integer.class, PropertyType.INT32);
        defaultMappedTypes.put(Long.class, PropertyType.INT64);
        defaultMappedTypes.put(Float.class, PropertyType.FLOAT);
        defaultMappedTypes.put(Double.class, PropertyType.DOUBLE);

        defaultMappedTypes.put(String.class, PropertyType.STRING);
        defaultMappedTypes.put(BigInteger.class, PropertyType.UINT64);
        defaultMappedTypes.put(BigDecimal.class, PropertyType.DOUBLE);

        defaultMappedTypes.put(com.ultipa.sdk.data.Point.class, PropertyType.POINT);
        defaultMappedTypes.put(Point.class, PropertyType.POINT);

        defaultMappedTypes.put(Period.class, PropertyType.STRING);
        defaultMappedTypes.put(Duration.class, PropertyType.STRING);
        defaultMappedTypes.put(Instant.class, PropertyType.TIMESTAMP);
        defaultMappedTypes.put(Timestamp.class, PropertyType.TIMESTAMP);
        defaultMappedTypes.put(Date.class, PropertyType.TIMESTAMP);
        defaultMappedTypes.put(ZonedDateTime.class, PropertyType.TIMESTAMP);

        defaultMappedTypes.put(LocalDate.class, PropertyType.DATETIME);
        defaultMappedTypes.put(LocalTime.class, PropertyType.DATETIME);
        defaultMappedTypes.put(LocalDateTime.class, PropertyType.DATETIME);

        defaultMappedTypes.put(UUID.class, PropertyType.STRING);
        defaultMappedTypes.put(Locale.class, PropertyType.STRING);
        defaultMappedTypes.put(Class.class, PropertyType.STRING);

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
        return ARRAY_MAPPED_TYPES.stream()
                .filter(it -> it.getFirst() == type)
                .map(Pair::getSecond)
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns the mapped persistence simple type.
     *
     * @param type must not be {@literal null}.
     * @return persistence simple type
     */
    @Nullable
    public static PropertyType getSimpleType(PropertyType type) {
        return ARRAY_MAPPED_TYPES.stream()
                .filter(it -> it.getSecond() == type)
                .map(Pair::getFirst)
                .findFirst()
                .orElse(null);
    }
}
