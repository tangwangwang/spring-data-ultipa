package com.tangwangwang.spring.data.ultipa.core.exception;

/**
 * @author Wangwang Tang
 * @since 1.0
 */
public class ParameterBindingException extends UltipaPersistenceException {
    public ParameterBindingException() {
    }

    public ParameterBindingException(String message) {
        super(message);
    }
}
