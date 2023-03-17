package org.springframework.data.ultipa.core.mapping;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.data.mapping.model.BasicPersistentEntity;
import org.springframework.data.ultipa.annotation.Edge;
import org.springframework.data.ultipa.annotation.Node;
import org.springframework.data.util.TypeInformation;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * Ultipa specific {@link UltipaPersistentEntity} implementation.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
public class BasicUltipaPersistentEntity<T> extends BasicPersistentEntity<T, UltipaPersistentProperty> implements UltipaPersistentEntity<T> {

    private final @Nullable Node node;

    private final @Nullable Edge edge;

    private final String schemaName;
    private final String description;

    public BasicUltipaPersistentEntity(TypeInformation<T> information) {
        super(information);

        Class<T> clazz = information.getType();

        String defaultSchemaName = StringUtils.uncapitalize(information.getType().getSimpleName());
        this.node = AnnotatedElementUtils.findMergedAnnotation(clazz, Node.class);
        this.edge = AnnotatedElementUtils.findMergedAnnotation(clazz, Edge.class);

        if (node != null && edge != null) {
            throw new IllegalStateException(String.format("%s found multiple schema annotation type annotation exception.", getType()));
        }

        if (node != null) {
            this.schemaName = StringUtils.hasText(node.name()) ? node.name() : defaultSchemaName;
            this.description = node.description();
        } else if (edge != null) {
            this.schemaName = StringUtils.hasText(edge.name()) ? edge.name() : defaultSchemaName;
            this.description = edge.description();
        } else {
            this.schemaName = defaultSchemaName;
            this.description = "";
        }
    }

    @Override
    public String getSchemaName() {
        return schemaName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean isNode() {
        return node != null;
    }

    @Override
    public boolean isEdge() {
        return edge != null;
    }
}
