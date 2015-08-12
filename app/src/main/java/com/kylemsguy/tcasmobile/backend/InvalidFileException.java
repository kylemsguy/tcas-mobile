package com.kylemsguy.tcasmobile.backend;

/**
 * Thrown when an invalid file is given.
 */
public class InvalidFileException extends Exception {
    /**
     * Constructs a new {@code Exception} that includes the current stack trace.
     */
    public InvalidFileException() {
    }

    /**
     * Constructs a new {@code Exception} with the current stack trace and the
     * specified detail message.
     *
     * @param detailMessage the detail message for this exception.
     */
    public InvalidFileException(String detailMessage) {
        super(detailMessage);
    }

    /**
     * Constructs a new {@code Exception} with the current stack trace, the
     * specified detail message and the specified cause.
     *
     * @param detailMessage the detail message for this exception.
     * @param throwable
     */
    public InvalidFileException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    /**
     * Constructs a new {@code Exception} with the current stack trace and the
     * specified cause.
     *
     * @param throwable the cause of this exception.
     */
    public InvalidFileException(Throwable throwable) {
        super(throwable);
    }
}
