package com.tangwangwang.spring.data.ultipa.core.schema;

import com.tangwangwang.spring.data.ultipa.core.mapping.model.UltipaSystemProperty;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An edge persistent implementation of the ultipa schema.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
class EdgePersistSchema extends AbstractPersistSchema implements EdgeSchema {
    private static final String INSERT_UQL = "insert().into(%s).edges({ %s }) as edges return edges{*}";
    private static final String UPDATE_UQL = "update().edges({ %s }).set({ %s }) as edges return edges{*}";
    private static final String INSERT_IF_ABSENT_UQL = "find().nodes(%s) as from find().nodes(%s) as to WITH from, to OPTIONAL n(from).re({ %s }).n(to) as paths with count(paths) as count where count == 0 " +
            "insert().into(%s).edges({ _from_uuid: from._uuid, _to_uuid: to._uuid })";
    private static final NodeSchema NONE_NODE = new NodePersistSchema();
    private NodeSchema from = NONE_NODE;
    private NodeSchema to = NONE_NODE;

    EdgePersistSchema() {
    }

    EdgePersistSchema(Object source) {
        super(source);
    }

    @Override
    public NodeSchema from() {
        if (this.from == NONE_NODE) {
            from(new NodePersistSchema());
        }
        return this.from;
    }

    @Override
    public void from(NodeSchema schema) {
        if (schema.hasRight(this)) {
            this.from = schema instanceof CycledNodePersistSchema ? schema : new CycledNodePersistSchema(schema);
        } else {
            this.from = schema;
            this.from.right(new CycledEdgePersistSchema(this));
        }
    }

    @Override
    public NodeSchema to() {
        if (this.to == NONE_NODE) {
            this.to(new NodePersistSchema());
        }
        return this.to;
    }

    @Override
    public void to(NodeSchema schema) {
        if (schema.hasLeft(this)) {
            this.to = schema instanceof CycledNodePersistSchema ? schema : new CycledNodePersistSchema(schema);
        } else {
            this.to = schema;
            this.to.left(new CycledEdgePersistSchema(this));
        }
    }

    @Override
    public PersistSchema find(String schema, Object source) {
        if (Objects.equals(getSchema(), schema) && Objects.equals(getSource(), source)) {
            return this;
        }
        return around()
                .filter(it -> it.find(schema, source) != null)
                .findAny()
                .orElse(null);
    }

    @Override
    public Stream<NodeSchema> around() {
        return Stream.of(this.from, this.to);
    }

    protected String getInsertIfAbsentUql() {
        Long fromUuid = from().getSystemUuid();
        Long toUuid = to().getSystemUuid();
        return String.format(INSERT_IF_ABSENT_UQL, fromUuid, toUuid, getSchemaFilterClause(), getSchemaFilterClause());
    }

    @Override
    protected String getInsertUql() {
        if (getIsNew() == null) {
            return getInsertIfAbsentUql();
        }

        String setterClause = Stream.of(getIdentifierSetterClause(), getSystemPropertySetterClause(), getPropertySetterClause())
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(SETTER_DELIMITER));

        return String.format(INSERT_UQL, getSchemaFilterClause(), setterClause);
    }

    @Override
    protected String getUpdateUql() {
        String filterClause = Stream.of(getSchemaFilterClause(), getIdentifierFilterClause())
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(FILTER_DELIMITER));
        String setterClause = Stream.of(getSystemPropertySetterClause(), getPropertySetterClause())
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(SETTER_DELIMITER));

        return String.format(UPDATE_UQL, filterClause, setterClause);
    }

    @Nullable
    protected String getSystemPropertySetterClause() {
        Map<String, Object> systemProperties = new HashMap<>();
        systemProperties.put(UltipaSystemProperty.FROM_UUID.getMappedName(), from.getSystemUuid());
        systemProperties.put(UltipaSystemProperty.TO_UUID.getMappedName(), to.getSystemUuid());
        systemProperties.put(UltipaSystemProperty.FROM.getMappedName(), String.format("\"%s\"", from.getSystemId()));
        systemProperties.put(UltipaSystemProperty.TO.getMappedName(), String.format("\"%s\"", to.getSystemId()));

        return systemProperties.entrySet().stream()
                .filter(it -> it.getValue() != null)
                .map(it -> String.format(SETTER_CLAUSE, it.getKey(), it.getValue()))
                .collect(Collectors.joining(SETTER_DELIMITER));
    }
}
