package org.springframework.data.ultipa.core.mapping;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.data.mapping.model.BasicPersistentEntity;
import org.springframework.data.ultipa.annotation.CascadeType;
import org.springframework.data.ultipa.annotation.Edge;
import org.springframework.data.ultipa.annotation.Node;
import org.springframework.data.util.TypeInformation;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private final Map<CascadeType, List<UltipaPersistentProperty>> cascadeProperties = new HashMap<>();

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
    public void addPersistentProperty(UltipaPersistentProperty property) {
        super.addPersistentProperty(property);

        if (property.isReferenceProperty()) {
            List<CascadeType> cascadeTypes = property.getCascadeTypes();
            if (cascadeTypes.contains(CascadeType.ALL)) {
                cascadeProperties.computeIfAbsent(CascadeType.ALL, it -> new ArrayList<>()).add(property);
            } else {
                cascadeTypes.forEach(cascadeType -> cascadeProperties.computeIfAbsent(cascadeType, it -> new ArrayList<>()).add(property));
            }
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

    @Override
    public List<UltipaPersistentProperty> getCascadeProperty(CascadeType cascadeType) {
        return Stream.of(cascadeType, CascadeType.ALL)
                .map(cascadeProperties::get)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }
}
