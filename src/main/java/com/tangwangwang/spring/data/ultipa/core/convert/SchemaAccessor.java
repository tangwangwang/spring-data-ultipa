package com.tangwangwang.spring.data.ultipa.core.convert;

import com.tangwangwang.spring.data.ultipa.core.mapping.UltipaPersistentProperty;
import com.tangwangwang.spring.data.ultipa.core.schema.Schema;
import org.springframework.lang.Nullable;

/**
 * Wrapper value object for a {@link Schema} to be able to access raw values by {@link UltipaPersistentProperty}
 * references. The accessors will transparently resolve nested document values that a {@link UltipaPersistentProperty}
 * might refer to through a path expression in field names.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
class SchemaAccessor {

    private final Schema schema;

    /**
     * Creates a new {@link SchemaAccessor} for the given {@link Schema}.
     *
     * @param schema must be a {@link Schema} effectively, must not be {@literal null}.
     */
    SchemaAccessor(Schema schema) {
        this.schema = schema;
    }

    /**
     * Returns the value the given {@link UltipaPersistentProperty} refers to.
     *
     * @param property must not be {@literal null}.
     * @return can be {@literal null}.
     */
    @Nullable
    public Object get(UltipaPersistentProperty property) {
        return schema.get(property.getPropertyName());
    }

    /**
     * Puts the given value into the backing {@link Schema} based on the coordinates defined through the given
     * {@link UltipaPersistentProperty}.
     *
     * @param property must not be {@literal null}.
     * @param value    can be {@literal null}.
     */
    public void set(UltipaPersistentProperty property, @Nullable Object value) {
        schema.put(property.getPropertyName(), value);
    }

}
