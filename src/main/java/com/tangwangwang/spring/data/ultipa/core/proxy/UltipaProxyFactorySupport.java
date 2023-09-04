package com.tangwangwang.spring.data.ultipa.core.proxy;

import com.tangwangwang.spring.data.ultipa.core.UltipaOperations;
import com.tangwangwang.spring.data.ultipa.core.mapping.UltipaPersistentProperty;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Set;

/**
 * @author Wangwang Tang
 * @since 1.0
 */
abstract class UltipaProxyFactorySupport {


    protected static UltipaProxy createProxyTarget(UltipaOperations operations, UltipaPersistentProperty property, @Nullable Object source) {
        Class<?> propertyType = property.getType();
        if (List.class == propertyType) {
            return new ListProxy(operations, property, source);
        }
        if (Set.class == propertyType) {
            return new SetProxy(operations, property, source);
        }
        if (property.isCollectionLike()) {
            throw new IllegalStateException(String.format("Ultipa does not support proxy type '%s'", propertyType));
        } else {
            return new SchemaProxy(operations, property, source);
        }
    }

    /**
     * Returns the CGLib enhanced type for the given source type.
     */
    protected static Class<?> getEnhancedTypeFor(Class<?> type) {

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(type);
        enhancer.setCallbackType(MethodInterceptor.class);
        enhancer.setInterfaces(new Class[]{UltipaProxy.class});

        return enhancer.createClass();
    }

}

