package org.springframework.data.ultipa.core.proxy;

import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.ultipa.annotation.From;
import org.springframework.data.ultipa.annotation.To;
import org.springframework.data.ultipa.core.UltipaOperations;
import org.springframework.data.ultipa.core.mapping.UltipaPersistentEntity;
import org.springframework.data.ultipa.core.mapping.UltipaPersistentProperty;
import org.springframework.data.ultipa.core.query.Query;
import org.springframework.data.util.Lazy;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Wangwang Tang
 * @since 1.0
 */
abstract class AbstractUltipaProxy implements UltipaProxy {

    /**
     * Query the next nodes based on the give node, Sort by _uuid by default
     */
    protected static final String NODE_TO_NEXT_NODE_UQL = "n({ ${sourceIdKey} == #{sourceId} && @${sourceSchemaName} }).re({@${betweenSchemaName}}).n({@${targetSchemaName}} as nodes) return nodes{*} ORDER BY nodes._uuid";
    /**
     * Query the next edges based on the give node, Sort by _uuid by default
     */
    protected static final String NODE_TO_NEXT_EDGE_UQL = "find().nodes({ ${sourceIdKey} == #{sourceId} && @${sourceSchemaName} }) as node with node._id as id" +
            " find().edges({ _from == id && @${targetSchemaName} }) as edges return edges{*} ORDER BY edges._uuid";
    /**
     * Query the next node based on the give edge
     *
     * @see To
     */
    protected static final String EDGE_TO_NEXT_NODE_UQL = "find().edges({ ${sourceIdKey} == #{sourceId} && @${sourceSchemaName} }) as edge with edge._to as id" +
            " find().nodes({ _id == id && @${targetSchemaName} }) as node return node{*}";

    /**
     * Query the prev nodes based on the give node, Sort by _uuid by default
     */
    protected static final String NODE_TO_PREV_NODE_UQL = "n({ ${sourceIdKey} == #{sourceId} && @${sourceSchemaName} }).le({@${betweenSchemaName}}).n({@${targetSchemaName}} as nodes) return nodes{*} ORDER BY nodes._uuid";
    /**
     * Query the prev edges based on the give node, Sort by _uuid by default
     */
    protected static final String NODE_TO_PREV_EDGE_UQL = "find().nodes({ ${sourceIdKey} == #{sourceId} && @${sourceSchemaName} }) as node with node._id as id" +
            " find().edges({ _to == id && @${targetSchemaName} }) as edges return edges{*} ORDER BY edges._uuid";
    /**
     * Query the prev node based on the give edge
     *
     * @see From
     */
    protected static final String EDGE_TO_PREV_NODE_UQL = "find().edges({ ${sourceIdKey} == #{sourceId} && @${sourceSchemaName} }) as edge with edge._from as id" +
            " find().nodes({ _id == id && @${targetSchemaName} }) as node return node{*}";

    protected final UltipaOperations operations;
    protected final MappingContext<? extends UltipaPersistentEntity<?>, UltipaPersistentProperty> mappingContext;
    protected final @Nullable Object source;

    protected @Nullable Object target;
    protected final Class<?> sourceType;
    protected final Class<?> targetType;
    protected final @Nullable String betweenEdge;
    protected final Lazy<Boolean> isLeft;
    protected final Lazy<Boolean> isRight;
    protected final Lazy<Class<?>> targetClass;
    protected final Lazy<Object> sourceId;
    protected final Lazy<Field> field;
    protected transient boolean initialized = false;

    protected AbstractUltipaProxy(UltipaOperations operations, UltipaPersistentProperty property, @Nullable Object source) {
        this.operations = operations;
        this.mappingContext = operations.getConverter().getMappingContext();
        this.source = source;
        this.sourceType = property.getOwner().getType();
        this.targetType = property.getActualType();
        this.betweenEdge = property.getBetweenEdge();
        this.isLeft = Lazy.of(() -> property.isLeftProperty() || property.isFromProperty());
        this.isRight = Lazy.of(() -> property.isRightProperty() || property.isToProperty());
        this.targetClass = Lazy.of(property::getType);
        this.sourceId = Lazy.of(() -> source == null ? null : property.getOwner().getRequiredIdProperty().getProperty(source));
        this.field = Lazy.of(property::getRequiredField);
    }

    @Override
    public Object getTarget() {
        initialize();
        return target;
    }

    @Override
    public Object getSource() {
        return source;
    }

    @Override
    public Class<?> getTargetClass() {
        return this.targetClass.get();
    }

    protected Query createQuery() {
        UltipaPersistentEntity<?> sourceEntity = mappingContext.getRequiredPersistentEntity(this.sourceType);
        UltipaPersistentEntity<?> targetEntity = mappingContext.getRequiredPersistentEntity(this.targetType);
        Map<String, Object> paramMap = getParamMap(sourceEntity, targetEntity);
        if (isLeft.get()) {
            if (sourceEntity.isNode()) {
                if (targetEntity.isNode()) {
                    Assert.isTrue(StringUtils.hasText(betweenEdge), String.format("No between or betweenClass property found for schema annotation of schema field %s!", field.get()));
                    return operations.createQuery(NODE_TO_PREV_NODE_UQL, paramMap);
                }
                if (targetEntity.isEdge()) {
                    return operations.createQuery(NODE_TO_PREV_EDGE_UQL, paramMap);
                }
            }
            if (sourceEntity.isEdge() && targetEntity.isNode()) {
                return operations.createQuery(EDGE_TO_PREV_NODE_UQL, paramMap);
            }
        }
        if (isRight.get()) {
            if (sourceEntity.isNode()) {
                if (targetEntity.isNode()) {
                    Assert.isTrue(StringUtils.hasText(betweenEdge), String.format("No between or betweenClass property found for schema annotation of schema field %s!", field.get()));
                    return operations.createQuery(NODE_TO_NEXT_NODE_UQL, paramMap);
                }
                if (targetEntity.isEdge()) {
                    return operations.createQuery(NODE_TO_NEXT_EDGE_UQL, paramMap);
                }
            }
            if (sourceEntity.isEdge() && targetEntity.isNode()) {
                return operations.createQuery(EDGE_TO_NEXT_NODE_UQL, paramMap);
            }
        }
        throw new IllegalStateException("Unable to resolve matching uql for proxy object.");
    }

    private Map<String, Object> getParamMap(UltipaPersistentEntity<?> sourceEntity, UltipaPersistentEntity<?> targetEntity) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("sourceSchemaName", sourceEntity.getSchemaName());
        paramMap.put("betweenSchemaName", betweenEdge);
        paramMap.put("targetSchemaName", targetEntity.getSchemaName());
        paramMap.put("sourceIdKey", sourceEntity.getRequiredIdProperty().getPropertyName());
        paramMap.put("sourceId", sourceId.get());
        return paramMap;
    }

    protected final void initialize() {
        if (initialized) {
            return;
        }
        synchronized (this) {
            if (initialized) {
                return;
            }
            this.target = this.source == null ? null : getInitializeTarget();
            initialized = true;
        }
    }

    @Nullable
    protected abstract Object getInitializeTarget();

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public Object getSourceId() {
        return sourceId.get();
    }

}
