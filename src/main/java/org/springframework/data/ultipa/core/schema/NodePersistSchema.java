package org.springframework.data.ultipa.core.schema;

import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A node persistent implementation of the ultipa schema.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
class NodePersistSchema extends AbstractPersistSchema implements NodeSchema {
    private static final String INSERT_UQL = "insert().into(%s).nodes({ %s }) as nodes return nodes{*}";
    private static final String UPDATE_UQL = "update().nodes({ %s }).set({ %s }) as nodes return nodes{*}";
    private final List<EdgeSchema> left = new ArrayList<>();
    private final List<EdgeSchema> right = new ArrayList<>();
    private @Nullable String systemId;

    NodePersistSchema() {
    }

    NodePersistSchema(Object source) {
        super(source);
    }

    @Override
    public void setSystemId(String id) {
        this.systemId = id;
    }

    @Nullable
    @Override
    public String getSystemId() {
        return systemId;
    }

    @Override
    public EdgeSchema left() {
        EdgePersistSchema left = new EdgePersistSchema();
        left(left);
        return left;
    }

    @Override
    public void left(EdgeSchema schema) {
        if (Objects.equals(schema.to(), this)) {
            addLeft(schema instanceof CycledEdgePersistSchema ? schema : new CycledEdgePersistSchema(schema));
        } else {
            addLeft(schema);
            schema.to(new CycledNodePersistSchema(this));
        }
    }

    @Override
    public EdgeSchema right() {
        EdgePersistSchema right = new EdgePersistSchema();
        right(right);
        return right;
    }

    @Override
    public void right(EdgeSchema schema) {
        if (Objects.equals(schema.from(), this)) {
            addRight(schema instanceof CycledEdgePersistSchema ? schema : new CycledEdgePersistSchema(schema));
        } else {
            addRight(schema);
            schema.from(new CycledNodePersistSchema(this));
        }
    }

    @Override
    public boolean hasLeft(EdgeSchema schema) {
        return this.left.contains(schema);
    }

    @Override
    public boolean hasRight(EdgeSchema schema) {
        return this.right.contains(schema);
    }

    private void addLeft(EdgeSchema schema) {
        if (!hasLeft(schema)) {
            this.left.add(schema);
        }
    }

    private void addRight(EdgeSchema schema) {
        if (!hasRight(schema)) {
            this.right.add(schema);
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
    public Stream<EdgeSchema> around() {
        return Stream.of(this.left, this.right).flatMap(Collection::stream);
    }

    @Override
    protected String getInsertIfAbsentUql() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected String getInsertUql() {
        String setterClause = Stream.of(getIdentifierSetterClause(), getPropertySetterClause())
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(SETTER_DELIMITER));

        return String.format(INSERT_UQL, getSchemaFilterClause(), setterClause);
    }

    @Override
    protected String getUpdateUql() {
        String filterClause = Stream.of(getSchemaFilterClause(), getIdentifierFilterClause())
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(FILTER_DELIMITER));

        return String.format(UPDATE_UQL, filterClause, getPropertySetterClause());
    }

}
