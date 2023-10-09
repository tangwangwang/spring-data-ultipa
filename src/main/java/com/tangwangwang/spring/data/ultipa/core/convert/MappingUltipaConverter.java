package com.tangwangwang.spring.data.ultipa.core.convert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tangwangwang.spring.data.ultipa.annotation.*;
import com.tangwangwang.spring.data.ultipa.core.UltipaOperations;
import com.tangwangwang.spring.data.ultipa.core.mapping.UltipaPersistentEntity;
import com.tangwangwang.spring.data.ultipa.core.mapping.UltipaPersistentProperty;
import com.tangwangwang.spring.data.ultipa.core.mapping.model.UltipaEnumTypeHolder;
import com.tangwangwang.spring.data.ultipa.core.mapping.model.UltipaPropertyTypeHolder;
import com.tangwangwang.spring.data.ultipa.core.mapping.model.UltipaSystemProperty;
import com.tangwangwang.spring.data.ultipa.core.proxy.UltipaProxy;
import com.tangwangwang.spring.data.ultipa.core.proxy.UltipaProxyFactory;
import com.tangwangwang.spring.data.ultipa.core.schema.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.expression.MapAccessor;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.geo.Point;
import org.springframework.data.mapping.InstanceCreatorMetadata;
import org.springframework.data.mapping.MappingException;
import org.springframework.data.mapping.Parameter;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.model.*;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.data.util.CustomCollections;
import org.springframework.data.util.StreamUtils;
import org.springframework.data.util.TypeInformation;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * {@link UltipaConverter} implementation uses a {@link MappingContext} to do sophisticated mapping of domain objects
 * to {@link Schema}.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
public class MappingUltipaConverter extends AbstractUltipaConverter implements ApplicationContextAware, BeanClassLoaderAware {

    private static final String INVALID_TYPE_TO_READ = "Expected to read Document %s into type %s but didn't find a PersistentEntity for the latter!";

    private final Map<Class<? extends IdGenerator<?>>, IdGenerator<?>> idGenerators = new ConcurrentHashMap<>();
    private final MappingContext<? extends UltipaPersistentEntity<?>, UltipaPersistentProperty> mappingContext;

    private final SpELContext spELContext = new SpELContext(new MapAccessor());
    private final UltipaProxyFactory proxyFactory;
    private final UltipaTypeMapper typeMapper;
    private final ObjectMapper objectMapper;
    private final SpelAwareProxyProjectionFactory projectionFactory;
    private @Nullable ApplicationContext applicationContext;
    private @Nullable AutowireCapableBeanFactory beanFactory;

    public MappingUltipaConverter(MappingContext<? extends UltipaPersistentEntity<?>, UltipaPersistentProperty> mappingContext) {
        this(mappingContext, null);
    }

    public MappingUltipaConverter(MappingContext<? extends UltipaPersistentEntity<?>, UltipaPersistentProperty> mappingContext, @Nullable ObjectMapper objectMapper) {
        super(new DefaultConversionService());

        Assert.notNull(mappingContext, "MappingContext must not be null!");

        this.mappingContext = mappingContext;
        this.typeMapper = UltipaTypeMapper.create(mappingContext);
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
        this.proxyFactory = new UltipaProxyFactory();
        this.projectionFactory = new SpelAwareProxyProjectionFactory();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        this.beanFactory = applicationContext.getAutowireCapableBeanFactory();
        this.projectionFactory.setBeanFactory(applicationContext);
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.projectionFactory.setBeanClassLoader(classLoader);
    }

    @Override
    public MappingContext<? extends UltipaPersistentEntity<?>, UltipaPersistentProperty> getMappingContext() {
        return mappingContext;
    }

    @Override
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R> R read(Class<R> type, Schema source) {
        return read(ClassTypeInformation.from((Class<R>) ClassUtils.getUserClass(type)), source);
    }

