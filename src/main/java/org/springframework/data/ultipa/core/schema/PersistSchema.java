package org.springframework.data.ultipa.core.schema;

import org.springframework.data.ultipa.annotation.CascadeType;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.*;

/**
 * @author Wangwang Tang
 * @since 1.0
 */
abstract class PersistSchema extends MapSchema {

    protected @Nullable Object source;
    protected @Nullable String idName;
    protected @Nullable Object idValue;
    protected boolean isNew;
    protected List<CascadeType> cascadeTypes = Collections.emptyList();

    final UUID index;
    final Map<UUID, PersistSchema> graphSet;

    PersistSchema() {
        graphSet = new HashMap<>();
        index = UUID.randomUUID();
        graphSet.put(index, this);
    }

    public PersistSchema(Map<UUID, PersistSchema> graphSet) {
        this.graphSet = graphSet;
        index = UUID.randomUUID();
        graphSet.put(index, this);
    }

    @Override
    public String getIdName() {
        return idName;
    }

    @Override
    public void setIdName(@Nullable String idName) {
        this.idName = idName;
    }

    @Override
    public Object getIdValue() {
        return this.idValue;
    }

    @Override
    public void setIdValue(@Nullable Object idValue) {
        this.idValue = idValue;
    }

    @Override
    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    @Override
    public void setSource(Object source) {
        this.source = source;
    }

    @Override
    public void setCascadeTypes(List<CascadeType> cascadeTypes) {
        this.cascadeTypes = cascadeTypes;
    }

    @Override
    @Nullable
    public Schema find(String name, Object idValue) {
        for (PersistSchema schema : graphSet.values()) {
            if (Objects.equals(schema.name, name) && Objects.equals(schema.idValue, idValue)) {
                return schema;
            }
            if (schema.source == idValue) {
                return schema;
            }
        }
        return null;
    }

    public String toUql() {
        if (!isNew) {
            return toUpdateUql();
        }

        List<PersistSchema> insertSchemas = new ArrayList<>();
        this.collectInsertSchemas(insertSchemas);

        Map<UUID, Integer> returnIndexMap = new HashMap<>();
        List<String> edges = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        int returnIndex = 1;
        for (PersistSchema insertSchema : insertSchemas) {
            if (insertSchema instanceof EdgeSchema) {
                EdgeSchema edgeSchema = (EdgeSchema) insertSchema;
                // Remove duplicate edges
                String edgeId = edgeSchema.fromSchema.index.toString() + edgeSchema.toSchema.index;
                if (edges.contains(edgeId)) {
                    continue;
                }

                if (!returnIndexMap.containsKey(edgeSchema.fromSchema.index)) {
                    if (edgeSchema.fromSchema.isNew) {
                        sb.append(edgeSchema.fromSchema.toInsertUql(returnIndex));
                    } else {
                        sb.append(edgeSchema.fromSchema.toQueryUql(returnIndex));
                    }
                    returnIndexMap.put(edgeSchema.fromSchema.index, returnIndex);
                    returnIndex++;
                }

                if (!returnIndexMap.containsKey(edgeSchema.toSchema.index)) {
                    if (edgeSchema.toSchema.isNew) {
                        sb.append(edgeSchema.toSchema.toInsertUql(returnIndex));
                    } else {
                        sb.append(edgeSchema.toSchema.toQueryUql(returnIndex));
                    }
                    returnIndexMap.put(edgeSchema.toSchema.index, returnIndex);
                    returnIndex++;
                }

                if (edgeSchema.isNew) {
                    sb.append(edgeSchema.toInsertUql(returnIndex, returnIndexMap.get(edgeSchema.fromSchema.index), returnIndexMap.get(edgeSchema.toSchema.index)));
                    returnIndexMap.put(edgeSchema.index, returnIndex);
                    returnIndex++;
                }
                edges.add(edgeId);
            }

            if (insertSchema instanceof NodeSchema) {
                NodeSchema nodeSchema = (NodeSchema) insertSchema;
                if (!returnIndexMap.containsKey(nodeSchema.index)) {
                    if (nodeSchema.isNew) {
                        sb.append(nodeSchema.toInsertUql(returnIndex));
                    } else {
                        sb.append(nodeSchema.toQueryUql(returnIndex));
                    }
                    returnIndexMap.put(nodeSchema.index, returnIndex);
                    returnIndex++;
                }
            }
        }


        sb.append(" return r").append(returnIndexMap.get(index)).append("{*}");
        returnIndexMap.values().stream()
                .filter(i -> !Objects.equals(i, returnIndexMap.get(index)))
                .forEach(i -> sb.append(", r").append(i).append("{*}"));

        return sb.toString();
    }

