package org.springframework.data.ultipa.core.schema;

import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * {@link Schema} implementation backed by a {@link ArrayList}.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
class ArraySchema implements Schema {

    private final ArrayList<Object> delegate;

    ArraySchema(@Nullable List<Object> source) {
        this.delegate = source == null ? new ArrayList<>() : new ArrayList<>(source);
    }

    public List<Object> toArray() {
        return delegate;
    }

    @Nullable
    @Override
    public String getSchema() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Schema setSchema(String name) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public Object put(Object key, @Nullable Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object get(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Object> toMap() {
        return Collections.emptyMap();
    }
}
