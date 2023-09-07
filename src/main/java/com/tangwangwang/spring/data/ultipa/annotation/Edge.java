package com.tangwangwang.spring.data.ultipa.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.data.annotation.Persistent;

import java.lang.annotation.*;

/**
 * The annotation to configure the mapping from an edge schema with a domain object.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
@Inherited
@Persistent
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Edge {

    /**
     * @return See {@link #name()}.
     */
    @AliasFor("name")
    String value() default "";

    /**
     * @return The name of the edge schema in the graph.
     */
    @AliasFor("value")
    String name() default "";

    /**
     * @return The description of the edge schema in the graph.
     */
    String description() default "";

}
