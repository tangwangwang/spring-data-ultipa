package com.tangwangwang.spring.data.ultipa.repository.config;

import com.tangwangwang.spring.data.ultipa.core.convert.MappingUltipaConverter;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.data.mapping.context.PersistentEntities;

/**
 * @author Wangwang Tang
 * @since 1.0
 */
public class PersistentEntitiesFactoryBean implements FactoryBean<PersistentEntities> {

    private final MappingUltipaConverter converter;

    /**
     * Creates a new {@link PersistentEntitiesFactoryBean} for the given {@link MappingUltipaConverter}.
     *
     * @param converter must not be {@literal null}.
     */
    public PersistentEntitiesFactoryBean(MappingUltipaConverter converter) {
        this.converter = converter;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.beans.factory.FactoryBean#getObject()
     */
    @Override
    public PersistentEntities getObject() {
        return PersistentEntities.of(this.converter.getMappingContext());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.beans.factory.FactoryBean#getObjectType()
     */
    @Override
    public Class<?> getObjectType() {
        return PersistentEntities.class;
    }

}