    abstract String toUpdateUql();

    abstract void collectInsertSchemas(List<PersistSchema> insertSchemas);

    static class NodeSchema extends PersistSchema {
        private final List<EdgeSchema> prevSchemas = new ArrayList<>();
        private final List<EdgeSchema> nextSchemas = new ArrayList<>();

        public NodeSchema() {
        }

        public NodeSchema(Map<UUID, PersistSchema> graphSet) {
            super(graphSet);
        }

        @Override
        public Schema left() {
            EdgeSchema schema = new EdgeSchema(graphSet, new NodeSchema(graphSet), this);
            prevSchemas.add(schema);
            return schema;
        }

        @Override
        public void left(Schema leftSchema) {
            if (leftSchema instanceof EdgeSchema) {
                graphSet.remove(((EdgeSchema) leftSchema).toSchema.index);
                ((EdgeSchema) leftSchema).toSchema = this;
            }
        }

        @Override
        public Schema right() {
            EdgeSchema schema = new EdgeSchema(graphSet, this, new NodeSchema(graphSet));
            nextSchemas.add(schema);
            return schema;
        }

        @Override
        public void right(Schema rightSchema) {
            if (rightSchema instanceof EdgeSchema) {
                graphSet.remove(((EdgeSchema) rightSchema).fromSchema.index);
                ((EdgeSchema) rightSchema).fromSchema = this;
            }
        }

        String toInsertUql(int index) {
            StringBuilder sb = new StringBuilder();
            sb.append(" insert().into(@")
                    .append(name)
                    .append(").nodes({");
            if (idValue != null) {
                sb.append(" ").append(idName).append(": ").append(toUqlValue(idValue)).append(",");
            }
            this.forEach((key, value) -> sb.append(" ").append(key).append(": ").append(toUqlValue(value)).append(","));
            if (sb.charAt(sb.length() - 1) == ',') {
                sb.deleteCharAt(sb.length() - 1);
            }
            sb.append(" }) as r").append(index).append(" with r").append(index);
            return sb.toString();
        }

        String toUpdateUql() {
            Assert.notNull(name, "");
            Assert.notNull(idName, "");
            Assert.notNull(idValue, "");
            StringBuilder sb = new StringBuilder();
            sb.append("update().nodes({@").append(name).append(" && ")
                    .append(idName).append(" == ").append(toUqlValue(idValue))
                    .append("}).set({");
            this.forEach((key, value) -> sb.append(" ").append(key).append(": ").append(toUqlValue(value)).append(","));
            if (sb.charAt(sb.length() - 1) == ',') {
                sb.deleteCharAt(sb.length() - 1);
            }
            sb.append(" }) as node return node{*}");
            return sb.toString();
        }

        String toQueryUql(int index) {
            Assert.notNull(name, "");
            Assert.notNull(idName, "");
            Assert.notNull(idValue, "");
            return String.format(" find().nodes({ @%s && @%s.%s == %s }) as r%s with r%s ", name, name, idName, toUqlValue(idValue), index, index);
        }

