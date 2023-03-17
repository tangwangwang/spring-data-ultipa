package org.springframework.data.ultipa.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * Annotation to declare finder count queries directly on repository methods
 *
 * @author Wangwang Tang
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Documented
@Query(count = true)
public @interface CountQuery {

    /**
     * Takes an uql string to define the actual query to be executed. This one will take precedence over the
     * method name then. Alias for {@link Query#value}.
     *
     * @return an empty String by default.
     */
    @AliasFor(annotation = Query.class)
    String value() default "";
}