    @SuppressWarnings("unchecked")
    private <R> R read(TypeInformation<R> typeHint, Schema source) {

        TypeInformation<? extends R> typeToUse = typeMapper.readType(source, typeHint);
        Class<? extends R> rawType = typeToUse.getType();

        if (conversions.hasCustomReadTarget(source.getClass(), rawType)) {
            R r = conversionService.convert(source, rawType);
            Assert.notNull(r, "could not convert into object of class " + rawType);
            return r;
        }

        if (Schema.class.isAssignableFrom(rawType)) {
            return (R) source;
        }

        if (typeToUse.isMap()) {
            return (R) readMap(source);
        }

        if (typeToUse.equals(ClassTypeInformation.OBJECT)) {
            return (R) source;
        }

        // interface projection
        if (typeToUse.getType().isInterface()) {
            return readProjection(typeToUse.getType(), source);
        }

        UltipaPersistentEntity<?> entity = mappingContext.getPersistentEntity(typeToUse);

        if (entity == null) {
            throw new MappingException(String.format(INVALID_TYPE_TO_READ, source, typeToUse.getType()));
        }

        return readEntity((UltipaPersistentEntity<R>) entity, source);
    }

    @Override
    public Map<String, Object> readMap(Schema source) {
        return source.toMap();
    }

    @Override
    public List<Object> readArray(Schema source) {
        return source.toArray();
    }

    private <R> R readProjection(Class<R> type, Schema schema) {
        if (CustomCollections.isCollection(type)) {
            return projectionFactory.createProjection(type, schema.toArray());
        }
        return projectionFactory.createProjection(type, schema.toMap());
    }

    private <R> R readEntity(UltipaPersistentEntity<R> entity, Schema source) {
        SpELExpressionEvaluator evaluator = new DefaultSpELExpressionEvaluator(source, spELContext);
        SchemaAccessor accessor = new SchemaAccessor(source);

        InstanceCreatorMetadata<?> creatorMetadata = entity.getInstanceCreatorMetadata();

        ParameterValueProvider<UltipaPersistentProperty> provider = creatorMetadata != null
                && creatorMetadata.hasParameters() ? getParameterProvider(entity, accessor, evaluator)
                : NoOpParameterValueProvider.INSTANCE;

        EntityInstantiator instantiator = instantiators.getInstantiatorFor(entity);
        R instance = instantiator.createInstance(entity, provider);

        if (entity.requiresPropertyPopulation()) {
            UltipaPropertyValueProvider valueProvider = new UltipaPropertyValueProvider(accessor, evaluator);
            return populateSimpleProperties(entity, valueProvider, instance);
        }

        return instance;
    }

    private <R> R populateSimpleProperties(UltipaPersistentEntity<R> entity,
                                           PropertyValueProvider<UltipaPersistentProperty> valueProvider, R instance) {

        PersistentPropertyAccessor<R> accessor = new ConvertingPropertyAccessor<>(entity.getPropertyAccessor(instance),
                conversionService);

        for (UltipaPersistentProperty prop : entity) {
            // filter from property and to property
            if (prop.isReferenceProperty()) {
                continue;
            }

            // set property value
            accessor.setProperty(prop, valueProvider.getPropertyValue(prop));
        }
        return populateLazyLoadingProperties(entity, accessor.getBean());
    }

    private <R> R populateLazyLoadingProperties(UltipaPersistentEntity<R> entity, R instance) {
        PersistentPropertyAccessor<R> accessor = entity.getPropertyAccessor(instance);
        for (UltipaPersistentProperty prop : entity) {
            // Handling from property and to property and schema property
            if (prop.isReferenceProperty() && applicationContext != null) {
                UltipaOperations ultipaOperations = applicationContext.getBean(UltipaOperations.class);
                Object lazyLoadingProxy = this.proxyFactory.createLazyLoadingProxy(ultipaOperations, prop, instance);
                if (prop.getFetchType() == FetchType.EAGER) {
                    accessor.setProperty(prop, UltipaProxy.getTarget(lazyLoadingProxy));
                } else {
                    accessor.setProperty(prop, lazyLoadingProxy);
                }
            }
        }
        return accessor.getBean();
    }

