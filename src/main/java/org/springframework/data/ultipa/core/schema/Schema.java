package org.springframework.data.ultipa.core.schema;

import com.ultipa.sdk.operate.entity.Edge;
import com.ultipa.sdk.operate.entity.Node;
import org.springframework.data.ultipa.annotation.CascadeType;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.*;

/**
 * A representation of an Ultipa schema as extended {@link Map}.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
public interface Schema {

    String ID_FIELD_NAME = "_id";
    String UUID_FIELD_NAME = "_uuid";
    String FROM_FIELD_NAME = "_from";
    String TO_FIELD_NAME = "_to";
    String FROM_UUID_FIELD_NAME = "_from_uuid";
    String TO_UUID_FIELD_NAME = "_to_uuid";

    /**
     * Create a new {@link Schema} containing key-value pairs.
     *
     * @return a new {@link Schema}.
     */
    static Schema createMap() {
        return new MapSchema();
    }

    /**
     * Create a new node {@link Schema}.
     *
     * @return a new {@link Schema}.
     */
    static Schema createNode() {
        return new PersistSchema.NodeSchema();
    }

    /**
     * Create a new edge {@link Schema}.
     *
     * @return a new {@link Schema}.
     */
    static Schema createEdge() {
        return new PersistSchema.EdgeSchema();
    }

    /**
     * Create a {@link Schema} from a {@link Map} containing key-value pairs.
     *
     * @param map source map containing key-value pairs. must not be {@literal null}.
     * @return a new {@link Schema}.
     */
    static Schema from(Map<String, Object> map) {
        Assert.notNull(map, "Map must not be null");

        return new MapSchema(new LinkedHashMap<>(map));
    }

    /**
     * Create a {@link Schema} from a {@link List} containing element.
     *
     * @param array source list containing element. must not be {@literal null}.
     * @return a new {@link Schema}.
     */
    static Schema from(List<Object> array) {
        Assert.notNull(array, "Array must not be null");

        return new ArraySchema(new ArrayList<>(array));
    }

    /**
     * Create a {@link Schema} from a {@link Node}.
     *
     * @param node source node. must not be {@literal null}.
     * @return a new {@link Schema}.
     */
    static Schema from(Node node) {
        Assert.notNull(node, "Node must not be null");

        return new MapSchema(node);
    }

    /**
     * Create a {@link Schema} from a {@link Edge}.
     *
     * @param edge source edge. must not be {@literal null}.
     * @return a new {@link Schema}.
     */
    static Schema from(Edge edge) {
        Assert.notNull(edge, "Edge must not be null");

        return new MapSchema(edge);
    }


    /**
     * Retrieve the identifier associated with this {@link Schema}.
     * <p>
     * The default implementation throws {@link UnsupportedOperationException}.
     *
     * @return the name associated with this {@link Schema}.
     */
    default String getName() {
        throw new UnsupportedOperationException();
    }

    /**
     * Set the name for this {@link Schema}.
     * <p>
     * The default implementation throws {@link UnsupportedOperationException}.
     *
     * @return This {@link Schema}, for chained calls.
     */
    default Schema setName(String name) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    default String getIdName() {
        throw new UnsupportedOperationException();
    }

    default void setIdName(String idName) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    default Object getIdValue() {
        throw new UnsupportedOperationException();
    }

    default void setIdValue(@Nullable Object idValue) {
        throw new UnsupportedOperationException();
    }

    default void setNew(boolean isNew) {
        throw new UnsupportedOperationException();
    }

    default void setSource(Object source) {
        throw new UnsupportedOperationException();
    }

    default String toUql() {
        throw new UnsupportedOperationException();
    }

    @Nullable
    default Schema find(String name, Object idValue) {
        throw new UnsupportedOperationException();
    }

    default Schema left() {
        throw new UnsupportedOperationException();
    }

    default void left(Schema leftSchema) {
        throw new UnsupportedOperationException();
    }

    default Schema left(String name) {
        Schema left = left();
        left.setCascadeTypes(Collections.singletonList(CascadeType.ALL));
        left.setNew(true);
        return left.setName(name);
    }

    default Schema right() {
        throw new UnsupportedOperationException();
    }

    default void right(Schema rightSchema) {
        throw new UnsupportedOperationException();
    }

    default Schema right(String name) {
        Schema right = right();
        right.setCascadeTypes(Collections.singletonList(CascadeType.ALL));
        right.setNew(true);
        return right.setName(name);
    }

    default void setCascadeTypes(List<CascadeType> cascadeTypes) {
        throw new UnsupportedOperationException();
    }

    default Set<Map.Entry<String, Object>> entrySet() {
        throw new UnsupportedOperationException();
    }

    default Object put(String key, Object value) {
        throw new UnsupportedOperationException();
    }

    default Object get(Object key) {
        throw new UnsupportedOperationException();
    }

    default List<Object> toArray() {
        throw new UnsupportedOperationException();
    }

    default Map<String, Object> toMap() {
        throw new UnsupportedOperationException();
    }
}
