package com.tangwangwang.spring.data.ultipa.core.exception;

/**
 * The basic exception class of Spring Data Ultipa
 *
 * @author Wangwang Tang
 * @since 1.0
 */
public abstract class UltipaPersistenceException extends RuntimeException {

    public UltipaPersistenceException() {
    }

    public UltipaPersistenceException(String message) {
        super(message);
    }

    public UltipaPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }

    public UltipaPersistenceException(Throwable cause) {
        super(cause);
    }

    public UltipaPersistenceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