    private ParameterValueProvider<UltipaPersistentProperty> getParameterProvider(
            UltipaPersistentEntity<?> entity, SchemaAccessor source, SpELExpressionEvaluator evaluator) {

        UltipaPropertyValueProvider provider = new UltipaPropertyValueProvider(source, evaluator);

        // noinspection ConstantConditions
        PersistentEntityParameterValueProvider<UltipaPersistentProperty> parameterProvider = new PersistentEntityParameterValueProvider<>(
                entity, provider, null);

        return new SpELExpressionParameterValueProvider<>(evaluator, conversionService, parameterProvider);
    }

    private Object handleJson(Object value, UltipaPersistentProperty property) {
        if (!property.isJson()) {
            return value;
        }
        try {
            return objectMapper.readValue(value.toString(), new TypeReference<Object>() {
                @Override
                public Type getType() {
                    return property.getRequiredField().getGenericType();
                }
            });
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(String.format("%s deserialize %s failed", value.getClass(),
                    property.getRequiredField().getGenericType()), e);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Nullable
    private Object handleEnumeration(Object value, UltipaPersistentProperty property) {
        if (!property.isEnumProperty()) {
            return value;
        }
        Class<? extends Enum> rawType = (Class<? extends Enum>) property.getType();
        EnumType enumeratedType = property.getRequiredEnumeratedType();
        switch (enumeratedType) {
            case FIELD:
                if (UltipaEnumTypeHolder.hasEnumField(rawType)) {
                    Field enumField = UltipaEnumTypeHolder.getRequiredEnumField(rawType);
                    Object enumFieldValue = ClassUtils.isAssignable(rawType, value.getClass()) ? value
                            : conversionService.convert(value, enumField.getType());

                    for (Enum constant : rawType.getEnumConstants()) {
                        PropertyAccessor accessor = new BeanWrapperImpl(constant);
                        Object propertyValue = accessor.getPropertyValue(enumField.getName());
                        if (propertyValue != null && Objects.equals(enumFieldValue, propertyValue)) {
                            return constant;
                        }
                    }
                    throw new IllegalArgumentException(String.format("'%s' cannot be converted to %s", value, rawType));
                } else {
                    return Enum.valueOf(rawType, value.toString());
                }
            case NAME:
                return Enum.valueOf(rawType, value.toString());
            case ORDINAL:
                return rawType.getEnumConstants()[Integer.parseInt(value.toString())];
            default:
                return null;
        }
    }

    @Nullable
    private UltipaPersistentEntity<?> getUltipaPersistentEntity(Object source) {
        Object target = source instanceof UltipaProxy ? ((UltipaProxy) source).getTarget() : source;
        if (target == null) {
            return null;
        }

        Class<?> entityType = ClassUtils.getUserClass(source.getClass());
        return mappingContext.getPersistentEntity(entityType);
    }

    @Override
    public void write(Object source, Schema sink) {
        UltipaPersistentEntity<?> entity = getUltipaPersistentEntity(source);
        if (entity == null) {
            return;
        }
        writeInternal(source, sink, entity);
    }

    private void writeInternal(Object source, Schema sink, UltipaPersistentEntity<?> entity) {
        PersistentPropertyAccessor<?> accessor = entity.getPropertyAccessor(source);

        for (UltipaPersistentProperty property : entity) {
            Object value = accessor.getProperty(property);
            writeProperty(source, sink, property, value);
        }
    }

    private void writeProperty(Object source, Schema sink, UltipaPersistentProperty property, @Nullable Object value) {
        if (property.isReferenceProperty()) {
            return;
        }

        if (!property.getOwner().isNew(source)) {
            if (property.isCreatedProperty() || property.isReadonly()) {
                return;
            }
        }

        if (property.isIdProperty() && sink instanceof PersistSchema) {
            writeIdValue(source, (PersistSchema) sink, property, value);
            return;
        }

        if (value != null && property.isEnumProperty()) {
            // noinspection unchecked
            Class<? extends Enum<?>> type = (Class<? extends Enum<?>>) property.getType();
            value = getPotentiallyConvertedEnumWrite(value, type, property.getRequiredEnumeratedType());
        }

        if (value != null && property.isJson()) {
            try {
                value = objectMapper.writeValueAsString(value);
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException(String.format("%s serialize %s failed.", value.getClass(),
                        property.getRequiredField().getGenericType()), e);
            }
        }

        Object convertedValue = getPotentiallyConvertedSimpleWrite(value, property.getPropertyType());

        String uqlValue = getPotentiallyConvertedUrlWrite(convertedValue, property.getPropertyType());
        sink.put(property.getPropertyName(), uqlValue);
    }

    private void writeIdValue(Object source, PersistSchema sink, UltipaPersistentProperty property, @Nullable Object idValue) {
        if (!property.isIdProperty()) {
            return;
        }
        sink.setIdName(property.getPropertyName());

        if (idValue != null) {
            sink.setIdValue(idValue);
            sink.setIsNew(false);
            writeIdValue(sink, property, idValue);
        } else {
            sink.setIsNew(true);
            idValue = computeId(property, source);
            writeIdValue(sink, property, idValue);
            Boolean isUniqueIdentifier = Optional.ofNullable(property.getSystemProperty())
                    .map(UltipaSystemProperty::isUniqueIdentifier)
                    .orElse(false);
            if (idValue != null || isUniqueIdentifier) {
                return;
            }
            throw new IllegalArgumentException(String.format("%s is the primary key of %s, neither generator reference nor generator class configured.",
                    property.getPropertyName(), property.getOwner().getSchemaName()));
        }
    }

    private void writeIdValue(PersistSchema sink, UltipaPersistentProperty property, @Nullable Object idValue) {
        if (idValue == null) {
            return;
        }
        Object convertedValue = getPotentiallyConvertedSimpleWrite(idValue, property.getPropertyType());
        String uqlValue = getPotentiallyConvertedUrlWrite(convertedValue, property.getPropertyType());
        sink.setIdValue(uqlValue);
        sink.put(property.getPropertyName(), uqlValue);
    }

    @Nullable
    private Object computeId(UltipaPersistentProperty idProperty, Object entity) {
        if (!idProperty.isIdProperty()) {
            return null;
        }

        IdGenerator<?> idGenerator = null;

        GeneratedValue generatedValueAnnotation = idProperty.findAnnotation(GeneratedValue.class);
        if (generatedValueAnnotation != null) {
            String idGeneratorRef = generatedValueAnnotation.generatorRef();
            if (StringUtils.hasText(idGeneratorRef) && this.applicationContext != null) {
                idGenerator = this.applicationContext.getBean(idGeneratorRef, IdGenerator.class);
            } else {
                Class<? extends IdGenerator<?>> idGeneratorClass = generatedValueAnnotation.generatorClass();
                if (idGeneratorClass == GeneratedValue.InternalIdGenerator.class) {
                    idGenerator = GeneratedValue.InternalIdGenerator.INSTANCE;
                } else if (idGeneratorClass == GeneratedValue.UUIDGenerator.class) {
                    idGenerator = GeneratedValue.UUIDGenerator.INSTANCE;
                } else {
                    idGenerator = this.idGenerators.computeIfAbsent(idGeneratorClass, this::createBeanOrInstantiate);
                }
            }
        }

        if (idGenerator == null) {
            if (idProperty.getActualType() == UUID.class) {
                idGenerator = GeneratedValue.UUIDGenerator.INSTANCE;
            } else {
                idGenerator = GeneratedValue.InternalIdGenerator.INSTANCE;
            }
        }

        return idGenerator.generateId(idProperty.getPropertyName(), entity);
    }

    @Override
    public void write(Object source, PersistSchema sink, CascadeType cascade) {
        sink.setSource(source);
        UltipaPersistentEntity<?> entity = getUltipaPersistentEntity(source);
        if (entity == null) {
            return;
        }

        if (!entity.isSchema()) {
            return;
        }

        if (entity.isEdge() && !(sink instanceof EdgeSchema)) {
            return;
        }

        if (entity.isNode() && !(sink instanceof NodeSchema)) {
            return;
        }

        writeReference(source, sink, entity, cascade);
    }

    private void writeReference(Object source, PersistSchema sink, UltipaPersistentEntity<?> entity, CascadeType cascade) {
        sink.setSchema(entity.getSchemaName());
        PersistentPropertyAccessor<?> accessor = entity.getPropertyAccessor(source);

        for (UltipaPersistentProperty property : entity) {
            Object value = accessor.getProperty(property);
            if (property.isReferenceProperty() && value != null) {
                writeReference(sink, property, value, cascade);
            }
        }
    }

    private void writeReference(PersistSchema sink, UltipaPersistentProperty property, Object value, CascadeType cascade) {
        UltipaPersistentEntity<?> entity = mappingContext.getPersistentEntity(property);
        if (entity == null) {
            return;
        }

        CascadeType propertyCascade = cascade;

        List<CascadeType> propertyCascadeTypes = property.getCascadeTypes();
        // property cascade not matched
        if (!CollectionUtils.containsAny(propertyCascadeTypes, Arrays.asList(cascade, CascadeType.ALL))) {
            propertyCascade = null;
        }

        // Uninitialized proxies are ignored
        if (value instanceof UltipaProxy && !((UltipaProxy) value).isInitialized()) {
            propertyCascade = null;
        }

        if (value.getClass().isArray()) {
            for (Object element : CollectionUtils.arrayToList(value)) {
                writeReference(sink, property, element, entity, propertyCascade);
            }
        } else if (value instanceof Iterable) {
            // noinspection unchecked
            for (Object element : ((Iterable<Object>) value)) {
                writeReference(sink, property, element, entity, propertyCascade);
            }
        } else if (CustomCollections.isCollection(value.getClass())) {
            // noinspection DataFlowIssue
            for (Object element : ((Collection<?>) value)) {
                writeReference(sink, property, element, entity, propertyCascade);
            }
        } else {
            writeReference(sink, property, value, entity, propertyCascade);
        }
    }

    private void writeReferenceReadonly(PersistSchema sink, UltipaPersistentProperty property, Object value, UltipaPersistentEntity<?> entity) {
        PersistSchema existsSchema = sink.find(entity.getSchemaName(), value);
        if (!(existsSchema instanceof NodeSchema) && existsSchema != null) {
            return;
        }
        NodeSchema node = Optional.ofNullable((NodeSchema) existsSchema).orElseGet(() -> {
            NodeSchema newNode = NodeSchema.of(value);
            newNode.setSchema(entity.getSchemaName());
            newNode.queried();
            return newNode;
        });

        if (sink instanceof NodeSchema) {
            String edgeName = property.getBetweenEdge();
            if (!StringUtils.hasText(edgeName)) {
                return;
            }

            if (property.isLeftProperty()) {
                ((NodeSchema) sink).left(edgeName).left(node);
            }

            if (property.isRightProperty()) {
                ((NodeSchema) sink).right(edgeName).right(node);
            }
        }

        if (sink instanceof EdgeSchema) {
            if (property.isFromProperty()) {
                ((EdgeSchema) sink).from(node);
            }

            if (property.isToProperty()) {
                ((EdgeSchema) sink).to(node);
            }
        }
    }

    private void writeReference(PersistSchema sink, UltipaPersistentProperty property, Object value, UltipaPersistentEntity<?> entity, @Nullable CascadeType cascade) {
        if (cascade == null) {
            writeReferenceReadonly(sink, property, value, entity);
            return;
        }

        // Update cascade does not persist new entity
        if (cascade == CascadeType.UPDATE && entity.isNew(value)) {
            return;
        }

        // Get the schema that already exists
        PersistSchema referenceSchema = sink.find(entity.getSchemaName(), value);
        if (referenceSchema != null) {
            writeReference(sink, property, referenceSchema);
            return;
        }

        String edgeName = property.getBetweenEdge();
        if (property.isFromProperty() || property.isLeftProperty()) {
            referenceSchema = StringUtils.hasText(edgeName) ? sink.left(edgeName).left() : sink.left();
        }

        if (property.isToProperty() || property.isRightProperty()) {
            referenceSchema = StringUtils.hasText(edgeName) ? sink.right(edgeName).right() : sink.right();
        }

        if (referenceSchema != null) {
            write(value, referenceSchema, cascade);
        }
    }

    private void writeReference(PersistSchema sink, UltipaPersistentProperty property, PersistSchema existsSchema) {
        String edgeName = property.getBetweenEdge();

        if (property.isFromProperty() || property.isLeftProperty()) {
            if (StringUtils.hasText(edgeName)) {
                sink.left(edgeName).left(existsSchema);
            } else {
                sink.left(existsSchema);
            }
        }

        if (property.isToProperty() || property.isRightProperty()) {
            if (StringUtils.hasText(edgeName)) {
                sink.right(edgeName).right(existsSchema);
            } else {
                sink.right(existsSchema);
            }
        }
    }

    @Nullable
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object getPotentiallyConvertedEnumWrite(Object value, Class<? extends Enum> type, EnumType enumType) {
        switch (enumType) {
            case FIELD:
                if (UltipaEnumTypeHolder.hasEnumField(type)) {
                    PropertyAccessor enumAccessor = new BeanWrapperImpl(value);
                    return enumAccessor.getPropertyValue(UltipaEnumTypeHolder.getRequiredEnumField(type).getName());
                }
                return ((Enum<?>) value).name();
            case NAME:
                return ((Enum<?>) value).name();
            case ORDINAL:
                return ((Enum<?>) value).ordinal();
        }
        return value;
    }

    @Nullable
    private Object getPotentiallyConvertedSimpleWrite(@Nullable Object value, @Nullable PropertyType propertyType) {
        if (value == null || propertyType == null) {
            return value;
        }
        switch (propertyType) {
            case STRING:
            case TEXT:
                return conversionService.convert(value, String.class);
            case INT32:
            case UINT32:
            case INT64:
            case UINT64:
            case FLOAT:
            case DOUBLE:
                return conversionService.convert(value, Number.class);
            case POINT:
                return conversionService.convert(value, Point.class);
            case DATETIME:
                return conversionService.convert(value, LocalDateTime.class);
            case TIMESTAMP:
                return conversionService.convert(value, Date.class);
            case STRING_ARRAY:
            case TEXT_ARRAY:
            case INT32_ARRAY:
            case UINT32_ARRAY:
            case INT64_ARRAY:
            case UINT64_ARRAY:
            case FLOAT_ARRAY:
            case DOUBLE_ARRAY:
            case DATETIME_ARRAY:
            case TIMESTAMP_ARRAY:
                PropertyType simpleType = UltipaPropertyTypeHolder.getSimpleType(propertyType);
                Class<?> valueClass = value.getClass();
                Stream<?> stream = null;
                if (valueClass.isArray()) {
                    stream = CollectionUtils.arrayToList(value).stream();
                }

                if (Iterable.class.isAssignableFrom(valueClass)) {
                    stream = StreamUtils.createStreamFromIterator(((Iterable<?>) value).iterator());
                }

                if (CustomCollections.isCollection(valueClass)) {
                    stream = ((Collection<?>) value).stream();
                }

                if (stream != null) {
                    return stream
                            .map(it -> getPotentiallyConvertedSimpleWrite(it, simpleType))
                            .collect(Collectors.toList());
                }
                return value;
            default:
                return value;
        }
    }

    private String getPotentiallyConvertedUrlWrite(@Nullable Object value, @Nullable PropertyType propertyType) {
        String nullValue = UltipaSystemProperty.NULL_VALUE;
        if (propertyType == null || value == null) {
            return nullValue;
        }
        switch (propertyType) {
            case STRING:
            case TEXT:
                return Optional.of(value)
                        .map(String::valueOf)
                        // because ultipa db interpret character \\t as \t, so need to replace \\t as \\\\t to keep character \\t
                        .map(v -> v.replace("\\", "\\\\"))
                        .map(v -> v.replace("\"", "\\\""))
                        .map(it -> String.format("\"%s\"", it))
                        .orElse(nullValue);
            case INT32:
            case UINT32:
            case INT64:
            case UINT64:
            case FLOAT:
            case DOUBLE:
                return Optional.of(value)
                        .map(it -> conversionService.convert(it, String.class))
                        .orElse(nullValue);
            case POINT:
            case DATETIME:
            case TIMESTAMP:
                return Optional.of(value)
                        .map(it -> conversionService.convert(it, String.class))
                        .map(it -> String.format("\"%s\"", it))
                        .orElse(nullValue);
            case STRING_ARRAY:
            case TEXT_ARRAY:
            case INT32_ARRAY:
            case UINT32_ARRAY:
            case INT64_ARRAY:
            case UINT64_ARRAY:
            case FLOAT_ARRAY:
            case DOUBLE_ARRAY:
            case DATETIME_ARRAY:
            case TIMESTAMP_ARRAY:
                Class<?> valueClass = value.getClass();
                Stream<?> stream = null;
                if (valueClass.isArray()) {
                    stream = CollectionUtils.arrayToList(value).stream();
                }

                if (Iterable.class.isAssignableFrom(valueClass)) {
                    stream = StreamUtils.createStreamFromIterator(((Iterable<?>) value).iterator());
                }

                if (CustomCollections.isCollection(valueClass)) {
                    stream = ((Collection<?>) value).stream();
                }

                if (stream != null) {
                    PropertyType simpleType = UltipaPropertyTypeHolder.getSimpleType(propertyType);
                    return stream
                            .map(it -> getPotentiallyConvertedUrlWrite(it, simpleType))
                            .collect(Collectors.joining(", ", "[ ", " ]"));
                }
                return nullValue;
            default:
                return nullValue;
        }
    }

    private <T> T createBeanOrInstantiate(Class<T> t) {
        T idGenerator;
        if (this.beanFactory == null) {
            idGenerator = BeanUtils.instantiateClass(t);
        } else {
            idGenerator = this.beanFactory.getBeanProvider(t).getIfUnique(() -> this.beanFactory.createBean(t));
        }
        return idGenerator;
    }

    /**
     * {@link PropertyValueProvider} to evaluate a SpEL expression if present on the property or simply accesses the field
     * of the configured source {@link Schema}.
     *
     * @author Wangwang Tang
     * @since 1.0
     */
    class UltipaPropertyValueProvider implements PropertyValueProvider<UltipaPersistentProperty> {

        final SchemaAccessor accessor;
        final SpELExpressionEvaluator evaluator;

        /**
         * Creates a new {@link UltipaPropertyValueProvider} for the given source and
         * {@link SpELExpressionEvaluator}
         *
         * @param accessor  must not be {@literal null}.
         * @param evaluator must not be {@literal null}.
         */
        UltipaPropertyValueProvider(SchemaAccessor accessor, SpELExpressionEvaluator evaluator) {

            Assert.notNull(accessor, "DocumentAccessor must no be null!");
            Assert.notNull(evaluator, "SpELExpressionEvaluator must not be null!");

            this.accessor = accessor;
            this.evaluator = evaluator;
        }

        /*
         * (non-Javadoc)
         * @see org.springframework.data.convert.PropertyValueProvider#getPropertyValue(org.springframework.data.mapping.PersistentProperty)
         */
        @SuppressWarnings("unchecked")
        @Nullable
        @Override
        public <T> T getPropertyValue(UltipaPersistentProperty property) {
            String expression = property.getSpelExpression();
            Object value = expression != null ? evaluator.evaluate(expression) : accessor.get(property);

            return (T) Optional.ofNullable(value)
                    // .map(v -> handlePropertyType(v, property))
                    .map(v -> handleJson(v, property))
                    .map(v -> handleEnumeration(v, property))
                    .orElse(null);
        }

    }

    enum NoOpParameterValueProvider implements ParameterValueProvider<UltipaPersistentProperty> {

        INSTANCE;

        @Override
        public <T> T getParameterValue(Parameter<T, UltipaPersistentProperty> parameter) {
            return null;
        }
    }
}
