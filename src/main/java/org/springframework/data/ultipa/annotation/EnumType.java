package org.springframework.data.ultipa.annotation;

/**
 * Defines mapping for enumerated types.  The constants of this
 * enumerated type specify how a persistent property or
 * field of an enumerated type should be persisted.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
public enum EnumType {

    /**
     * Use the name property of an enumeration
     */
    NAME,
    /**
     * Use the ordinal property of an enumeration
     */
    ORDINAL,
    /**
     * Use the other property of an enumeration, Find by {@link EnumId} annotation
     */
    FIELD,

}
