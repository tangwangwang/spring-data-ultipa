package org.springframework.data.ultipa.annotation;

/**
 * The type of fetched data.
 *
 * @author Wangwang Tang
 * @since 1.0
 */
public enum FetchType {

    /**
     * Defines that data can be lazily fetched.
     */
    LAZY,

    /**
     * Defines that data must be eagerly fetched.
     */
    EAGER

}
