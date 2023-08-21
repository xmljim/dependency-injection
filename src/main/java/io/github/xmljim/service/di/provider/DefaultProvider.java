package io.github.xmljim.service.di.provider;

import io.github.xmljim.service.di.ServiceManagerException;
import io.github.xmljim.service.di.annotations.Generated;
import io.github.xmljim.service.di.annotations.ServiceProvider;
import io.github.xmljim.service.di.service.Service;
import io.github.xmljim.service.di.util.ServiceLifetime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static io.github.xmljim.service.di.internal.ClassUtils.findConstructor;
import static io.github.xmljim.service.di.internal.ClassUtils.getParameterValues;
import static io.github.xmljim.service.di.internal.ClassUtils.injectFields;

/**
 * {@inheritDoc}
 */
class DefaultProvider extends Providers {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultProvider.class);
    private Object instance;


    public DefaultProvider(Service service, Class<?> providerClass) {
        super(service, providerClass);

        if (service.enforceAssignableFromProvider()) {
            if (!service.getServiceClass().isAssignableFrom(providerClass)) {
                LOGGER.error("Expected provider class to be assignable from service class: [Service={}, Provider={}]",
                    service.getServiceClass(), providerClass);
                throwError("Expected provider to implement or extend Service");
            }
        }

        applyProviderProperties();
    }

    /**
     * Internal method for assigning a provider's name and lifetime properties. If the
     * provider class includes a {@link ServiceProvider} annotation, it will use these values. Otherwise,
     * it will default to the provider class' name and use {@link ServiceLifetime#TRANSIENT}
     */
    private void applyProviderProperties() {
        if (getProviderClass().isAnnotationPresent(ServiceProvider.class)) {
            LOGGER.debug("Provider class as ServiceProvider annotation");
            var serviceProvider = getProviderClass().getAnnotation(ServiceProvider.class);
            setName(serviceProvider.name().isEmpty() ? getProviderClass().getName() : serviceProvider.name());
            this.setServiceLifetime(serviceProvider.lifetime());
        } else {
            setName(getProviderClass().getName());
            this.setServiceLifetime(ServiceLifetime.TRANSIENT);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings({"unchecked", "unused"})
    public <T> T getInstance() {
        //note: can't use Injector service due to infinite recursion
        //return the instance if one was saved previously (i.e., ServiceLifetime == SINGLETON)
        if (instance != null) {
            LOGGER.debug("Provider class cached. Returning existing instance");
            return (T) instance;
        }

        //locate the constructor to use on the provider
        Constructor<?> constructor = findConstructor(getProviderClass(), getService().getServiceRegistry());

        LOGGER.debug("Constructor to create new provider instance: {}", constructor);

        //initialize parameter values to be used for creating the new class instance (e.g., dependency injection)
        List<?> parameterValues = getParameterValues(constructor, getService().getServiceRegistry());

        try {
            //create the instance.
            T instance = (T) constructor.newInstance(parameterValues.toArray());
            LOGGER.debug("Service Provider Instance created: {}", instance.getClass());

            //if the provider is defined as a singleton instance, save it for future requests
            if (getServiceLifetime() == ServiceLifetime.SINGLETON) {
                LOGGER.debug("Caching provider instance as a singleton");
                this.instance = instance;
            }

            //now find any fields that might want dependency injection
            return injectFields(getService().getServiceRegistry(), instance);

        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new ServiceManagerException(e.getMessage(), e);
        }
    }

    /**
     * Utility for generating errors
     * @param message the error message
     * @return an exception instance
     */
    private ServiceManagerException serviceError(String message) {
        LOGGER.error(message);
        return new ServiceManagerException(message);
    }

    /**
     * Utility for generating errors
     * @param message   the error message
     * @param throwable The underlying exception that caused this error
     * @return an exception instance
     */
    @Generated
    private ServiceManagerException serviceError(String message, Throwable throwable) {
        return new ServiceManagerException(message, throwable);
    }

    /**
     * Throw an error
     * @param message the error message
     */
    @SuppressWarnings("SameParameterValue")
    @Generated
    private void throwError(String message) {
        throw serviceError(message);
    }

    /**
     * Throw an error
     * @param message   the error message
     * @param throwable The underlying exception that caused this error
     */
    @Generated
    private void throwError(String message, Throwable throwable) {
        throw serviceError(message, throwable);
    }


}
