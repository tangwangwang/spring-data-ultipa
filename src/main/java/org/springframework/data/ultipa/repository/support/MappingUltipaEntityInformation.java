package org.springframework.data.ultipa.repository.support;

import org.springframework.data.repository.core.support.PersistentEntityInformation;
import org.springframework.data.ultipa.core.mapping.UltipaPersistentEntity;

/**
 * {@link UltipaEntityInformation} implementation using a {@link UltipaPersistentEntity} instance to look up the necessary
 * information. Can be configured with a custom collection to be returned which will trump the one returned by the
 * {@link UltipaPersistentEntity} if given.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
public class MappingUltipaEntityInformation<T, ID> extends PersistentEntityInformation<T, ID> implements UltipaEntityInformation<T, ID> {

    public MappingUltipaEntityInformation(UltipaPersistentEntity<T> entity) {
        super(entity);
    }

}
