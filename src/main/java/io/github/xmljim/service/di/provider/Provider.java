package io.github.xmljim.service.di.provider;

import io.github.xmljim.service.di.annotations.ServiceProvider;
import io.github.xmljim.service.di.service.Service;
import io.github.xmljim.service.di.util.ServiceLifetime;

/**
 * A Service Provider. Providers must be concrete classes that fulfill the service request. In <em>most</em> cases,
 * a service provider extends or implements a service class, but this is not necessarily a requirement. For example,
 * it is quite possible to implement a service as a mediator pattern where the service is a type of "request",
 * and is fulfilled by a type of "response" (look for news on this topic soon). In this case, the provider may not
 * implement or extend the service request.
 */
@SuppressWarnings("unused")
public interface Provider {
    /**
     * The provider name. By default, the name is the
     * provider classname. If the class is decorated with the {@link ServiceProvider}
     * attribute, the name will be retrieved from {@link ServiceProvider#name()}
     * @return the provider name
     */
    String getName();

    /**
     * The service lifetime for this provider. It will be set to {@link ServiceLifetime#TRANSIENT} by default.
     * This can be overridden with the {@link ServiceProvider} attribute
     * @return the service lifetime
     */
    ServiceLifetime getServiceLifetime();

    /**
     * Specifies the lifetime of this service provider instance
     * @param lifetime The lifetime enumerated value
     */
    void setServiceLifetime(ServiceLifetime lifetime);

    /**
     * The class that be instantiated
     * @return The provider class
     */
    Class<?> getProviderClass();

    /**
     * Create a new instance of the requested service using this provider
     * @param <T> The underlying type for the service
     * @return a new instance of the requested service provider
     */

    <T> T getInstance();

    /**
     * Return the service that contains this provider
     * @return the service that contains this provider
     */
    Service getService();
}
