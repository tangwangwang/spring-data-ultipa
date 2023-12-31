package com.tangwangwang.spring.data.ultipa.core.mapping;

import com.tangwangwang.spring.data.ultipa.annotation.PropertyType;
import com.tangwangwang.spring.data.ultipa.core.UltipaOperations;
import com.tangwangwang.spring.data.ultipa.core.mapping.model.UltipaProperty;
import com.tangwangwang.spring.data.ultipa.core.mapping.model.UltipaSchema;
import com.tangwangwang.spring.data.ultipa.core.mapping.model.UltipaSimpleTypeHolder;
import com.tangwangwang.spring.data.ultipa.core.query.Query;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mapping.context.AbstractMappingContext;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.model.FieldNamingStrategy;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.PropertyNameFieldNamingStrategy;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.util.TypeInformation;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Default implementation of a {@link MappingContext} for Ultipa using {@link BasicUltipaPersistentEntity} and
 * {@link BasicUltipaPersistentProperty} as primary abstractions.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
public class UltipaMappingContext extends AbstractMappingContext<UltipaPersistentEntity<?>, UltipaPersistentProperty> {

    private static final String SHOW_NODE_SCHEMA_UQL = "show().node_schema()";
    private static final String SHOW_EDGE_SCHEMA_UQL = "show().edge_schema()";
    private static final String SHOW_NODE_PROPERTY_UQL = "show().node_property()";
    private static final String SHOW_EDGE_PROPERTY_UQL = "show().edge_property()";
    private static final String CREATE_NODE_SCHEMA_UQL = "create().node_schema(\"%s\", \"%s\")";
    private static final String CREATE_EDGE_SCHEMA_UQL = "create().edge_schema(\"%s\", \"%s\")";
    private static final String CREATE_NODE_PROPERTY_UQL = "create().node_property(@%s, \"%s\", \"%s\", \"%s\")";
    private static final String CREATE_EDGE_PROPERTY_UQL = "create().edge_property(@%s, \"%s\", \"%s\", \"%s\")";

    private static final FieldNamingStrategy DEFAULT_NAMING_STRATEGY = PropertyNameFieldNamingStrategy.INSTANCE;
    private FieldNamingStrategy fieldNamingStrategy = DEFAULT_NAMING_STRATEGY;
    private boolean validate;
    private boolean generate;

    private final Map<String, UltipaSchema> nodeSchemas = new HashMap<>();
    private final Map<String, UltipaSchema> edgeSchemas = new HashMap<>();

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

    /**
     * Configure whether to validate schemas and properties. Defaults is false.
     *
     * @param validate Whether to validate schemas and properties
     */
    public void setValidate(@Nullable Boolean validate) {
        this.validate = validate != null && validate;
    }

