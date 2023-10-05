package com.tangwangwang.spring.data.ultipa.core;

import com.tangwangwang.spring.data.ultipa.annotation.CascadeType;
import com.tangwangwang.spring.data.ultipa.core.convert.UltipaConverter;
import com.tangwangwang.spring.data.ultipa.core.exception.QueryException;
import com.tangwangwang.spring.data.ultipa.core.mapping.UltipaMappingContext;
import com.tangwangwang.spring.data.ultipa.core.mapping.UltipaPersistentEntity;
import com.tangwangwang.spring.data.ultipa.core.mapping.UltipaPersistentProperty;
import com.tangwangwang.spring.data.ultipa.core.mapping.event.BeforeConvertCallback;
import com.tangwangwang.spring.data.ultipa.core.mapping.model.UltipaSystemProperty;
import com.tangwangwang.spring.data.ultipa.core.query.Query;
import com.tangwangwang.spring.data.ultipa.core.schema.EdgeSchema;
import com.tangwangwang.spring.data.ultipa.core.schema.NodeSchema;
import com.tangwangwang.spring.data.ultipa.core.schema.PersistSchema;
import com.tangwangwang.spring.data.ultipa.core.schema.Schema;
import com.tangwangwang.spring.data.ultipa.repository.support.UltipaEntityInformation;
import com.tangwangwang.spring.data.ultipa.repository.support.UltipaEntityInformationSupport;
import com.ultipa.Ultipa;
import com.ultipa.sdk.connect.Connection;
import com.ultipa.sdk.connect.conf.RequestConfig;
import com.ultipa.sdk.connect.driver.UltipaClientDriver;
import com.ultipa.sdk.operate.entity.*;
import com.ultipa.sdk.operate.exception.UqlExecutionException;
import com.ultipa.sdk.operate.response.Response;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mapping.callback.EntityCallbacks;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of {@link UltipaOperations} using the new Ultipa client.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
public class UltipaTemplate implements UltipaOperations, ApplicationContextAware, InitializingBean {

    private final static String ENTITY_MUST_NOT_BE_NULL = "Entity must not be null!";
    private final static String FIND_NODES_UQL = "find().nodes({ %s }) as nodes return nodes{*}";
    private final static String FIND_EDGES_UQL = "find().edges({ %s }) as edges return edges{*}";
    private final static String REMOVE_NODES_UQL = "delete().nodes({ %s })";
    private final static String REMOVE_EDGES_UQL = "delete().edges({ %s })";
    private static final SpelExpressionParser PARSER = new SpelExpressionParser(new SpelParserConfiguration(true, true));
    private final UltipaClientDriver clientDriver;
    private final MappingContext<? extends UltipaPersistentEntity<?>, UltipaPersistentProperty> mappingContext;
    private final UltipaConverter converter;
    private final boolean useLeader;
    private @Nullable EntityCallbacks entityCallbacks;

    public UltipaTemplate(UltipaClientDriver clientDriver, UltipaConverter converter) {
        this(clientDriver, converter, false);
    }

    public UltipaTemplate(UltipaClientDriver clientDriver, UltipaConverter converter, boolean useLeader) {

        Assert.notNull(clientDriver, "UltipaClientDriver is required");
        Assert.notNull(converter, "UltipaConverter is required");

        this.clientDriver = clientDriver;
        this.converter = converter;
        this.mappingContext = converter.getMappingContext();
        this.useLeader = useLeader;
    }

    @Override
    public Query createQuery(String uql) {
        return new AnonymityQuery(this, uql, PARSER, null, null);
    }

    @Override
    public Query createQuery(String uql, Map<String, Object> paramMap) {
        return new AnonymityQuery(this, uql, PARSER, paramMap, null);
    }

    @Override
    public Query createQuery(String uql, Sort sort) {
        return new AnonymityQuery(this, uql, PARSER, null, sort, null);
    }

    @Override
    public Query createQuery(String uql, Pageable pageable) {
        return new AnonymityQuery(this, uql, PARSER, null, pageable, null);
    }

    @Override
    public Query createQuery(String uql, Sort sort, @Nullable String sortPrefix) {
        return new AnonymityQuery(this, uql, PARSER, null, sort, sortPrefix);
    }

    @Override
    public Query createQuery(String uql, Pageable pageable, @Nullable String sortPrefix) {
        return new AnonymityQuery(this, uql, PARSER, null, pageable, sortPrefix);
    }


