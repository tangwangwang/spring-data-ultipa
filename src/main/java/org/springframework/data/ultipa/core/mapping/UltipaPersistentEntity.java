package org.springframework.data.ultipa.core.mapping;

import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.model.MutablePersistentEntity;

/**
 * Ultipa specific {@link PersistentEntity} abstraction.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
public interface UltipaPersistentEntity<T> extends MutablePersistentEntity<T, UltipaPersistentProperty> {

    /**
     * Returns the name to be used to store the schema.
     *
     * @return schema name
     */
    String getSchemaName();

    /**
     * Returns the description to be used to store the schema.
     *
     * @return schema description
     */
    String getDescription();

    boolean isNode();

    boolean isEdge();

    default boolean isSchema() {
        return isNode() || isEdge();
    }
}
