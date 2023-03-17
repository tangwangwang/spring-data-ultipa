package org.springframework.data.ultipa.core.convert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.expression.MapAccessor;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.mapping.InstanceCreatorMetadata;
import org.springframework.data.mapping.MappingException;
import org.springframework.data.mapping.Parameter;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.model.*;
import org.springframework.data.ultipa.annotation.*;
import org.springframework.data.ultipa.core.UltipaOperations;
import org.springframework.data.ultipa.core.mapping.UltipaPersistentEntity;
import org.springframework.data.ultipa.core.mapping.UltipaPersistentProperty;
import org.springframework.data.ultipa.core.mapping.model.UltipaEnumTypeHolder;
import org.springframework.data.ultipa.core.proxy.UltipaProxy;
import org.springframework.data.ultipa.core.proxy.UltipaProxyFactory;
import org.springframework.data.ultipa.core.schema.IdGenerator;
import org.springframework.data.ultipa.core.schema.Schema;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.data.util.TypeInformation;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link UltipaConverter} implementation uses a {@link MappingContext} to do sophisticated mapping of domain objects
 * to {@link Schema}.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
public class MappingUltipaConverter extends AbstractUltipaConverter implements ApplicationContextAware {

    private static final String INVALID_TYPE_TO_READ = "Expected to read Document %s into type %s but didn't find a PersistentEntity for the latter!";

    private final Map<Class<? extends IdGenerator<?>>, IdGenerator<?>> idGenerators = new ConcurrentHashMap<>();
    private final MappingContext<? extends UltipaPersistentEntity<?>, UltipaPersistentProperty> mappingContext;

    private final SpELContext spELContext = new SpELContext(new MapAccessor());
    private final UltipaProxyFactory proxyFactory;
    private final UltipaTypeMapper typeMapper;
    private final ObjectMapper objectMapper;
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
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        this.beanFactory = applicationContext.getAutowireCapableBeanFactory();
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

    @Nullable
    private Object handleNullValue(Object value, UltipaPersistentProperty property) {
        PropertyType propertyType = property.getPropertyType();
        if (propertyType == PropertyType.TIMESTAMP || propertyType == PropertyType.DATETIME
                ? ((Date) value).getTime() == 0
                : Objects.equals(propertyType.getNullValue(), value)) {
            return null;
        }
        return value;
    }

