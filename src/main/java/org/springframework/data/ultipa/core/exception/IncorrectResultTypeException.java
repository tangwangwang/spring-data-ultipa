package org.springframework.data.ultipa.core.exception;

/**
 * @author Wangwang Tang
 * @since 1.0
 */
public class IncorrectResultTypeException extends UltipaPersistenceException {
    private final Class<?> expectedType;

    private final Class<?> actualType;

    public IncorrectResultTypeException(Class<?> expectedType, Class<?> actualType) {
        super("Incorrect result type: expected " + expectedType + ", actual " + actualType);
        this.expectedType = actualType;
        this.actualType = actualType;
    }

    public Class<?> getExpectedType() {
        return expectedType;
    }

    public Class<?> getActualType() {
        return actualType;
    }
}