    /**
     * Configure whether to automatically generate schemas and properties that do not exist. Defaults is false.
     *
     * @param generate Whether to auto generate schemas and properties
     */
    public void setGenerate(@Nullable Boolean generate) {
        this.generate = generate != null && generate;
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
        UltipaPersistentProperty persistentProperty = new BasicUltipaPersistentProperty(property, owner, simpleTypeHolder, fieldNamingStrategy);

        if (!owner.isSchema()) {
            return persistentProperty;
        }

        if (!generate && !validate) {
            return persistentProperty;
        }

        if (persistentProperty.isSystemProperty()) {
            return persistentProperty;
        }

        if (persistentProperty.isReferenceProperty()) {

            if (persistentProperty.getEdgeClass() != null) {
                addPersistentEntity(persistentProperty.getEdgeClass());
            }

            String edgeName = persistentProperty.getEdgeName();
            if (StringUtils.hasText(edgeName)) {
                edgeSchemas.computeIfAbsent(edgeName, UltipaSchema::of);
            }

            return persistentProperty;
        }

        if (owner.isNode()) {
            UltipaSchema schema = nodeSchemas.computeIfAbsent(owner.getSchemaName(), UltipaSchema::of);
            schema.addProperty(persistentProperty.getPropertyName(), persistentProperty.getPropertyType(), persistentProperty.getDescription());
        }

        if (owner.isEdge()) {
            UltipaSchema schema = edgeSchemas.computeIfAbsent(owner.getSchemaName(), UltipaSchema::of);
            schema.addProperty(persistentProperty.getPropertyName(), persistentProperty.getPropertyType(), persistentProperty.getDescription());
        }

        return persistentProperty;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.mapping.BasicMappingContext#createPersistentEntity(org.springframework.data.util.TypeInformation, org.springframework.data.mapping.model.MappingContext)
     */
    @Override
    protected <T> UltipaPersistentEntity<T> createPersistentEntity(TypeInformation<T> typeInformation) {
        BasicUltipaPersistentEntity<T> persistentEntity = new BasicUltipaPersistentEntity<>(typeInformation);

        if (!persistentEntity.isSchema()) {
            return persistentEntity;
        }

        if (!generate && !validate) {
            return persistentEntity;
        }

        String schemaName = persistentEntity.getSchemaName();
        if (persistentEntity.isNode() && !nodeSchemas.containsKey(schemaName)) {
            nodeSchemas.put(schemaName, UltipaSchema.of(schemaName, persistentEntity.getDescription()));
        }

        if (persistentEntity.isEdge() && !edgeSchemas.containsKey(schemaName)) {
            edgeSchemas.put(schemaName, UltipaSchema.of(schemaName, persistentEntity.getDescription()));
        }

        return persistentEntity;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        super.setApplicationContext(applicationContext);
    }

    public void initializeStructure(UltipaOperations operations) {
        if (!validate && !generate) {
            return;
        }

        // initialize
        generateAndValidateNodeSchema(operations);
        generateAndValidateEdgeSchema(operations);
    }

    private void generateAndValidateNodeSchema(UltipaOperations operations) {
        Map<String, UltipaSchema> persistentSchemas = initializeQuerySchema(operations, SHOW_NODE_SCHEMA_UQL);
        initializeQueryProperty(operations, persistentSchemas, SHOW_NODE_PROPERTY_UQL);

        nodeSchemas.forEach((schemaName, schema) -> {
            if (!persistentSchemas.containsKey(schemaName)) {

                if (generate) {
                    Query query = operations.createQuery(String.format(CREATE_NODE_SCHEMA_UQL, schema.getName(), schema.getDescription()));
                    query.execute();
                } else if (validate) {
                    throw new IllegalArgumentException(String.format("Can't find node schema for '%s'.", schemaName));
                }

            }

            schema.forEach((propertyName, property) -> {
                UltipaSchema persistentSchema = persistentSchemas.getOrDefault(schemaName, UltipaSchema.of(schemaName));
                if (persistentSchema.containsKey(propertyName)) {
                    UltipaProperty persistentProperty = persistentSchema.get(propertyName);
                    if (validate && persistentProperty.getType() != property.getType()) {
                        throw new IllegalArgumentException(String.format("The node property type for '%s.%s' do not match: expected %s, actual %s.",
                                schemaName, propertyName, persistentProperty.getType().getMappedName(), property.getType().getMappedName()));
                    }
                } else if (generate) {
                    Query query = operations.createQuery(String.format(CREATE_NODE_PROPERTY_UQL, schemaName, propertyName, property.getType().getMappedName(), property.getDescription()));
                    query.execute();
                } else if (validate) {
                    throw new IllegalArgumentException(String.format("Can't find node property for '%s.%s'.", schemaName, propertyName));
                }
            });
        });
    }

    private void generateAndValidateEdgeSchema(UltipaOperations operations) {
        Map<String, UltipaSchema> persistentSchemas = initializeQuerySchema(operations, SHOW_EDGE_SCHEMA_UQL);
        initializeQueryProperty(operations, persistentSchemas, SHOW_EDGE_PROPERTY_UQL);

        edgeSchemas.forEach((schemaName, schema) -> {
            if (!persistentSchemas.containsKey(schemaName)) {

                if (generate) {
                    Query query = operations.createQuery(String.format(CREATE_EDGE_SCHEMA_UQL, schema.getName(), schema.getDescription()));
                    query.execute();
                } else if (validate) {
                    throw new IllegalArgumentException(String.format("Can't find edge schema for '%s'.", schemaName));
                }

            }

            schema.forEach((propertyName, property) -> {
                UltipaSchema persistentSchema = persistentSchemas.getOrDefault(schemaName, UltipaSchema.of(schemaName));
                if (persistentSchema.containsKey(propertyName)) {
                    UltipaProperty persistentProperty = persistentSchema.get(propertyName);
                    if (validate && persistentProperty.getType() != property.getType()) {
                        throw new IllegalArgumentException(String.format("The edge property type for '%s.%s' do not match: expected %s, actual %s.",
                                schemaName, propertyName, persistentProperty.getType().getMappedName(), property.getType().getMappedName()));
                    }
                } else if (generate) {
                    Query query = operations.createQuery(String.format(CREATE_EDGE_PROPERTY_UQL, schemaName, propertyName, property.getType().getMappedName(), property.getDescription()));
                    query.execute();
                } else if (validate) {
                    throw new IllegalArgumentException(String.format("Can't find edge property for '%s.%s'.", schemaName, propertyName));
                }
            });
        });
    }

    private Map<String, UltipaSchema> initializeQuerySchema(UltipaOperations operations, String querySchemaUql) {
        Query query = operations.createQuery(querySchemaUql);
        List<AnonymitySchema> schemas = query.findAll(AnonymitySchema.class);
        return schemas.stream().collect(Collectors.toMap(AnonymitySchema::getName,
                schema -> UltipaSchema.of(schema.getName(), schema.getDescription())));
    }

    private void initializeQueryProperty(UltipaOperations operations, Map<String, UltipaSchema> schemas, String queryPropertyUql) {
        Query query = operations.createQuery(queryPropertyUql);
        List<AnonymityProperty> properties = query.findAll(AnonymityProperty.class);
        properties.forEach(property -> {
            UltipaSchema schema = schemas.computeIfAbsent(property.getSchema(), UltipaSchema::of);
            schema.addProperty(property.getName(), PropertyType.resolve(property.getType()), property.getDescription());
        });
    }

    interface AnonymitySchema {
        String getName();

        String getDescription();
    }

    interface AnonymityProperty {
        String getName();

        String getType();

        String getDescription();

        String getSchema();
    }

}
