package io.github.xmljim.service.di.service;

import io.github.xmljim.service.di.annotations.ServiceProvider;
import io.github.xmljim.service.di.provider.Provider;
import io.github.xmljim.service.di.registry.ServiceRegistry;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * A registered service containing one or more providers.  Services are principally "key/set" pairings of a
 * service class and one or more provider classes that implement the service. While there is no absolute requirement
 * that a service must be an interface, it is a general convention (perhaps best practice) that they are interfaces.
 * Similarly, while the general convention is that a service provider implements service class, there is no
 * requirement.  However, if you wish to enforce assignability between service and provider, you can set the
 * {@link #enforceAssignableFromProvider()} to {@code true}
 */
public interface Service {

    /**
     * Append a provider to the service
     * @param Provider The provider instance
     */
    void appendProvider(Provider Provider);

    /**
     * Get the provider. Preference will be given to provider classes that are
     * decorated with the {@link ServiceProvider} annotation. If none are present, it will return
     * the first available
     * @return An optional containing the provider if one exists, otherwise it will return {@link Optional#empty()}
     */
    Optional<Provider> getProvider();

    /**
     * Get the named provider.
     * <p>
     * If a class is decorated with the {@link ServiceProvider} annotation, the provider name will be assigned
     * from the {@link ServiceProvider#name()} value (if it is not empty). Otherwise the name will be the fully
     * qualified classname;
     * </p>
     * @return An optional containing the provider if one exists, otherwise it will return {@link Optional#empty()}
     */
    Optional<Provider> getProvider(String name);

    /**
     * Interrogates the service's provider cache for a provider class matching
     * the request class
     * @param providerClass the provider class to look for
     * @return {@code true} if the provider class was found; {@code false} otherwise
     */
    boolean hasProvider(Class<?> providerClass);

    /**
     * Return the underlying class for this provider
     * @return the class reference
     */
    Class<?> getServiceClass();

    /**
     * Return the ServiceRegistry
     * @return the Service Registry
     */
    ServiceRegistry getServiceRegistry();

    /**
     * apply assignability enforcement on a provider
     * @return if {@code true}, the provider class should be assignable from the service class
     */
    boolean enforceAssignableFromProvider();

    /**
     * Return a stream of providers
     * @return a stream of providers
     */
    Stream<Provider> getProviders();
}
