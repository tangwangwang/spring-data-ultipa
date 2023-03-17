package org.springframework.data.ultipa.core.mapping;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mapping.context.AbstractMappingContext;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.model.FieldNamingStrategy;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.PropertyNameFieldNamingStrategy;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.ultipa.core.mapping.model.UltipaSimpleTypeHolder;
import org.springframework.data.util.TypeInformation;
import org.springframework.lang.Nullable;

/**
 * Default implementation of a {@link MappingContext} for Ultipa using {@link BasicUltipaPersistentEntity} and
 * {@link BasicUltipaPersistentProperty} as primary abstractions.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
public class UltipaMappingContext extends AbstractMappingContext<UltipaPersistentEntity<?>, UltipaPersistentProperty> {

    private static final FieldNamingStrategy DEFAULT_NAMING_STRATEGY = PropertyNameFieldNamingStrategy.INSTANCE;
    private FieldNamingStrategy fieldNamingStrategy = DEFAULT_NAMING_STRATEGY;
    private @Nullable ApplicationContext applicationContext;

    /**
     * Creates a new {@link UltipaMappingContext}.
     */
    public UltipaMappingContext() {
        setSimpleTypeHolder(UltipaSimpleTypeHolder.HOLDER);
    }

    /**
     * Configures the {@link FieldNamingStrategy} to be used to determine the schema property name if no manual mapping
     * is applied. Defaults to a strategy using the plain property name.
     *
     * @param fieldNamingStrategy the {@link FieldNamingStrategy} to be used to determine the schema property name if
     *                            no manual mapping is applied.
     */
    public void setFieldNamingStrategy(@Nullable FieldNamingStrategy fieldNamingStrategy) {
        this.fieldNamingStrategy = fieldNamingStrategy == null ? DEFAULT_NAMING_STRATEGY : fieldNamingStrategy;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.mapping.context.AbstractMappingContext#shouldCreatePersistentEntityFor(org.springframework.data.util.TypeInformation)
     */
    @Override
    protected boolean shouldCreatePersistentEntityFor(TypeInformation<?> type) {
        return super.shouldCreatePersistentEntityFor(type);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.mapping.AbstractMappingContext#createPersistentProperty(java.lang.reflect.Field, java.beans.PropertyDescriptor, org.springframework.data.mapping.MutablePersistentEntity, org.springframework.data.mapping.SimpleTypeHolder)
     */
    @Override
    public UltipaPersistentProperty createPersistentProperty(Property property, UltipaPersistentEntity<?> owner,
                                                             SimpleTypeHolder simpleTypeHolder) {
        return new BasicUltipaPersistentProperty(property, owner, simpleTypeHolder, fieldNamingStrategy);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.mapping.BasicMappingContext#createPersistentEntity(org.springframework.data.util.TypeInformation, org.springframework.data.mapping.model.MappingContext)
     */
    @Override
    protected <T> UltipaPersistentEntity<T> createPersistentEntity(TypeInformation<T> typeInformation) {
        return new BasicUltipaPersistentEntity<>(typeInformation);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        this.applicationContext = applicationContext;
        super.setApplicationContext(applicationContext);
    }
}
