package com.tangwangwang.spring.data.ultipa.core.mapping.event;

import org.springframework.data.mapping.callback.EntityCallback;

/**
 * @author Wangwang Tang
 * @since 1.0
 */
@FunctionalInterface
public interface BeforeConvertCallback<T> extends EntityCallback<T> {

    /**
     * Entity callback method invoked before a domain object is converted to be persisted. Can return either the same or a
     * modified instance of the domain object.
     *
     * @param entity the domain object to save.
     * @param schema name of the schema.
     * @return the domain object to be persisted.
     */
    T onBeforeConvert(T entity, String schema);

}
