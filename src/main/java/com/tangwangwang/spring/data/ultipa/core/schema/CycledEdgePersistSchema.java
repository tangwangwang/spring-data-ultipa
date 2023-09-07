package com.tangwangwang.spring.data.ultipa.core.schema;

import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * A cycled edge persistent implementation of the ultipa schema.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
class CycledEdgePersistSchema extends EdgePersistSchema {

    private final EdgeSchema target;

    CycledEdgePersistSchema(EdgeSchema target) {
        this.target = target;
    }

    @Override
    public NodeSchema from() {
        return target.from();
    }

    @Override
    public void from(NodeSchema schema) {
        target.from(schema);
    }

    @Override
    public NodeSchema to() {
        return target.to();
    }

    @Override
    public void to(NodeSchema schema) {
        target.to(schema);
    }

    @Override
    public PersistSchema find(String schema, Object source) {
        if (Objects.equals(getSchema(), schema) && Objects.equals(getSource(), source)) {
            return this;
        }
        return null;
    }

    @Override
    public Stream<NodeSchema> around() {
        return target.around();
    }

    @Override
    public void setIdName(String idName) {
        target.setIdName(idName);
    }

    @Override
    public void setIdValue(@Nullable Object idValue) {
        target.setIdValue(idValue);
    }

    @Override
    public void setIsNew(Boolean isNew) {
        target.setIsNew(isNew);
    }

    @Nullable
    @Override
    public Boolean getIsNew() {
        return target.getIsNew();
    }

    @Override
    public void setSystemUuid(Long uuid) {
        target.setSystemUuid(uuid);
    }

    @Nullable
    @Override
    public Long getSystemUuid() {
        return target.getSystemUuid();
    }

    @Override
    public void persisted() {
        target.persisted();
    }

    @Override
    public boolean isPersisted() {
        return target.isPersisted();
    }

    @Override
    public String toUqlString() {
        return target.toUqlString();
    }

    @Override
    public String getSchema() {
        return target.getSchema();
    }

    @Override
    public Schema setSchema(String name) {
        return target.setSchema(name);
    }

    @Override
    public Object getSource() {
        return target.getSource();
    }

    @Override
    public List<Object> toArray() {
        return target.toArray();
    }

    @Override
    public Map<String, Object> toMap() {
        return target.toMap();
    }

    @Override
    public Object get(Object key) {
        return target.get(key);
    }

    @Override
    public Object put(Object key, @Nullable Object value) {
        return target.put(key, value);
    }

    @Override
    public int hashCode() {
        return target.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof EdgeSchema && target.equals(obj);
    }

    @Override
    public String toString() {
        return target.toString();
    }
}