    @Nullable
    private Object handlePropertyType(Object value, UltipaPersistentProperty property) {
        if (property.getPropertyType() == PropertyType.UINT64) {
            return conversionService.convert(value, BigInteger.class);
        }
        return value;
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
        Enumerated.Type enumeratedType = property.getRequiredEnumeratedType();
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

    @Override
    public void write(Object source, Schema sink) {
        Object target = source instanceof UltipaProxy ? ((UltipaProxy) source).getTarget() : source;
        if (target == null) {
            return;
        }

        Class<?> entityType = ClassUtils.getUserClass(source.getClass());
        UltipaPersistentEntity<?> entity = mappingContext.getPersistentEntity(entityType);

        if (entity == null) {
            return;
        }

        writeInternal(source, sink, entity);
        sink.setCascadeTypes(Collections.singletonList(CascadeType.ALL));
    }

    @SuppressWarnings("unchecked")
    private void writeInternal(Object source, Schema sink, UltipaPersistentEntity<?> entity) {
        sink.setSource(source);
        sink.setName(entity.getSchemaName());
        PersistentPropertyAccessor<?> accessor = entity.getPropertyAccessor(source);

        List<UltipaPersistentProperty> referenceProperties = new ArrayList<>();
        for (UltipaPersistentProperty property : entity) {
            if (property.isReferenceProperty()) {
                referenceProperties.add(property);
                continue;
            }

            Object value = accessor.getProperty(property);

            if (value != null && property.isEnumProperty()) {
                value = getPotentiallyConvertedEnumWrite(value, property);
            }

            if (value != null && property.isJson()) {
                try {
                    value = objectMapper.writeValueAsString(value);
                } catch (JsonProcessingException e) {
                    throw new IllegalArgumentException(String.format("%s serialize %s failed.", value.getClass(),
                            property.getRequiredField().getGenericType()), e);
                }
            }

            if (property.isIdProperty()) {
                writeIdValue(source, property, sink, value);
                continue;
            }

            if (!entity.isNew(source) && property.isCreatedProperty()) {
                continue;
            }

            String convertedValue = getPotentiallyConvertedSimpleWrite(value, property.getPropertyType());
            sink.put(property.getPropertyName(), convertedValue);
        }

        for (UltipaPersistentProperty property : referenceProperties) {
            Object value = accessor.getProperty(property);
            if (value != null) {
                if (value.getClass().isArray()) {
                    Arrays.asList((Object[]) value).forEach(element -> writeReference(element, property, sink));
                } else if (value instanceof Iterable) {
                    ((Iterable<Object>) value).forEach(element -> writeReference(element, property, sink));
                } else {
                    writeReference(value, property, sink);
                }
            }
        }
    }

    private void writeIdValue(Object source, UltipaPersistentProperty property, Schema sink, @Nullable Object idValue) {
        if (!property.isIdProperty()) {
            return;
        }
        sink.setIdName(property.getPropertyName());

        if (idValue != null) {
            sink.setIdValue(idValue);
            sink.setNew(false);
        } else {
            sink.setNew(true);
            idValue = computeId(property, source);
            sink.setIdValue(idValue);
            if (idValue == null && !Arrays.asList(Schema.ID_FIELD_NAME, Schema.UUID_FIELD_NAME).contains(property.getPropertyName())) {
                throw new IllegalArgumentException(String.format("%s is the primary key of %s, neither generator reference nor generator class configured.",
                        property.getPropertyName(), property.getOwner().getSchemaName()));
            }
        }
    }


    private void writeReference(Object value, UltipaPersistentProperty property, Schema sink) {
        Class<?> referenceType = property.getReferenceType();

        if (CollectionUtils.isEmpty(property.getCascadeTypes()) || referenceType == null) {
            return;
        }

        UltipaPersistentEntity<?> entity = mappingContext.getPersistentEntity(referenceType);

        if (entity == null) {
            return;
        }

        String betweenSchemaName = property.getBetweenSchemaName();

        // Get the schema that already exists
        PersistentPropertyAccessor<?> accessor = entity.getPropertyAccessor(value);
        Object id = accessor.getProperty(entity.getRequiredIdProperty());
        Schema schema = sink.find(entity.getSchemaName(), id == null ? value : id);
        if (schema != null) {
            writeReference(property, sink, schema);
            return;
        }

        Schema referenceSchema = null;
        if (property.isFromProperty() || property.isLeftProperty()) {
            referenceSchema = StringUtils.hasText(betweenSchemaName) ? sink.left(betweenSchemaName).left() : sink.left();
        }

        if (property.isToProperty() || property.isRightProperty()) {
            referenceSchema = StringUtils.hasText(betweenSchemaName) ? sink.right(betweenSchemaName).right() : sink.right();
        }

        if (referenceSchema != null) {
            writeInternal(value, referenceSchema, entity);
            referenceSchema.setCascadeTypes(property.getCascadeTypes());
        }
    }

    private void writeReference(UltipaPersistentProperty property, Schema sink, Schema existsSchema) {
        String betweenSchemaName = property.getBetweenSchemaName();

        if (property.isFromProperty() || property.isLeftProperty()) {
            if (StringUtils.hasText(betweenSchemaName)) {
                sink.left(betweenSchemaName).left(existsSchema);
            } else {
                sink.left(existsSchema);
            }
        }

        if (property.isToProperty() || property.isRightProperty()) {
            if (StringUtils.hasText(betweenSchemaName)) {
                sink.right(betweenSchemaName).right(existsSchema);
            } else {
                sink.right(existsSchema);
            }
        }
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

    @Nullable
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object getPotentiallyConvertedEnumWrite(Object value, UltipaPersistentProperty property) {
        Class<? extends Enum> type = (Class<? extends Enum<?>>) property.getType();
        Enumerated.Type enumType = property.getRequiredEnumeratedType();
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

    private String getPotentiallyConvertedSimpleWrite(@Nullable Object value, PropertyType propertyType) {
        switch (propertyType) {
            case STRING:
            case TEXT:
                return Optional.ofNullable(value)
                        .map(v -> conversionService.convert(v, String.class))
                        .orElse(propertyType.getNullValue().toString());
            case DATETIME:
            case TIMESTAMP:
                return Optional.ofNullable(value)
                        .map(v -> conversionService.convert(v, LocalDateTime.class))
                        .map(d -> conversionService.convert(d, String.class))
                        .orElse(propertyType.getNullValue().toString());
            default:
                return Optional.ofNullable(value)
                        .map(v -> conversionService.convert(v, Number.class))
                        .map(String::valueOf)
                        .orElse(propertyType.getNullValue().toString());
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
                    .map(v -> handleNullValue(v, property))
                    .map(v -> handlePropertyType(v, property))
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
