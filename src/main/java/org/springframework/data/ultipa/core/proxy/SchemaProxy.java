package org.springframework.data.ultipa.core.proxy;

import org.springframework.data.ultipa.core.UltipaOperations;
import org.springframework.data.ultipa.core.mapping.UltipaPersistentProperty;
import org.springframework.lang.Nullable;

import java.io.Serializable;

/**
 * @author Wangwang Tang
 * @since 1.0
 */
class SchemaProxy extends AbstractUltipaProxy implements UltipaProxy, Serializable {

    public SchemaProxy(UltipaOperations operations, UltipaPersistentProperty property, @Nullable Object source) {
        super(operations, property, source);
    }

    @Nullable
    @Override
    protected Object getInitializeTarget() {
        return createQuery().findOne(targetType);
    }

}