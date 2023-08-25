package org.springframework.data.ultipa.core.mapping;

import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.ultipa.annotation.CascadeType;
import org.springframework.data.ultipa.annotation.EnumType;
import org.springframework.data.ultipa.annotation.FetchType;
import org.springframework.data.ultipa.annotation.PropertyType;
import org.springframework.data.ultipa.core.mapping.model.UltipaSystemProperty;
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
    EnumType getEnumeratedType();

    default EnumType getRequiredEnumeratedType() {
        EnumType enumeratedType = getEnumeratedType();
        if (enumeratedType == null) {
            throw new IllegalStateException(String.format("Required enumerated type not found for %s", getPropertyName()));
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
    String getBetweenEdge();

    @Nullable
    String getEdgeName();

    @Nullable
    Class<?> getEdgeClass();

    @Nullable
    Class<?> getReferenceType();

    @Nullable
    <E> Object getProperty(E entity);

    boolean isSystemProperty();

    @Nullable
    UltipaSystemProperty getSystemProperty();

    default UltipaSystemProperty getRequiredSystemProperty() {
        UltipaSystemProperty systemProperty = getSystemProperty();
        if (systemProperty == null) {
            throw new IllegalStateException(String.format("Required system property not found for %s", getPropertyName()));
        }
        return systemProperty;
    }

}
