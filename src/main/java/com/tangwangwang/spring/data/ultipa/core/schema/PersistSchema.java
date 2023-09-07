package com.tangwangwang.spring.data.ultipa.core.schema;

import org.springframework.lang.Nullable;

/**
 * A persistent implementation of the ultipa schema.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
public interface PersistSchema extends Schema {

    @Nullable
    Object getSource();

    void setSource(Object source);

    void setSystemUuid(Long uuid);

    @Nullable
    Long getSystemUuid();

    void setIdName(String idName);

    void setIdValue(@Nullable Object idValue);

    void setIsNew(Boolean isNew);

    @Nullable
    Boolean getIsNew();

    PersistSchema left();

    void left(PersistSchema schema);

    default PersistSchema left(String schema) {
        PersistSchema left = left();
        left.setSchema(schema);
        return left;
    }

    PersistSchema right();

    void right(PersistSchema schema);

    default PersistSchema right(String schema) {
        PersistSchema right = right();
        right.setSchema(schema);
        return right;
    }

    boolean isPersisted();

    void persisted();

    @Nullable
    PersistSchema find(String schema, Object source);

    String toUqlString();
}
