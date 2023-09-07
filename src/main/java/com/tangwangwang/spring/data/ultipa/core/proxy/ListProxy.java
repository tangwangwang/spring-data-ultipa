package com.tangwangwang.spring.data.ultipa.core.proxy;

import com.tangwangwang.spring.data.ultipa.core.UltipaOperations;
import com.tangwangwang.spring.data.ultipa.core.mapping.UltipaPersistentProperty;
import org.springframework.lang.Nullable;

/**
 * @author Wangwang Tang
 * @since 1.0
 */
class ListProxy extends AbstractUltipaProxy {

    protected ListProxy(UltipaOperations operations, UltipaPersistentProperty property, @Nullable Object source) {
        super(operations, property, source);
    }

    @Override
    protected Object getInitializeTarget() {
        return createQuery().findAll(targetType);
    }
}
