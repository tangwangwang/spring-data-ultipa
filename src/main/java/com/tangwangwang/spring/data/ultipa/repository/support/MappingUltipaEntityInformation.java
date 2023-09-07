package com.tangwangwang.spring.data.ultipa.repository.support;

import com.tangwangwang.spring.data.ultipa.core.mapping.UltipaPersistentEntity;

/**
 * {@link UltipaEntityInformation} implementation using a {@link UltipaPersistentEntity} instance to look up the necessary
 * information. Can be configured with a custom collection to be returned which will trump the one returned by the
 * {@link UltipaPersistentEntity} if given.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
public class MappingUltipaEntityInformation<T, ID> extends UltipaEntityInformationSupport<T, ID> {

    private final UltipaPersistentEntity<T> entity;

    public MappingUltipaEntityInformation(UltipaPersistentEntity<T> entity) {
        super(entity);
        this.entity = entity;

        if (!isNode() && !isEdge()) {
            throw new IllegalStateException(String.format("%s not found schema annotation type annotation exception.", entity.getType()));
        }
    }

    @Override
    public String getSchemaName() {
        return entity.getSchemaName();
    }

    @Override
    public String getIdPropertyName() {
        return entity.getRequiredIdProperty().getPropertyName();
    }

    @Override
    public boolean isSystemId() {
        return entity.getRequiredIdProperty().isSystemProperty();
    }

    @Override
    public boolean isNode() {
        return entity.isNode();
    }

    @Override
    public boolean isEdge() {
        return entity.isEdge();
    }
}
