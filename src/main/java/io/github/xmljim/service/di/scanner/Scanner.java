package io.github.xmljim.service.di.scanner;

import io.github.xmljim.service.di.registry.ServiceRegistry;
import io.github.xmljim.service.di.util.ClassFilter;

import java.util.function.Predicate;

/**
 * Scanner to locate all services
 */
@SuppressWarnings({"unused", "SameReturnValue"})
public interface Scanner {

    String getName();

    /**
     * Scan and register services
     * @param serviceRegistry The service registry to append services
     * @return {@code true} if the scanner completes successfully
     */
    boolean scan(ServiceRegistry serviceRegistry);

    /**
     * The service class filter to apply for the services to register
     * @return a {@link Predicate} holding the criteria to evaluate for each service class
     */
    ClassFilter getServiceClassFilter();

    /**
     * The provider class filter to apply for the providers to append to a service
     * @return a {@link Predicate} holding the criteria to evaluate for each provider class
     */
    ClassFilter getProviderClassFilter();

    /**
     * Enforce assignability
     * @return enforce assignability
     */
    boolean enforceProviderAssignableFromService();
}
