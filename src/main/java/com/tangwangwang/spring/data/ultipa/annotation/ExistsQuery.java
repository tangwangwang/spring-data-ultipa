package com.tangwangwang.spring.data.ultipa.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * Annotation to declare finder exists queries directly on repository methods.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Documented
@Query(exists = true)
public @interface ExistsQuery {

    /**
     * Takes an uql string to define the actual query to be executed. This one will take precedence over the
     * method name then. Alias for {@link Query#value}.
     *
     * @return empty {@link String} by default.
     */
    @AliasFor(annotation = Query.class)
    String value() default "";
}