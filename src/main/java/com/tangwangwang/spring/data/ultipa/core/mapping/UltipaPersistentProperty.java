package com.tangwangwang.spring.data.ultipa.core.mapping;

import com.tangwangwang.spring.data.ultipa.annotation.CascadeType;
import com.tangwangwang.spring.data.ultipa.annotation.EnumType;
import com.tangwangwang.spring.data.ultipa.annotation.FetchType;
import com.tangwangwang.spring.data.ultipa.annotation.PropertyType;
import com.tangwangwang.spring.data.ultipa.core.mapping.model.UltipaSystemProperty;
import org.springframework.data.mapping.PersistentProperty;
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

    /**
     * Returns whether it is a read-only property in the schema.
     *
     * @return Read-only properties return true, otherwise return false
     */
    boolean isReadonly();

    /**
     * Returns whether it is a json string property in the schema.
     *
     * @return Json string properties return true, otherwise return false
     */
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

    default Class<?> getRequiredReferenceType() {
        Class<?> referenceType = getReferenceType();
        if (referenceType == null) {
            throw new IllegalStateException(String.format("Required reference property not found for %s", getPropertyName()));
        }
        return referenceType;
    }

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
