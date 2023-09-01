package org.springframework.data.ultipa.core.schema;

import java.util.stream.Stream;

/**
 * An edge implementation of the ultipa schema.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
public interface EdgeSchema extends PersistSchema {

    /**
     * Create a new {@link EdgeSchema}.
     *
     * @return a new {@link EdgeSchema}.
     */
    static EdgeSchema of(Object source) {
        return new EdgePersistSchema(source);
    }

    /**
     * Create a new delegated {@link EdgeSchema}.
     *
     * @param delegatedSchema a delegated {@link EdgeSchema}.
     * @return a new delegated {@link EdgeSchema}.
     */
    static EdgeSchema of(EdgeSchema delegatedSchema) {
        return new CycledEdgePersistSchema(delegatedSchema);
    }

    @Override
    default NodeSchema left() {
        return from();
    }

    @Override
    default void left(PersistSchema schema) {
        if (schema instanceof NodeSchema) {
            from((NodeSchema) schema);
        }
    }

    NodeSchema from();

    void from(NodeSchema schema);

    @Override
    default NodeSchema right() {
        return to();
    }

    @Override
    default void right(PersistSchema schema) {
        if (schema instanceof NodeSchema) {
            to((NodeSchema) schema);
        }
    }

    NodeSchema to();

    void to(NodeSchema schema);

    Stream<NodeSchema> around();

}
