package org.springframework.data.ultipa.annotation;

import java.lang.annotation.*;

/**
 * This annotation used to annotate the field of an Enum class, which is the identity value the enum instance.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnumId {
}
