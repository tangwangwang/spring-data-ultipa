package com.tangwangwang.spring.data.ultipa.repository.support;

import com.tangwangwang.spring.data.ultipa.core.UltipaOperations;
import com.tangwangwang.spring.data.ultipa.core.mapping.UltipaPersistentEntity;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.repository.core.support.PersistentEntityInformation;
import org.springframework.util.Assert;

/**
 * @author Wangwang Tang
 * @since 1.0
 */
public abstract class UltipaEntityInformationSupport<T, ID> extends PersistentEntityInformation<T, ID> implements UltipaEntityInformation<T, ID> {

    protected UltipaEntityInformationSupport(PersistentEntity<T, ? extends PersistentProperty<?>> persistentEntity) {
        super(persistentEntity);
    }


    public static <T, ID> UltipaEntityInformation<T, ID> getEntityInformation(Class<T> domainClass, UltipaOperations operations) {
        UltipaPersistentEntity<?> persistentEntity = operations.getConverter().getMappingContext().getRequiredPersistentEntity(domainClass);
        return getEntityInformation(persistentEntity);
    }

    /**
     * Create an {@link UltipaEntityInformation} by {@link UltipaPersistentEntity}
     *
     * @param entity The given entity object, must not be {@literal null}.
     * @return a new {@link UltipaEntityInformation} Object
     */
    @SuppressWarnings("unchecked")
    public static <T, ID> UltipaEntityInformation<T, ID> getEntityInformation(UltipaPersistentEntity<?> entity) {
        Assert.notNull(entity, "Entity must not be null!");
        return new MappingUltipaEntityInformation<>((UltipaPersistentEntity<T>) entity);
    }

}
