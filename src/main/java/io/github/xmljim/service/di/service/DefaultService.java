package io.github.xmljim.service.di.service;

import io.github.xmljim.service.di.annotations.ServiceProvider;
import io.github.xmljim.service.di.provider.Provider;
import io.github.xmljim.service.di.registry.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.Optional;

/**
 * Default service implementation.
 */
class DefaultService extends Services {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultService.class);

    public DefaultService(Class<?> serviceClass, ServiceRegistry serviceRegistry) {
        super(serviceClass, serviceRegistry);
    }

    public DefaultService(Class<?> serviceClass, ServiceRegistry serviceRegistry, boolean enforceAssignableFrom) {
        super(serviceClass, serviceRegistry, enforceAssignableFrom);
    }


    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unused")
    @Override
    public Optional<Provider> getProvider() {
        LOGGER.debug("Get Provider");
        Comparator<Provider> comparePriority = Comparator.comparingInt(p -> p.getProviderClass().getAnnotation(ServiceProvider.class).priority());
        var provider = getProviders()
            .filter(p -> p.getProviderClass().isAnnotationPresent(ServiceProvider.class)) //preferred option
            .max(comparePriority)
            .or(() -> getProviders().findFirst());
        
        LOGGER.debug("Provider found: {}", provider);
        return provider;
    }


}
