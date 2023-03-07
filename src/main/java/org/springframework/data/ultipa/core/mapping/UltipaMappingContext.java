package org.springframework.data.ultipa.core.mapping;

import org.springframework.data.mapping.context.AbstractMappingContext;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.util.TypeInformation;

/**
 * @author Wangwang Tang
 * @since 1.0
 */
public class UltipaMappingContext extends AbstractMappingContext<UltipaPersistentEntity<?>, UltipaPersistentProperty> {
    /**
     * @param typeInformation
     * @param <T>
     * @return
     */
    @Override
    protected <T> UltipaPersistentEntity<?> createPersistentEntity(TypeInformation<T> typeInformation) {
        return null;
    }

    /**
     * @param property
     * @param owner
     * @param simpleTypeHolder
     * @return
     */
    @Override
    protected UltipaPersistentProperty createPersistentProperty(Property property, UltipaPersistentEntity<?> owner, SimpleTypeHolder simpleTypeHolder) {
        return null;
    }
}
