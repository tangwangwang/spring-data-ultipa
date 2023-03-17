package org.springframework.data.ultipa.core.exception;

/**
 * @author Wangwang Tang
 * @since 1.0
 */
public class IncorrectResultSizeException extends UltipaPersistenceException {

    private final int expectedSize;

    private final int actualSize;


    /**
     * Constructor for IncorrectResultSizeDataAccessException.
     *
     * @param expectedSize the expected result size
     */
    public IncorrectResultSizeException(int expectedSize) {
        super("Incorrect result size: expected " + expectedSize);
        this.expectedSize = expectedSize;
        this.actualSize = -1;
    }

    /**
     * Constructor for IncorrectResultSizeDataAccessException.
     *
     * @param expectedSize the expected result size
     * @param actualSize   the actual result size (or -1 if unknown)
     */
    public IncorrectResultSizeException(int expectedSize, int actualSize) {
        super("Incorrect result size: expected " + expectedSize + ", actual " + actualSize);
        this.expectedSize = expectedSize;
        this.actualSize = actualSize;
    }

    /**
     * Constructor for IncorrectResultSizeDataAccessException.
     *
     * @param msg          the detail message
     * @param expectedSize the expected result size
     */
    public IncorrectResultSizeException(String msg, int expectedSize) {
        super(msg);
        this.expectedSize = expectedSize;
        this.actualSize = -1;
    }

    /**
     * Constructor for IncorrectResultSizeDataAccessException.
     *
     * @param msg          the detail message
     * @param expectedSize the expected result size
     * @param ex           the wrapped exception
     */
    public IncorrectResultSizeException(String msg, int expectedSize, Throwable ex) {
        super(msg, ex);
        this.expectedSize = expectedSize;
        this.actualSize = -1;
    }

    /**
     * Constructor for IncorrectResultSizeDataAccessException.
     *
     * @param msg          the detail message
     * @param expectedSize the expected result size
     * @param actualSize   the actual result size (or -1 if unknown)
     */
    public IncorrectResultSizeException(String msg, int expectedSize, int actualSize) {
        super(msg);
        this.expectedSize = expectedSize;
        this.actualSize = actualSize;
    }

    /**
     * Constructor for IncorrectResultSizeDataAccessException.
     *
     * @param msg          the detail message
     * @param expectedSize the expected result size
     * @param actualSize   the actual result size (or -1 if unknown)
     * @param ex           the wrapped exception
     */
    public IncorrectResultSizeException(String msg, int expectedSize, int actualSize, Throwable ex) {
        super(msg, ex);
        this.expectedSize = expectedSize;
        this.actualSize = actualSize;
    }


    /**
     * Return the expected result size.
     */
    public int getExpectedSize() {
        return this.expectedSize;
    }

    /**
     * Return the actual result size (or -1 if unknown).
     */
    public int getActualSize() {
        return this.actualSize;
    }

}
