package org.springframework.data.ultipa.annotation;

/**
 * Cascading relationships between each schema.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
public enum CascadeType {

    /**
     * Cascade all operations
     */
    ALL,

    /**
     * Cascade persist operation
     */
    PERSIST,

    /**
     * Cascade merge operation
     */
    MERGE,

    /**
     * Cascade remove operation
     */
    REMOVE

}