    @Override
    public Query createQuery(String uql, Map<String, Object> paramMap, Sort sort) {
        return new AnonymityQuery(this, uql, PARSER, paramMap, sort, null);
    }

    @Override
    public Query createQuery(String uql, Map<String, Object> paramMap, Pageable pageable) {
        return new AnonymityQuery(this, uql, PARSER, paramMap, pageable, null);
    }

    @Override
    public Query createQuery(String uql, Map<String, Object> paramMap, Sort sort, @Nullable String sortPrefix) {
        return new AnonymityQuery(this, uql, PARSER, paramMap, sort, sortPrefix);
    }

    @Override
    public Query createQuery(String uql, Map<String, Object> paramMap, Pageable pageable, @Nullable String sortPrefix) {
        return new AnonymityQuery(this, uql, PARSER, paramMap, pageable, sortPrefix);
    }

    @Override
    public <T> T save(T entity) {
        return doSave(entity, true);
    }

    @Override
    public <T> T update(T entity) {
        return doSave(entity, false);
    }

    @SuppressWarnings("unchecked")
    private <T> T doSave(T entity, boolean isNew) {
        Assert.notNull(entity, ENTITY_MUST_NOT_BE_NULL);

        Class<?> entityType = ClassUtils.getUserClass(entity);
        UltipaPersistentEntity<T> entityMetadata = (UltipaPersistentEntity<T>) mappingContext.getRequiredPersistentEntity(entityType);

        if (!entityMetadata.isSchema()) {
            throw new IllegalArgumentException(String.format("%s must be a valid Node or a valid Edge!", entityType.getName()));
        }

        if (isNew != entityMetadata.isNew(entity)) {
            String message = isNew ? "Entity must be new object!" : "Entity must not be new object!";
            throw new IllegalArgumentException(message);
        }
        return doSave(entity, entityMetadata, isNew ? CascadeType.PERSIST : CascadeType.UPDATE);
    }

    private <T> T doSave(T entity, UltipaPersistentEntity<T> entityMetadata, CascadeType cascade) {
        Schema savedSchema = null;
        if (entityMetadata.isNode()) {
            NodeSchema node = NodeSchema.of(entity);
            this.converter.write(entity, node, cascade);
            savedSchema = doSaveNode(node);
        }
        if (entityMetadata.isEdge()) {
            EdgeSchema edge = EdgeSchema.of(entity);
            this.converter.write(entity, edge, cascade);
            savedSchema = doSaveEdge(edge);
        }
        if (savedSchema == null) {
            throw new IllegalArgumentException(String.format("%s must be a valid Node or a valid Edge!", entityMetadata.getType().getName()));
        }

        return this.converter.read(entityMetadata.getType(), savedSchema);
    }

    private Schema doSaveNode(NodeSchema node) {
        if (node.isPersisted()) {
            return node;
        }

        // persist node
        Schema savedSchema = doSavePersistSchema(node);
        Long uuid = (Long) savedSchema.get(UltipaSystemProperty.UUID.getMappedName());
        node.setSystemUuid(uuid);
        String id = (String) savedSchema.get(UltipaSystemProperty.ID.getMappedName());
        node.setSystemId(id);
        node.persisted();

        // persist left and right
        node.around().forEach(this::doSaveEdge);

        return savedSchema;
    }

    private Schema doSaveEdge(EdgeSchema edge) {
        // persist from and to
        edge.around().forEach(this::doSaveNode);

        if (edge.isPersisted()) {
            return edge;
        }

        // persist edge
        Schema savedSchema = doSavePersistSchema(edge);
        Long uuid = (Long) savedSchema.get(UltipaSystemProperty.UUID.getMappedName());
        edge.setSystemUuid(uuid);
        edge.persisted();

        return savedSchema;
    }

    private Schema doSavePersistSchema(PersistSchema schema) {
        Object entity = maybeCallBeforeConvert(schema.getSource(), schema.getSchema());
        if (entity != null) {
            this.converter.write(entity, schema);
        }

        String uql = schema.toUqlString();
        List<Schema> result = doExecute(uql);

        if (schema.getIsNew() == null) {
            return schema;
        }

        if (result.isEmpty()) {
            throw new QueryException("Persist entity error.", uql);
        }
        return result.get(0);
    }

