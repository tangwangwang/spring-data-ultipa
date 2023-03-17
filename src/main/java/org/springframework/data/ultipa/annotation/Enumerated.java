package org.springframework.data.ultipa.annotation;


import java.lang.annotation.*;

/**
 * Annotation to define enumerated type for schema properties.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Enumerated {

    Type value() default Type.FIELD;

    enum Type {
        /**
         * Use the name property of an enumeration
         */
        NAME,
        /**
         * Use the ordinal property of an enumeration
         */
        ORDINAL,
        /**
         * Use the other property of an enumeration, Find by {@link EnumValue} annotation
         */
        FIELD,
    }
}
