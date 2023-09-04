package com.tangwangwang.spring.data.ultipa.core.exception;

/**
 * @author Wangwang Tang
 * @since 1.0
 */
public class QueryException extends UltipaPersistenceException {

    private final String uql;

    public QueryException(String message, String uql) {
        super(message);
        this.uql = uql;
    }

    public QueryException(String message, Throwable cause, String uql) {
        super(message, cause);
        this.uql = uql;
    }

    public String getUql() {
        return uql;
    }
}