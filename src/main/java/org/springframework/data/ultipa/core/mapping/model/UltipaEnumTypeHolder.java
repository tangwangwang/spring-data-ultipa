package org.springframework.data.ultipa.core.mapping.model;

import org.springframework.data.ultipa.annotation.EnumId;
import org.springframework.lang.Nullable;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Responsible for the conversion of Ultipa data and enumeration types, and parsing fields annotated with
 * {@link EnumId} in enum classes
 *
 * @author Wangwang Tang
 * @since 1.0
 */
public class UltipaEnumTypeHolder {

    public static final Class<?> NAME_TYPE = String.class;
    public static final Class<?> ORDINAL_TYPE = int.class;
    /**
     * means empty field
     */
    private static final Object EMPTY = new Object();
    /**
     * referencing an empty field
     */
    private static final Field EMPTY_FIELD;

    private static final Map<Class<? extends Enum<?>>, Field> ULTIPA_ENUM_TYPES = new ConcurrentHashMap<>();

    static {
        try {
            EMPTY_FIELD = UltipaEnumTypeHolder.class.getDeclaredField("EMPTY");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private UltipaEnumTypeHolder() {
    }

    public static <E extends Enum<E>> boolean hasEnumField(Class<E> enumType) {
        return getEnumField(enumType) != null;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <E extends Enum<E>> Class<E> getEnumFieldType(Class<E> enumType) {
        return (Class<E>) Optional.ofNullable(getEnumField(enumType))
                .map(Field::getType)
                .orElse(null);
    }

    public static <E extends Enum<E>> Field getRequiredEnumField(Class<E> enumType) {
        Field enumField = getEnumField(enumType);

        if (enumField == null) {
            throw new IllegalArgumentException();
        }

        return enumField;
    }

    @Nullable
    public static <E extends Enum<E>> Field getEnumField(Class<E> enumType) {
        Field field = resolveEnumField(enumType);
        return field == EMPTY_FIELD ? null : field;
    }

    private static <E extends Enum<E>> Field resolveEnumField(Class<E> enumType) {
        return ULTIPA_ENUM_TYPES.computeIfAbsent(enumType, key -> Arrays.stream(key.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(EnumId.class))
                .findFirst().orElse(EMPTY_FIELD));
    }

}
