package org.springframework.data.ultipa.core.mapping;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Reference;
import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.MappingException;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.mapping.model.AnnotationBasedPersistentProperty;
import org.springframework.data.mapping.model.FieldNamingStrategy;
import org.springframework.data.mapping.model.PropertyNameFieldNamingStrategy;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.ultipa.annotation.*;
import org.springframework.data.ultipa.core.mapping.model.UltipaSimpleTypeHolder;
import org.springframework.data.util.Lazy;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Ultipa specific {@link PersistentProperty} implementation.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
public class BasicUltipaPersistentProperty extends AnnotationBasedPersistentProperty<UltipaPersistentProperty> implements UltipaPersistentProperty {

    private final SimpleTypeHolder simpleTypeHolder;
    private final FieldNamingStrategy fieldNamingStrategy;
    private final Lazy<Boolean> isEnum = Lazy.of(() -> Enum.class.isAssignableFrom(getType()));
    private final Lazy<Boolean> isReference = Lazy.of(() -> isAnnotationPresent(Reference.class));
    private final Lazy<Boolean> isFrom = Lazy.of(() -> isAnnotationPresent(From.class));
    private final Lazy<Boolean> isTo = Lazy.of(() -> isAnnotationPresent(To.class));
    private final Lazy<Boolean> isLeft = Lazy.of(() -> isAnnotationPresent(Left.class));
    private final Lazy<Boolean> isRight = Lazy.of(() -> isAnnotationPresent(Right.class));
    private final Lazy<Boolean> isCreated = Lazy.of(() -> isAnnotationPresent(CreatedBy.class) || isAnnotationPresent(CreatedDate.class));

    public BasicUltipaPersistentProperty(org.springframework.data.mapping.model.Property property, UltipaPersistentEntity<?> owner,
                                         SimpleTypeHolder simpleTypeHolder, @Nullable FieldNamingStrategy fieldNamingStrategy) {
        super(property, owner, simpleTypeHolder);
        this.simpleTypeHolder = UltipaSimpleTypeHolder.HOLDER;
        this.fieldNamingStrategy = fieldNamingStrategy == null ? PropertyNameFieldNamingStrategy.INSTANCE : fieldNamingStrategy;

    }

    @Override
    protected Association<UltipaPersistentProperty> createAssociation() {
        return new Association<>(this, null);
    }

    @Override
    public UltipaPersistentEntity<?> getOwner() {
        return (UltipaPersistentEntity<?>) super.getOwner();
    }

    @Override
    public String getPropertyName() {
        String annotatedName = String.valueOf(getAnnotatedValue(Property.class));

        if (StringUtils.hasText(annotatedName)) {
            return annotatedName;
        }

        String fieldName = fieldNamingStrategy.getFieldName(this);

        if (!StringUtils.hasText(fieldName)) {
            throw new MappingException(String.format("Invalid (null or empty) field name returned for property %s by %s!", this, fieldNamingStrategy.getClass()));
        }

        return fieldName;
    }

    @Override
    public String getDescription() {
        return (String) getAnnotatedValue(Property.class, "description");
    }

    @Override
    public PropertyType getPropertyType() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isJson() {
        return !simpleTypeHolder.isSimpleType(getType()) && (boolean) getAnnotatedValue(Property.class, "json");
    }

    private Object getAnnotatedValue(Class<? extends Annotation> annotationType) {
        return getAnnotatedValue(annotationType, "value");
    }

    private Object getAnnotatedValue(Class<? extends Annotation> annotationType, String attributeName) {
        Annotation annotation = findAnnotation(annotationType);
        Object value = AnnotationUtils.getValue(annotation, attributeName);
        value = Optional.ofNullable(value).orElse(AnnotationUtils.getDefaultValue(annotationType, attributeName));
        if (value == null) {
            throw new IllegalArgumentException(String.format("%s has no attribute of %s!", annotationType, attributeName));
        }
        return value;
    }

    @Override
    public boolean isEnumProperty() {
        return isEnum.get();
    }

    @Override
    public EnumType getEnumeratedType() {
        return isEnumProperty() ? (EnumType) getAnnotatedValue(Enumerated.class) : null;
    }

    @Override
    public boolean isFromProperty() {
        return isFrom.get();
    }

    @Override
    public boolean isToProperty() {
        return isTo.get();
    }

    @Override
    public boolean isLeftProperty() {
        return isLeft.get();
    }

    @Override
    public boolean isRightProperty() {
        return isRight.get();
    }

    @Override
    public boolean isReferenceProperty() {
        return isReference.get();
    }

    @Override
    public boolean isCreatedProperty() {
        return isCreated.get();
    }

    @Override
    public List<CascadeType> getCascadeTypes() {
        if (isFromProperty()) {
            return Arrays.asList((CascadeType[]) getAnnotatedValue(From.class, "cascade"));
        }
        if (isToProperty()) {
            return Arrays.asList((CascadeType[]) getAnnotatedValue(To.class, "cascade"));
        }
        if (isLeftProperty()) {
            return Arrays.asList((CascadeType[]) getAnnotatedValue(Left.class, "cascade"));
        }
        if (isRightProperty()) {
            return Arrays.asList((CascadeType[]) getAnnotatedValue(Right.class, "cascade"));
        }
        return Collections.emptyList();
    }

    @Override
    public FetchType getFetchType() {
        if (isFromProperty()) {
            return (FetchType) getAnnotatedValue(From.class, "fetch");
        }
        if (isToProperty()) {
            return (FetchType) getAnnotatedValue(To.class, "fetch");
        }
        if (isLeftProperty()) {
            return (FetchType) getAnnotatedValue(Left.class, "fetch");
        }
        if (isRightProperty()) {
            return (FetchType) getAnnotatedValue(Right.class, "fetch");
        }
        return null;
    }

    @Override
    public String getBetweenSchemaName() {
        if (isLeftProperty()) {
            return getBetweenSchema(Left.class);
        }
        if (isRightProperty()) {
            return getBetweenSchema(Right.class);
        }
        return null;
    }

    @Override
    public Class<?> getReferenceType() {
        if (isReferenceProperty()) {
            return isCollectionLike() ? getComponentType() : getType();
        }
        return null;
    }

    @Nullable
    private String getBetweenSchema(Class<? extends Annotation> annotationType) {
        String between = (String) getAnnotatedValue(annotationType, "between");
        Class<?> betweenClass = (Class<?>) getAnnotatedValue(annotationType, "betweenClass");

        if (StringUtils.hasText(between) || betweenClass != void.class) {
            if (StringUtils.hasText(between)) {
                return between;
            }
            return resolverSchemaName(betweenClass);
        }
        return null;
    }

    private String resolverSchemaName(Class<?> type) {
        String defaultSchemaName = StringUtils.uncapitalize(type.getSimpleName());
        Node node = AnnotationUtils.findAnnotation(type, Node.class);
        Edge edge = AnnotationUtils.findAnnotation(type, Edge.class);
        if (node != null && edge != null) {
            throw new IllegalStateException(String.format("%s found multiple schema annotation type annotation exception.", getType()));
        }
        if (node != null && StringUtils.hasText(node.name())) {
            return node.name();
        }
        if (edge != null && StringUtils.hasText(edge.name())) {
            return edge.name();
        }
        return defaultSchemaName;
    }

    @Override
    public <E> Object getProperty(E entity) {
        PersistentPropertyAccessor<E> propertyAccessor = getOwner().getPropertyAccessor(entity);
        return propertyAccessor.getProperty(this);
    }
}
