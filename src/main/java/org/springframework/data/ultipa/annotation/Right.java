package org.springframework.data.ultipa.annotation;

import org.springframework.data.annotation.Reference;

import java.lang.annotation.*;

/**
 * Annotation to define the right node or edge of node schema. Can only be used in node schema.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Reference
public @interface Right {

    /**
     * (Optional) Represents the edge schema name between two node schemas, or the node schema name between
     * two edge schemas.
     * The between class no longer persists any properties, Only to connect to the node or edge.
     */
    String edge() default "";

    /**
     * (Optional) Type-safe alternative to {@link #edge()} for specifying the between schema name in two nodes or two edges.
     * The between class no longer persists any properties, Only to connect to the node or edge.
     */
    Class<?> edgeClass() default void.class;

    /**
     * (Optional) The operations that must be cascaded to the target of the association.
     * <p> By default no operations are cascaded.
     */
    CascadeType[] cascade() default {};

    /**
     * (Optional) Whether the association should be lazily loaded or must be eagerly fetched. The EAGER strategy is a
     * requirement on the persistence provider runtime that the associated entities must be eagerly fetched.
     * The LAZY strategy is a hint to the persistence provider runtime.
     */
    FetchType fetch() default FetchType.LAZY;

}
