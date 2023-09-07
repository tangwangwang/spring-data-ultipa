package com.tangwangwang.spring.data.ultipa.annotation;

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
     * Cascade update operation
     */
    UPDATE,

    /**
     * Cascade remove operation
     */
    REMOVE

}
