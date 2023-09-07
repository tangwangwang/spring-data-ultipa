package com.tangwangwang.spring.data.ultipa.core.proxy;

import com.tangwangwang.spring.data.ultipa.core.UltipaOperations;
import com.tangwangwang.spring.data.ultipa.core.mapping.UltipaPersistentProperty;
import org.springframework.lang.Nullable;

import java.util.HashSet;

/**
 * @author Wangwang Tang
 * @since 1.0
 */
class SetProxy extends AbstractUltipaProxy {

    protected SetProxy(UltipaOperations operations, UltipaPersistentProperty property, @Nullable Object source) {
        super(operations, property, source);
    }

    @Override
    protected Object getInitializeTarget() {
        return new HashSet<>(createQuery().findAll(targetType));
    }
}
