package io.github.xmljim.service.di.util;

/**
 * Specifies the lifetime of a provider instance
 */
public enum ServiceLifetime {
    /**
     * Creates a new instance of the provider every time it's requested
     */
    TRANSIENT,

    /**
     * Create and store a single instance of the provider that can be reused for subsequent requests
     */
    SINGLETON
}
