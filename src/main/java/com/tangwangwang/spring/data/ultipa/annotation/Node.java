package com.tangwangwang.spring.data.ultipa.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.data.annotation.Persistent;

import java.lang.annotation.*;

/**
 * The annotation to configure the mapping from a node schema with a domain object.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
@Inherited
@Persistent
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Node {

    /**
     * @return See {@link #name()}.
     */
    @AliasFor("name")
    String value() default "";

    /**
     * @return The name of the node schema in the graph.
     */
    @AliasFor("value")
    String name() default "";

    /**
     * @return The description of the node schema in the graph.
     */
    String description() default "";

}
