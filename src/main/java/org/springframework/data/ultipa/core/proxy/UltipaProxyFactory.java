package org.springframework.data.ultipa.core.proxy;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.Factory;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.data.ultipa.core.UltipaOperations;
import org.springframework.data.ultipa.core.mapping.UltipaPersistentProperty;
import org.springframework.lang.Nullable;
import org.springframework.objenesis.Objenesis;
import org.springframework.objenesis.ObjenesisStd;
import org.springframework.util.ReflectionUtils;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * @author Wangwang Tang
 * @since 1.0
 */
public class UltipaProxyFactory extends UltipaProxyFactorySupport {

    private final Objenesis objenesis;

    public UltipaProxyFactory() {
        this.objenesis = new ObjenesisStd(true);
    }

    public Object createLazyLoadingProxy(UltipaOperations operations, UltipaPersistentProperty property, Object source) {
        Class<?> propertyType = property.getType();
        if (source instanceof SchemaProxy) {
            source = ((SchemaProxy) source).getTarget();
        }
        UltipaProxy proxy = createProxyTarget(operations, property, source);
        LazyLoadingInterceptor interceptor = new LazyLoadingInterceptor(source, proxy);

        if (!propertyType.isInterface()) {
            Factory factory = (Factory) objenesis.newInstance(getEnhancedTypeFor(propertyType));
            factory.setCallbacks(new Callback[]{interceptor});
            return factory;
        }

        ProxyFactory proxyFactory = new ProxyFactory();

        for (Class<?> type : propertyType.getInterfaces()) {
            proxyFactory.addInterface(type);
        }

        proxyFactory.addInterface(UltipaProxy.class);
        proxyFactory.addInterface(propertyType);
        proxyFactory.addAdvice(interceptor);

        return proxyFactory.getProxy(UltipaProxy.class.getClassLoader());
    }

    public static class LazyLoadingInterceptor implements MethodInterceptor, org.springframework.cglib.proxy.MethodInterceptor, Serializable {

        private static final Method INITIALIZE_METHOD, FINALIZE_METHOD, GET_SOURCE_METHOD, CLASS_METHOD;

        static {
            try {
                INITIALIZE_METHOD = UltipaProxy.class.getMethod("getTarget");
                FINALIZE_METHOD = Object.class.getDeclaredMethod("finalize");
                GET_SOURCE_METHOD = UltipaProxy.class.getMethod("getSource");
                CLASS_METHOD = Object.class.getDeclaredMethod("getClass");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private final @Nullable Object source;
        private final UltipaProxy proxy;

        public LazyLoadingInterceptor(@Nullable Object source, UltipaProxy proxy) {
            this.source = source;
            this.proxy = proxy;
        }

        @Nullable
        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {
            return intercept(invocation.getThis(), invocation.getMethod(), invocation.getArguments(), null);
        }

        @Nullable
        @Override
        public Object intercept(@Nullable Object object, Method method, Object[] args, @Nullable MethodProxy methodProxy) throws Throwable {
            if (INITIALIZE_METHOD.equals(method)) {
                return proxy.getTarget();
            }
            if (GET_SOURCE_METHOD.equals(method)) {
                return source;
            }
            if (CLASS_METHOD.equals(method)) {
                return proxy.getTargetClass();
            }

            if (ReflectionUtils.isObjectMethod(method) || !proxy.isInitialized()) {

                if (ReflectionUtils.isToStringMethod(method)) {
                    return proxyToString();
                }

                if (ReflectionUtils.isEqualsMethod(method)) {
                    return proxyEquals(args[0]);
                }

                if (ReflectionUtils.isHashCodeMethod(method)) {
                    return proxyHashCode();
                }

                // finalize methods should not trigger proxy initialization
                if (FINALIZE_METHOD.equals(method)) {
                    return null;
                }
            }

            Object target = proxy.getTarget();

            if (target == null) {
                return null;
            }

            ReflectionUtils.makeAccessible(method);

            return method.invoke(target, args);
        }

        private String proxyToString() {
            StringBuilder description = new StringBuilder();

            if (proxy.getSourceId() != null) {
                description.append(proxy.getTargetClass().getSimpleName())
                        .append("$")
                        .append(proxy.getClass().getSimpleName())
                        .append(" (sourceId = ")
                        .append(proxy.getSourceId())
                        .append(")");
            } else {
                description.append(UltipaProxy.class.getSimpleName())
                        .append(" (source = null)");
            }

            return description.toString();
        }

        private boolean proxyEquals(Object that) {

            if (!(that instanceof UltipaProxy)) {
                return false;
            }

            if (that == proxy) {
                return true;
            }

            return proxyToString().equals(that.toString());
        }

        private int proxyHashCode() {
            return proxyToString().hashCode();
        }
    }
}
