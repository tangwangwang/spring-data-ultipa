package org.springframework.data.ultipa.core.schema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Wangwang Tang
 * @since 1.0
 */
class ArraySchema implements Schema {

    private final ArrayList<Object> delegate;

    public ArraySchema() {
        this(new ArrayList<>());
    }

    public ArraySchema(List<Object> delegate) {
        this.delegate = new ArrayList<>(delegate);
    }

    public List<Object> toArray() {
        return delegate;
    }

    @Override
    public Map<String, Object> toMap() {
        return Collections.emptyMap();
    }
}
