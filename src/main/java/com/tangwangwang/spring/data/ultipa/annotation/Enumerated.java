package com.tangwangwang.spring.data.ultipa.annotation;


import java.lang.annotation.*;

import static com.tangwangwang.spring.data.ultipa.annotation.EnumType.FIELD;

/**
 * Annotation to define enumerated type for schema properties.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Enumerated {

    /**
     * (Optional) The type used in mapping an enum type.
     */
    EnumType value() default FIELD;
}
