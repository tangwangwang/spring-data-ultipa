package org.springframework.data.ultipa.annotation;

import org.springframework.data.annotation.Reference;

import java.lang.annotation.*;

/**
 * Annotation to define the left node of edge schema. Can only be used in edge schema.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Reference
public @interface From {

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
