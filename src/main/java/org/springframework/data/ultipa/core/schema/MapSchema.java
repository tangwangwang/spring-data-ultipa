package org.springframework.data.ultipa.core.schema;

import com.ultipa.sdk.operate.entity.Edge;
import com.ultipa.sdk.operate.entity.Node;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * {@link Schema} implementation backed by a {@link LinkedHashMap}.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
class MapSchema implements Schema, Map<String, Object> {

    private final Map<String, Object> delegate;
    protected @Nullable String name;

    MapSchema() {
        this(new LinkedHashMap<>());
    }

    MapSchema(Map<String, Object> delegate) {
        this.delegate = new LinkedHashMap<>(delegate);
    }

    MapSchema(Node node) {
        this.delegate = new LinkedHashMap<>();
        this.name = node.getSchema();
        this.delegate.put(UUID_FIELD_NAME, node.getUUID());
        this.delegate.put(ID_FIELD_NAME, node.getID());
        this.delegate.putAll(node.getValues());
    }

    MapSchema(Edge edge) {
        this.delegate = new LinkedHashMap<>();
        this.name = edge.getSchema();
        this.delegate.put(UUID_FIELD_NAME, edge.getUUID());
        this.delegate.put(FROM_FIELD_NAME, edge.getFrom());
        this.delegate.put(TO_FIELD_NAME, edge.getTo());
        this.delegate.put(FROM_UUID_FIELD_NAME, edge.getFromUUID());
        this.delegate.put(TO_UUID_FIELD_NAME, edge.getToUUID());
        this.delegate.putAll(edge.getValues());
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.ultipa.core.schema.Schema#getName()
     */
    @Override
    public String getName() {
        if (name == null) {
            throw new IllegalStateException("No name associated with this Schema");
        }

        return this.name;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.ultipa.core.schema.Schema#setId(java.lang.String)
     */
    @Override
    public Schema setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public List<Object> toArray() {
        return Collections.emptyList();
    }

    @Override
    public Map<String, Object> toMap() {
        return new LinkedHashMap<>(delegate);
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#size()
     */
    @Override
    public int size() {
        return delegate.size();
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    @Override
    public boolean containsKey(Object key) {
        return delegate.containsKey(key);
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    @Override
    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#get(java.lang.Object)
     */
    @Override
    public Object get(Object key) {
        return delegate.get(key);
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#getOrDefault(java.lang.Object, java.lang.Object)
     */
    @Override
    public Object getOrDefault(Object key, Object defaultValue) {
        return delegate.getOrDefault(key, defaultValue);
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public Object put(String key, Object value) {
        return delegate.put(key, value);
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#remove(java.lang.Object)
     */
    @Override
    public Object remove(Object key) {
        return delegate.remove(key);
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#putAll(Map)
     */
    @Override
    public void putAll(Map<? extends String, ?> m) {
        delegate.putAll(m);
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#clear()
     */
    @Override
    public void clear() {
        delegate.clear();
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#keySet()
     */
    @Override
    public Set<String> keySet() {
        return delegate.keySet();
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#values()
     */
    @Override
    public Collection<Object> values() {
        return delegate.values();
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#entrySet()
     */
    @Override
    public Set<Entry<String, Object>> entrySet() {
        return delegate.entrySet();
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        return delegate.equals(o);
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#forEach(java.util.function.BiConsumer)
     */
    @Override
    public void forEach(BiConsumer<? super String, ? super Object> action) {
        delegate.forEach(action);
    }
}
