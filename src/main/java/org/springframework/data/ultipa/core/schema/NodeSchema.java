package org.springframework.data.ultipa.core.schema;

import org.springframework.lang.Nullable;

import java.util.stream.Stream;

/**
 * A node implementation of the ultipa schema.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
public interface NodeSchema extends PersistSchema {

    /**
     * Create a new {@link NodeSchema}.
     *
     * @return a new {@link NodeSchema}.
     */
    static NodeSchema of(Object source) {
        return new NodePersistSchema(source);
    }

    /**
     * Create a new delegated {@link NodeSchema}.
     *
     * @param delegatedSchema a delegated {@link NodeSchema}.
     * @return a new delegated {@link NodeSchema}.
     */
    static NodeSchema of(NodeSchema delegatedSchema) {
        return new CycledNodePersistSchema(delegatedSchema);
    }

    void setSystemId(String id);

    @Nullable
    String getSystemId();

    @Override
    EdgeSchema left();

    @Override
    default void left(PersistSchema schema) {
        if (schema instanceof EdgeSchema) {
            left((EdgeSchema) schema);
        }
    }

    void left(EdgeSchema schema);

    @Override
    EdgeSchema right();

    @Override
    default void right(PersistSchema schema) {
        if (schema instanceof EdgeSchema) {
            right((EdgeSchema) schema);
        }
    }

    void right(EdgeSchema schema);

    boolean hasLeft(EdgeSchema schema);

    boolean hasRight(EdgeSchema schema);

    Stream<EdgeSchema> around();
}
