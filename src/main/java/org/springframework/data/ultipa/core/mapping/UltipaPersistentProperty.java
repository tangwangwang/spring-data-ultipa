package org.springframework.data.ultipa.core.mapping;

import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.ultipa.annotation.CascadeType;
import org.springframework.data.ultipa.annotation.Enumerated;
import org.springframework.data.ultipa.annotation.FetchType;
import org.springframework.data.ultipa.annotation.PropertyType;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * Ultipa specific {@link PersistentProperty} extension.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
public interface UltipaPersistentProperty extends PersistentProperty<UltipaPersistentProperty> {

    @Override
    UltipaPersistentEntity<?> getOwner();

    /**
     * Returns the name to be used to store the property in the schema.
     *
     * @return schema property name
     */
    String getPropertyName();

    /**
     * Returns the description to be used to store the property in the schema.
     *
     * @return schema property description
     */
    String getDescription();

    /**
     * Returns the type to be used to store the property in the schema.
     *
     * @return schema property type
     */
    PropertyType getPropertyType();

    boolean isJson();

    boolean isEnumProperty();

    @Nullable
    Enumerated.Type getEnumeratedType();

    default Enumerated.Type getRequiredEnumeratedType() {
        Enumerated.Type enumeratedType = getEnumeratedType();
        if (enumeratedType == null) {
            throw new IllegalStateException();
        }
        return enumeratedType;
    }

    boolean isFromProperty();

    boolean isToProperty();

    boolean isLeftProperty();

    boolean isRightProperty();

    boolean isReferenceProperty();

    boolean isCreatedProperty();

    List<CascadeType> getCascadeTypes();

    @Nullable
    FetchType getFetchType();

    @Nullable
    String getBetweenSchemaName();

    @Nullable
    Class<?> getReferenceType();

    @Nullable
    <E> Object getProperty(E entity);

}
