package io.github.xmljim.service.di;

import io.github.xmljim.service.di.annotations.Generated;

/**
 * Core exception class for service manager
 */
public class ServiceManagerException extends RuntimeException {
    /**
     * Constructor
     */
    @Generated
    public ServiceManagerException() {
    }

    /**
     * Constructor
     * @param message the message
     */
    @Generated
    public ServiceManagerException(String message) {
        super(message);
    }

    /**
     * Create a formatted exception message
     * @param message the message template
     * @param args    the parameterized values
     */
    @Generated
    public ServiceManagerException(String message, Object... args) {
        this(message.formatted(args));
    }

    /**
     * Constructor
     * @param message message
     * @param cause   cause
     */
    @Generated
    public ServiceManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor
     * @param cause cause
     */
    @Generated
    public ServiceManagerException(Throwable cause) {
        super(cause);
    }
}
