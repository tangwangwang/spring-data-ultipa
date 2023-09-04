package com.tangwangwang.spring.data.ultipa.core.schema;

import com.tangwangwang.spring.data.ultipa.core.mapping.model.UltipaSystemProperty;
import com.ultipa.sdk.operate.entity.Edge;
import com.ultipa.sdk.operate.entity.Node;
import org.springframework.lang.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link Schema} implementation backed by a {@link LinkedHashMap}.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
class MapSchema implements Schema {

    private final Map<String, Object> delegate;
    private @Nullable String schema;

    MapSchema() {
        this(new LinkedHashMap<>());
    }

    MapSchema(Map<String, Object> delegate) {
        this.delegate = new LinkedHashMap<>(delegate);
    }

    MapSchema(Node node) {
        this.delegate = new LinkedHashMap<>();
        this.schema = node.getSchema();
        put(UltipaSystemProperty.ID.getMappedName(), node.getID());
        put(UltipaSystemProperty.UUID.getMappedName(), node.getUUID());
        this.delegate.putAll(node.getValues());
    }

    MapSchema(Edge edge) {
        this.delegate = new LinkedHashMap<>();
        this.schema = edge.getSchema();
        put(UltipaSystemProperty.UUID.getMappedName(), edge.getUUID());
        put(UltipaSystemProperty.FROM.getMappedName(), edge.getFrom());
        put(UltipaSystemProperty.TO.getMappedName(), edge.getTo());
        put(UltipaSystemProperty.FROM_UUID.getMappedName(), edge.getFromUUID());
        put(UltipaSystemProperty.TO_UUID.getMappedName(), edge.getToUUID());
        this.delegate.putAll(edge.getValues());
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.ultipa.core.schema.Schema#getSchema()
     */
    @Nullable
    @Override
    public String getSchema() {
        return this.schema;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.ultipa.core.schema.Schema#setSchema(java.lang.String)
     */
    @Override
    public Schema setSchema(String name) {
        this.schema = name;
        return this;
    }

    @Override
    public List<Object> toArray() {
        return Collections.emptyList();
    }

    @Override
    public Map<String, Object> toMap() {
        return delegate;
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#get(java.lang.Object)
     */
    @Override
    public Object get(Object key) {
        return delegate.get(key.toString());
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public Object put(Object key, @Nullable Object value) {
        return delegate.put(key.toString(), value);
    }

    public Map<String, Object> getDelegate() {
        return delegate;
    }
}
