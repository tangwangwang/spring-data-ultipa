package org.springframework.data.ultipa.annotation;

import org.springframework.data.annotation.QueryAnnotation;

import java.lang.annotation.*;

/**
 * Annotation to provide UQL statements that will be used for executing the method.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@QueryAnnotation
@Documented
public @interface Query {

    /**
     * The custom UQL query to get executed and mapped back, if any return type is defined.
     */
    String value() default "";

    /**
     * The sort prefix for custom UQL statement, Only needed for methods returning sorts or sorts of pages based on custom queries.
     */
    String sortPrefix() default "";

    /**
     * @return whether the query defined should be executed as count projection.
     */
    boolean count() default false;

    /**
     * @return whether the query defined should be executed as exists projection.
     */
    boolean exists() default false;

}
