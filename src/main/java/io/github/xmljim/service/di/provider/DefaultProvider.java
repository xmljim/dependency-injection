package io.github.xmljim.service.di.provider;

import io.github.xmljim.service.di.ServiceManagerException;
import io.github.xmljim.service.di.annotations.DependencyInjection;
import io.github.xmljim.service.di.annotations.Generated;
import io.github.xmljim.service.di.annotations.Inject;
import io.github.xmljim.service.di.annotations.ServiceProvider;
import io.github.xmljim.service.di.registry.ServiceRegistry;
import io.github.xmljim.service.di.service.Service;
import io.github.xmljim.service.di.util.ServiceLifetime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

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
        //return the instance if one was saved previously (i.e., ServiceLifetime == SINGLETON)
        if (instance != null) {
            LOGGER.debug("Provider class cached. Returning existing instance");
            return (T) instance;
        }

        //locate the constructor to use on the provider
        Constructor<?> constructor = getConstructor();

        LOGGER.debug("Constructor to create new provider instance: {}", constructor);

        //initialize parameter values to be used for creating the new class instance (e.g., dependency injection)
        List<?> parameterValues = getParameterValues(constructor);

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
            return injectFields(instance);

        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new ServiceManagerException(e.getMessage(), e);
        }


    }

    /**
     * After the provider has been instantiated, look for any fields on the class instance that
     * have been decorated with the {@link Inject} annotation. For each of these, create/get an instance of that
     * service and assign it to the field
     * @param instance The current service provider instance
     * @param <T>      The service provider type
     * @return the current service provider instance with all requested dependency injected fields assigned
     */
    private <T> T injectFields(T instance) {
        //var fields = instance.getClass().getDeclaredFields();

        Arrays.stream(instance.getClass().getDeclaredFields()).filter(field -> field.isAnnotationPresent(Inject.class))
            .forEach(field -> {
                LOGGER.debug("Injecting service into field: {}", field.getName());
                var inject = field.getAnnotation(Inject.class);

                var fieldInstance = inject.providerName().isEmpty() ? loadServiceInstance(field.getType()) :
                    loadServiceInstance(field.getType(), inject.providerName());
                try {
                    field.trySetAccessible();
                    field.set(instance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throwError(e.getMessage(), e);
                }
            });
        return instance;
    }

    /**
     * Find a constructor on the provider class to use for creating a new instance. Priority is given to
     * any constructor with the {@link DependencyInjection} annotation (which will be required if the
     * constructor includes any arguments). Note: all arguments must reference a registered service.
     * @return The constructor that will be used to create the provider instance
     */
    private Constructor<?> getConstructor() {
        List<Constructor<?>> allConstructors = Arrays.stream(getProviderClass().getConstructors()).toList();

        var validConstructors = allConstructors.stream()
            .filter(ctor -> isInjectable(ctor.getParameterTypes()))
            .filter(ctor -> Modifier.isPublic(ctor.getModifiers())).toList();

        var diConstructor = validConstructors.stream().filter(ctor -> ctor.getAnnotation(DependencyInjection.class) != null)
            .findFirst();

        LOGGER.debug("Pick constructor to create new provider instance: {}", diConstructor);

        return diConstructor.orElse(validConstructors.stream().findFirst().orElseThrow(() -> serviceError("No valid constructor found")));
    }

    /**
     * Internal utility for loading additional services required for this provider
     * @param serviceType The service type
     * @param <T>         the service instance type
     * @return a service instance
     */
    @SuppressWarnings("unchecked")
    private <T> T loadServiceInstance(Class<?> serviceType) {
        return ServiceRegistry.class.isAssignableFrom(serviceType) ? (T) getService().getServiceRegistry()
            : getService().getServiceRegistry().loadServiceProvider(serviceType);

    }

    /**
     * Internal utility for loading additional services required for this provider, using the name for the
     * additional service's provider
     * @param serviceType The service type
     * @param name        the provider name
     * @param <T>         The service instance type
     * @return The service instance
     */
    @SuppressWarnings("unchecked")
    private <T> T loadServiceInstance(Class<?> serviceType, String name) {
        return ServiceRegistry.class.isAssignableFrom(serviceType) ? (T) getService().getServiceRegistry()
            : getService().getServiceRegistry().loadServiceProvider(serviceType, name);
    }

    /**
     * Using a constructor, iterate through each parameter, initialize a service instance as the parameter
     * value.
     * @param constructor The constructor to interrogate
     * @param <P>         The provider constructor type
     * @return A list containing parameter values mapped to service instance for each parameter
     */
    private <P> List<?> getParameterValues(Constructor<P> constructor) {
        LOGGER.debug("Inject constructor parameter values: {}", constructor);
        return Arrays.stream(constructor.getParameters())
            .map(param -> param.isAnnotationPresent(ServiceProvider.class) ?
                loadServiceInstance(param.getType(), param.getAnnotation(ServiceProvider.class).name()) :
                loadServiceInstance(param.getType()))
            .toList();
    }

    /**
     * Used when trying to locate a "valid" constructor to create a provider instance. A "valid" constructor
     * contains either zero arguments, or all arguments reference a registered service.
     * @param parameterClass a varargs array of parameter types for a given constructor
     * @return {@code true} if no arguments exist, or all argument types reference a registered service
     */
    private synchronized boolean isInjectable(Class<?>... parameterClass) {
        return parameterClass.length == 0 || Arrays.stream(parameterClass).allMatch(c -> getService().getServiceRegistry().hasService(c));
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