    @Nullable
    private <T> T maybeCallBeforeConvert(@Nullable T entity, @Nullable String schema) {
        if (entity == null) {
            return null;
        }

        if (entityCallbacks != null) {
            return entityCallbacks.callback(BeforeConvertCallback.class, entity, schema);
        }

        return entity;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> void remove(T entity) {
        Assert.notNull(entity, ENTITY_MUST_NOT_BE_NULL);

        Class<T> entityType = (Class<T>) ClassUtils.getUserClass(entity);
        UltipaEntityInformation<T, Object> information = UltipaEntityInformationSupport.getEntityInformation(entityType, this);
        Object id = information.getId(entity);
        if (id == null) {
            return;
        }

        String schemaFilter = "@" + information.getSchemaName();
        String idFilter = generateIdFilter(information, schemaFilter);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("id", id);
        if (information.isNode()) {
            createQuery(String.format(REMOVE_NODES_UQL, schemaFilter + " && " + idFilter), paramMap).execute();
        } else {
            createQuery(String.format(REMOVE_EDGES_UQL, schemaFilter + " && " + idFilter), paramMap).execute();
        }
    }

    @Override
    public <T> T getById(Object id, Class<T> entityClass) {
        UltipaEntityInformation<T, Object> information = UltipaEntityInformationSupport.getEntityInformation(entityClass, this);
        String schemaFilter = "@" + information.getSchemaName();
        String idFilter = generateIdFilter(information, schemaFilter);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("id", id);
        if (information.isNode()) {
            return createQuery(String.format(FIND_NODES_UQL, schemaFilter + " && " + idFilter), paramMap).findOne(entityClass);
        } else {
            return createQuery(String.format(FIND_EDGES_UQL, schemaFilter + " && " + idFilter), paramMap).findOne(entityClass);
        }
    }

    private <T> String generateIdFilter(UltipaEntityInformation<T, Object> information, String schemaFilter) {
        String idFilter;
        if (information.isSystemId()) {
            idFilter = information.getIdPropertyName() + " == #{id} ";
        } else {
            idFilter = schemaFilter + "." + information.getIdPropertyName() + " == #{id} ";
        }
        return idFilter;
    }

    @Override
    public <T> boolean existsById(Object id, Class<T> entityClass) {
        return getById(id, entityClass) != null;
    }

    @Override
    public <T> List<T> getAll(Class<T> entityClass) {
        UltipaEntityInformation<T, Object> information = UltipaEntityInformationSupport.getEntityInformation(entityClass, this);
        String schemaFilter = "@" + information.getSchemaName();

        if (information.isNode()) {
            return createQuery(String.format(FIND_NODES_UQL, schemaFilter)).findAll(entityClass);
        } else {
            return createQuery(String.format(FIND_EDGES_UQL, schemaFilter)).findAll(entityClass);
        }
    }

    @Override
    public void execute(String uql) {
        doExecute(uql);
    }

    @Override
    public <T> T findOne(String uql, Class<T> entityClass) {
        List<Schema> schemas = doExecute(uql);
        if (schemas.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(1, schemas.size());
        }
        return schemas.isEmpty() ? null : this.converter.read(entityClass, schemas.get(0));
    }

    @Override
    public <T> List<T> findAll(String uql, Class<T> entityClass) {
        List<Schema> schemas = doExecute(uql);
        return schemas.stream().map(schema -> this.converter.read(entityClass, schema)).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> findOne(String uql) {
        List<Schema> schemas = doExecute(uql);
        if (schemas.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(1, schemas.size());
        }
        return schemas.isEmpty() ? null : this.converter.readMap(schemas.get(0));
    }

    @Override
    public List<Map<String, Object>> findAll(String uql) {
        List<Schema> schemas = doExecute(uql);
        return schemas.stream().map(this.converter::readMap).collect(Collectors.toList());
    }

    @Override
    public List<Object> findArray(String uql) {
        List<Schema> schemas = doExecute(uql);
        if (schemas.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(1, schemas.size());
        }
        return schemas.isEmpty() ? null : this.converter.readArray(schemas.get(0));
    }

    @Override
    public List<List<Object>> findArrays(String uql) {
        List<Schema> schemas = doExecute(uql);
        return schemas.stream().map(this.converter::readArray).collect(Collectors.toList());
    }

    private List<Schema> doExecute(String uql) {
        try {
            Connection connection = clientDriver.getConnection();
            Response response;

            if (useLeader) {
                RequestConfig requestConfig = new RequestConfig();
                requestConfig.setUseMaster(true).setHost(connection.getLeader().getHost());
                response = connection.uql(uql, requestConfig);
            } else {
                response = connection.uql(uql);
            }

            if (response.getStatus().getErrorCode() != Ultipa.ErrorCode.SUCCESS) {
                throw new QueryException(String.format("error code: %s, message: %s", response.getStatus().getErrorCode(),
                        response.getStatus().getMsg()), uql);
            }

            if (CollectionUtils.isEmpty(response.getItems())) {
                return Collections.emptyList();
            }

            return response.getItems().values().stream()
                    .map(this::convertSchema)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        } catch (UqlExecutionException e) {
            throw new QueryException(e.getErrorMsg(), e, uql);
        }
    }

    private List<Schema> convertSchema(DataItem dataItem) {
        if (CollectionUtils.isEmpty(dataItem.getEntities())) {
            return Collections.emptyList();
        }
        List<Schema> result = new ArrayList<>();
        dataItem.getEntities().forEach(uqlEntity -> {
            if (uqlEntity instanceof Node) {
                result.add(Schema.of((Node) uqlEntity));
            }
            if (uqlEntity instanceof Edge) {
                result.add(Schema.of((Edge) uqlEntity));
            }
            if (uqlEntity instanceof Attr) {
                String name = ((Attr) uqlEntity).getName();
                List<Schema> mapList = ((Attr) uqlEntity).getValues().stream()
                        .map(value -> {
                            Map<String, Object> map = new HashMap<>();
                            map.put(name, value);
                            return Schema.of(map);
                        }).collect(Collectors.toList());
                result.addAll(mapList);
            }
            if (uqlEntity instanceof Table) {
                List<String> headers = ((Table) uqlEntity).getHeaders().stream().map(Header::getPropertyName).collect(Collectors.toList());
                List<Schema> mapList = ((Table) uqlEntity).getRows().stream()
                        .map(row -> {
                            Map<String, Object> map = new HashMap<>();
                            for (int index = 0; index < headers.size(); index++) {
                                if (index < row.size()) {
                                    map.put(headers.get(index), row.get(index));
                                }
                            }
                            return Schema.of(map);
                        })
                        .collect(Collectors.toList());
                result.addAll(mapList);
            }
            if (uqlEntity instanceof UqlArray) {
                ((UqlArray) uqlEntity).forEach(element -> result.add(Schema.of(element)));
            }
            if (uqlEntity instanceof Path) {
                List<Schema> schemas = Stream.concat(
                        ((Path) uqlEntity).getNodes().stream().map(Schema::of),
                        ((Path) uqlEntity).getEdges().stream().map(Schema::of)
                ).collect(Collectors.toList());
                result.addAll(schemas);
            }
        });
        return result;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (entityCallbacks == null) {
            EntityCallbacks entityCallbacks = EntityCallbacks.create(applicationContext);
            Assert.notNull(entityCallbacks, "EntityCallbacks must not be null!");
            this.entityCallbacks = entityCallbacks;
        }
    }

    @Override
    public void afterPropertiesSet() {
        if (this.mappingContext instanceof UltipaMappingContext) {
            ((UltipaMappingContext) this.mappingContext).initializeStructure(this);
        }
    }

    @Override
    public UltipaConverter getConverter() {
        return converter;
    }

    static class AnonymityQuery extends Query {

        protected AnonymityQuery(UltipaOperations operations, String queryString, @Nullable ExpressionParser parser,
                                 @Nullable Map<String, Object> paramMap, @Nullable String sortPrefix) {
            super(operations, queryString, parser, paramMap, sortPrefix);
        }

        protected AnonymityQuery(UltipaOperations operations, String queryString, @Nullable ExpressionParser parser,
                                 @Nullable Map<String, Object> paramMap, Pageable pageable, @Nullable String sortPrefix) {
            super(operations, queryString, parser, paramMap, pageable, sortPrefix);
        }

        protected AnonymityQuery(UltipaOperations operations, String queryString, @Nullable ExpressionParser parser,
                                 @Nullable Map<String, Object> paramMap, Sort sort, @Nullable String sortPrefix) {
            super(operations, queryString, parser, paramMap, sort, sortPrefix);
        }
    }
}
