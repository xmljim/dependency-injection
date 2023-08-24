/*
 * Copyright 2023 Jim Earley (xml.jim@gmail.com)
 *
 * Licensed under the Apache NON-AI License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://raw.githubusercontent.com/non-ai-licenses/non-ai-licenses/main/NON-AI-APACHE2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.xmljim.service.di.registry;

import io.github.xmljim.service.di.inject.Injector;
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
     * Create a new instance of a class
     * <p>The class must have a constructor that has either no arguments or one where all arguments are "injectable"</p>
     * <p>If the class contains more than one constructor, the preferred constructor should have a
     * {@link io.github.xmljim.service.di.annotations.DependencyInjection} annotation</p>
     * @param classToLoad The class to load
     * @param <T>         The class type
     * @return a new class instance, with any injected services instantiated.
     */
    default <T> T loadClass(Class<T> classToLoad) {
        Injector injector = loadServiceProvider(Injector.class);
        return injector.createInstance(classToLoad);
    }

    /**
     * Load a class using a constructor containing zero or more injectable services and zero or more non-injectable
     * parameters.
     * <p><strong>IMPORTANT:</strong> The desired constrctor <em>must</em> have a
     * {@link io.github.xmljim.service.di.annotations.DependencyInjection} annotation.</p>
     * <p>
     * All injectable service parameters must appear before all non-injectable parameters
     * </p>
     * <p>Example:</p>
     * <p>Assume a class, {@code MyExampleClass} uses two services, {@code MyService1} and {@code MyService2}. It
     * also requires a String and boolean parameter using the constructor:</p>
     * <pre>
     *
     *    {@literal @}DependencyInjection
     *     public MyExampleClass(MyService1 myService1, MyService2, String name, boolean test) {
     *         ...
     *     }
     * </pre>
     * <p>
     * The invocation would be:
     * </p>
     * <pre>
     *     var serviceRegistry = ServiceRegistries.getInstance();
     *     MyExampleClass exampleClass = serviceRegistry.loadClass(MyExampleClass.class, "test", true);
     * </pre>
     * <p>
     * Notice that the {@code MyService1} and {@code MyService2} parameters are not included with the
     * {@code loadClass} parameters. This is because the constructor's arguments will be interrogated in sequence.
     * If the parameter type has a registered service, it will be resolved from the service registry. Otherwise
     * the parameter value will be taken from the additional args provided in order.
     * </p>
     * @param classToLoad The class to load
     * @param args        any non-injectable arguments
     * @param <T>         The class type to create
     * @return a new class instance
     */
    default <T> T loadClass(Class<T> classToLoad, Object... args) {
        Injector injector = loadServiceProvider(Injector.class);
        return injector.createInstanceWithArgs(classToLoad, args);
    }

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
