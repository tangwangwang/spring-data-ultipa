package org.springframework.data.ultipa.core.schema;

import com.ultipa.sdk.operate.entity.Edge;
import com.ultipa.sdk.operate.entity.Node;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A representation of an Ultipa schema.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
public interface Schema {

    /**
     * Create a {@link Schema} from a {@link Map} containing key-value pairs.
     *
     * @param map source map containing key-value pairs. must not be {@literal null}.
     * @return a new {@link Schema}.
     */
    static Schema of(Map<String, Object> map) {
        Assert.notNull(map, "Map must not be null");

        return new MapSchema(new LinkedHashMap<>(map));
    }

    /**
     * Create a {@link Schema} from a {@link List} containing element.
     *
     * @param array source list containing element. must not be {@literal null}.
     * @return a new {@link Schema}.
     */
    static Schema of(List<Object> array) {
        Assert.notNull(array, "Array must not be null");

        return new ArraySchema(new ArrayList<>(array));
    }

    /**
     * Create a {@link Schema} from a {@link Node}.
     *
     * @param node source node. must not be {@literal null}.
     * @return a new {@link Schema}.
     */
    static Schema of(Node node) {
        Assert.notNull(node, "Node must not be null");

        return new MapSchema(node);
    }

    /**
     * Create a {@link Schema} from a {@link Edge}.
     *
     * @param edge source edge. must not be {@literal null}.
     * @return a new {@link Schema}.
     */
    static Schema of(Edge edge) {
        Assert.notNull(edge, "Edge must not be null");

        return new MapSchema(edge);
    }

    /**
     * Retrieve the identifier associated with this {@link Schema}.
     *
     * @return the name associated with this {@link Schema}.
     */
    @Nullable
    String getSchema();

    /**
     * Set the name for this {@link Schema}.
     *
     * @return This {@link Schema}, for chained calls.
     */
    Schema setSchema(String name);

    @Nullable
    Object put(Object key, @Nullable Object value);

    Object get(Object key);

    List<Object> toArray();

    Map<String, Object> toMap();
}
