package org.springframework.data.ultipa.core.proxy;

import org.springframework.lang.Nullable;

/**
 * Provides proxy support for lazy loading.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
public interface UltipaProxy {

    /**
     * Initializes the proxy and returns the wrapped value.
     */
    @Nullable
    Object getTarget();

    /**
     * Returns the raw source object that defines the reference.
     */
    @Nullable
    Object getSource();

    /**
     * Returns the wrapped type (does not trigger initialization)
     */
    Class<?> getTargetClass();

    /**
     * Returns the proxy object is initialized (does not trigger initialization)
     */
    boolean isInitialized();

    /**
     * Returns source object id (does not trigger initialization)
     */
    @Nullable
    Object getSourceId();

    /**
     * Initializes the proxy and returns the wrapped value when give object is {@link UltipaProxy} instance,
     * otherwise returns null.
     */
    @Nullable
    static Object getTarget(Object object) {
        if (object instanceof UltipaProxy) {
            return ((UltipaProxy) object).getTarget();
        }
        return null;
    }

    /**
     * initializes the proxy when give object is {@link AbstractUltipaProxy} instance.
     */
    static void initialize(Object object) {
        if (object instanceof AbstractUltipaProxy) {
            ((AbstractUltipaProxy) object).initialize();
        }
    }

}