        void collectInsertSchemas(List<PersistSchema> insertSchemas) {
            if (insertSchemas.stream().anyMatch(schema -> Objects.equals(schema.index, this.index))) {
                return;
            }

            if (cascadeTypes.contains(CascadeType.PERSIST) || cascadeTypes.contains(CascadeType.ALL)) {
                insertSchemas.add(this);
                if (isNew) {
                    prevSchemas.forEach(edgeSchema -> edgeSchema.collectInsertSchemas(insertSchemas));
                    nextSchemas.forEach(edgeSchema -> edgeSchema.collectInsertSchemas(insertSchemas));
                }
            }
        }

    }

    static class EdgeSchema extends PersistSchema {
        private NodeSchema fromSchema;
        private NodeSchema toSchema;

        public EdgeSchema() {
            this.fromSchema = new NodeSchema(graphSet);
            this.toSchema = new NodeSchema(graphSet);
        }

        public EdgeSchema(Map<UUID, PersistSchema> graphSet, NodeSchema fromSchema, NodeSchema toSchema) {
            super(graphSet);
            this.fromSchema = fromSchema;
            this.toSchema = toSchema;
        }

        @Override
        public Schema left() {
            return fromSchema;
        }

        @Override
        public void left(Schema leftSchema) {
            if (leftSchema instanceof NodeSchema) {
                graphSet.remove(fromSchema.index);
                fromSchema = (NodeSchema) leftSchema;
            }
        }

        @Override
        public Schema right() {
            return toSchema;
        }

        @Override
        public void right(Schema rightSchema) {
            if (rightSchema instanceof NodeSchema) {
                graphSet.remove(toSchema.index);
                toSchema = (NodeSchema) rightSchema;
            }
        }

        String toInsertUql(int index, int fromIndex, int toIndex) {
            StringBuilder sb = new StringBuilder();

            sb.append(" insert().into(@")
                    .append(name)
                    .append(").edges({");
            if (idValue != null) {
                sb.append(" ").append(idName).append(": ").append(toUqlValue(idValue)).append(",");
            }
            this.forEach((key, value) -> sb.append(" ").append(key).append(": ").append(toUqlValue(value)).append(","));
            sb.append(" ").append(FROM_UUID_FIELD_NAME).append(": r").append(fromIndex).append("._uuid,");
            sb.append(" ").append(TO_UUID_FIELD_NAME).append(": r").append(toIndex).append("._uuid");
            sb.append(" }) as r").append(index).append(" with r").append(index);
            return sb.toString();
        }

        String toUpdateUql() {
            Assert.notNull(name, "");
            Assert.notNull(idName, "");
            Assert.notNull(idValue, "");
            StringBuilder sb = new StringBuilder();
            sb.append("update().edges({@").append(name).append(" && ")
                    .append(idName).append(" == ").append(toUqlValue(idValue))
                    .append("}).set({");
            this.forEach((key, value) -> sb.append(" ").append(key).append(": ").append(toUqlValue(value)).append(","));
            if (sb.charAt(sb.length() - 1) == ',') {
                sb.deleteCharAt(sb.length() - 1);
            }
            sb.append(" }) as edge return edge{*}");
            return sb.toString();
        }

        String queryUql(int index) {
            Assert.notNull(name, "");
            Assert.notNull(idName, "");
            Assert.notNull(idValue, "");
            return String.format(" find().edges({ @%s && @%s.%s == %s }) as r%s with r%s ", name, name, idName, toUqlValue(idValue), index, index);
        }

        void collectInsertSchemas(List<PersistSchema> insertSchemas) {
            if (insertSchemas.stream().anyMatch(schema -> Objects.equals(schema.index, this.index)) || !isNew) {
                return;
            }

            if (cascadeTypes.contains(CascadeType.PERSIST) || cascadeTypes.contains(CascadeType.ALL)) {
                insertSchemas.add(this);
                fromSchema.collectInsertSchemas(insertSchemas);
                toSchema.collectInsertSchemas(insertSchemas);
            }
        }
    }

    protected String toUqlValue(Object value) {
        if (value instanceof Number) {
            return value.toString();
        }
        return String.format("'%s'", value);
    }

}
