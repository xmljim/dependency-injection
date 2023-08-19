package io.github.xmljim.service.di.registry;

import io.github.xmljim.service.di.scanner.Scanner;
import io.github.xmljim.service.di.service.Service;
import io.github.xmljim.service.di.util.ClassFilter;
import io.github.xmljim.service.di.util.ClassFilters;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Storage for all registered services. Contains methods for loading, reloading and retrieving services.
 * <p>Implementation classes must provide the logic for the {@link #load()} and {@link #reload()} methods.
 * While not required, the general flow from these methods is to create {@link Scanner} instances which are
 * intended to scan classpath and module paths to load the registry.
 * </p>
 * <p>
 * Storage of {@link Service} instances is implementation specific. The only requirement is that the
 * implementation class must provide a {@link Stream} of all services stored.
 * </p>
 */
public interface ServiceRegistry {

    /**
     * Flag indicating if the underlying {@link Scanner} classes have located and registered all services
     * @return {@code true} when loaded
     */
    boolean isLoaded();

    /**
     * Initiate the underlying {@link Scanner} classes to load all registered services
     * <p> This method is the equivalent of running <code>load(ClassFilters.DEFAULT,
     * ClassFilters.DEFAULT)</code></p>
     */
    default void load() {
        load(ClassFilters.DEFAULT, ClassFilters.DEFAULT);
    }

    /**
     * Load all declared services, filtering for services and providers that match filter criteria. There are some
     * predefined filters in {@link ClassFilters}. However, you are not limited to using these.
     * <p> The filters are all Predicate functions typed to <code>Class&lt;?&gt;</code> and are
     * executed
     * during the initial scan. Only those classes that meet the filter criteria are considered
     * for service
     * registration </p>
     * @param serviceFilter  The service class filter
     * @param providerFilter the provider class filter
     * @see ClassFilters
     */
    void load(ClassFilter serviceFilter, ClassFilter providerFilter);


    /**
     * Load a named scanner
     * @param scannerName                          the scanner name
     * @param serviceFilter                        the service filter
     * @param providerFilter                       the provider filter
     * @param enforceProviderAssignableFromService enforce assignability
     * @return {@code true} if the scanner ran successfully
     */
    boolean load(String scannerName, ClassFilter serviceFilter, ClassFilter providerFilter,
        boolean enforceProviderAssignableFromService);

    /**
     * Load the registry using a Scanner
     * <p>Running a scanner in this fashion allows for custom scanners to be run in an ad-hoc manner. However,
     * invoking a scanner in this way does not append the scanner to the registry. The recommended method would be
     * to {@link #appendScanner(Scanner)} first and run one of the load methods</p>
     * @param scanner The scanner to run.
     * @return {@code true} if loaded
     */
    boolean load(Scanner scanner);

    /**
     * Returns whether a scanner has been run and loaded into the registry
     * @param scanner The scanner name
     * @return {@code true} if the scanner has been run; false if the scanner does not exist, or has not run. To determine
     *     if a scanner exists on the registry, use {@link #getScanners()} to locate all known scanners.
     */
    boolean isLoaded(String scanner);

    /**
     * Append a scanner to the registry. This has the effect of running this and all other scanners in subsequent load
     * and
     * reload scenarios.
     * @param scanner the scanner to append
     */
    void appendScanner(Scanner scanner);

    <S extends Scanner> void appendScanner(String name, Class<S> scannerClass);

    /**
     * Retrieve all known scanners in this registry
     * @return A set of all scanner names
     */
    Set<String> getScanners();

    /**
     * Reload the registry. This will clear all existing entries and run a default load. If you wish to
     * filter the service registry, use {@link #reload(ClassFilter, ClassFilter)}
     */
    default void reload() {
        reload(ClassFilters.DEFAULT, ClassFilters.DEFAULT);
    }

    /**
     * Clear all existing registry entries and reload using the specified service and provider class filters
     * @param serviceFilter  The service class filter
     * @param providerFilter the provider class filter
     */
    void reload(ClassFilter serviceFilter, ClassFilter providerFilter);

    /**
     * Locate a service
     * @param serviceClass the service class
     * @param <S>          the service class type
     * @return an Optional containing the requested service if present; otherwise, return {@link Optional#empty()}
     */
    default <S> Optional<Service> findService(Class<S> serviceClass) {
        return services().filter(service -> service.getServiceClass().equals(serviceClass))
            .findFirst();
    }

    /**
     * Locate all services that match ClassFilter criteria
     * @param serviceClassFilter The service ClassFilter
     * @return a set of services that match a given criteria
     */
    default Set<Service> findServices(ClassFilter serviceClassFilter) {
        return services().filter(s -> serviceClassFilter.test(s.getServiceClass())).collect(Collectors.toSet());
    }

    /**
     * Interrogate the registry for the existing of a service class
     * @param serviceClass The requested service class
     * @param <S>          The service class type
     * @return <code>true</code> if the service exists; {@code false} otherwise
     */
    default <S> boolean hasService(Class<S> serviceClass) {
        return services().anyMatch(service -> service.getServiceClass().equals(serviceClass));
    }

    /**
     * Append a service to the registry.
     * @param service The service to append
     */
    void appendService(Service service);

    /**
     * Stream interface for all registered services
     * @return a stream of all registered services
     */
    Stream<Service> services();

    /**
     * Load a service provider instance using this service
     * @param serviceClass The service class
     * @param <T>          The return type
     * @param <S>          The service type
     * @return a new service instance
     */
    <T, S> T loadServiceProvider(Class<S> serviceClass);

    /**
     * Load a service provider instance using the provider's name and this service
     * @param serviceClass The service class
     * @param providerName The provider name
     * @param <S>          The service type
     * @param <T>          The return type
     * @return A new service instance
     */
    <T, S> T loadServiceProvider(Class<S> serviceClass, String providerName);

    /**
     * Create a set of all service providers for a given service
     * @param serviceClass the service class
     * @param <S>          The service type
     * @return a Set of provider instances for a given service
     */
    <S, T> Set<T> loadAllServiceProviders(Class<S> serviceClass);

    /**
     * Enforce assignability between the service and provider classes. While this does not actually
     * provide any enforcement, it's provided as a means of passing the enforcement flag to {@link Scanner} classes
     * which instantiate service and provider instances.  This can still be overridden by
     * {@link Service#enforceAssignableFromProvider()}, or ignored by a {@link Scanner} implementation
     * @return {@code true} if the intent is to enforce assignability between service and provider classes. Otherwise,
     *     the provider will not actually attempt to enforce any relationship with the service class
     */
    boolean enforceProviderAssignableFromService();

    /**
     * Sets the assignability enforcement between service and provider
     * @param enforceProviderAssignableFromService the assignability enforcement flag
     */
    void setEnforceProviderAssignableFromService(boolean enforceProviderAssignableFromService);
}
