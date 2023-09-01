package org.springframework.data.ultipa.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * Annotation to define custom metadata for schema properties.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Documented
@Inherited
public @interface Property {

    /**
     * The key to be used to store the property inside the schema. Alias for {@link #name()}.
     *
     * @return an empty {@link String} by default.
     */
    @AliasFor("name")
    String value() default "";

    /**
     * The key to be used to store the property inside the schema. Alias for {@link #value()}.
     *
     * @return an empty {@link String} by default.
     */
    @AliasFor("value")
    String name() default "";

    /**
     * The actual desired target type the property should be stored as.
     *
     * @return {@link PropertyType#AUTO} by default.
     */
    PropertyType type() default PropertyType.AUTO;

    /**
     * Whether to use the Jackson serial number object
     *
     * @return Whether to serialize to json string
     */
    boolean json() default false;

    /**
     * Whether modification is not allowed, only read
     *
     * @return Whether only reads are allowed
     */
    boolean readonly() default false;

    /**
     * @return The description of the schema property in the graph.
     */
    String description() default "";

}
