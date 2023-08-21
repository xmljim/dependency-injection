package io.github.xmljim.service.di.inject;

import io.github.xmljim.service.di.ServiceManagerException;
import io.github.xmljim.service.di.annotations.DependencyInjection;
import io.github.xmljim.service.di.annotations.Generated;
import io.github.xmljim.service.di.annotations.ServiceProvider;
import io.github.xmljim.service.di.registry.ServiceRegistry;
import io.github.xmljim.service.di.util.ServiceLifetime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;

import static io.github.xmljim.service.di.internal.ClassUtils.findConstructor;
import static io.github.xmljim.service.di.internal.ClassUtils.getParameterValues;
import static io.github.xmljim.service.di.internal.ClassUtils.injectFields;

@ServiceProvider(name = "Injector", lifetime = ServiceLifetime.SINGLETON, priority = 1)
public class InjectorImpl implements Injector {
    private static final Logger LOGGER = LoggerFactory.getLogger(InjectorImpl.class);
    private final ServiceRegistry serviceRegistry;

    @Generated
    public InjectorImpl() {
        //no-op
        throw new ServiceManagerException("Cannot instantiate default zero-argument constructor due to dependency");
    }

    @DependencyInjection
    public InjectorImpl(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public <T> T createInstance(Class<T> instanceClass) {
        //locate the constructor to use on the provider
        Constructor<T> constructor = findConstructor(instanceClass, serviceRegistry);

        LOGGER.debug("Constructor to create new provider instance: {}", constructor);

        //initialize parameter values to be used for creating the new class instance (e.g., dependency injection)
        List<?> parameterValues = getParameterValues(constructor, serviceRegistry);

        try {
            //create the instance.
            T instance = constructor.newInstance(parameterValues.toArray());
            LOGGER.debug("Service Provider Instance created: {}", instance.getClass());

            //now find any fields that might want dependency injection
            return injectFields(serviceRegistry, instance);

        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new ServiceManagerException(e.getMessage(), e);
        }
    }

    public <T> T createInstanceWithArgs(Class<T> instanceClass, Object... args) {
        @SuppressWarnings("unchecked")
        Constructor<T> constructor = (Constructor<T>) Arrays.stream(instanceClass.getConstructors())
            .filter(ctor -> ctor.isAnnotationPresent(DependencyInjection.class))
            .findFirst()
            .orElseThrow(() -> new ServiceManagerException("Constructor must have @DependencyInjection annotation"));

        ArrayDeque<Object> otherArgsStack = new ArrayDeque<>();
        Arrays.stream(args).forEach(otherArgsStack::addLast);

        List<?> parameterValues = getParameterValues(constructor, otherArgsStack, serviceRegistry);

        try {
            //create the instance.
            T instance = constructor.newInstance(parameterValues.toArray());
            LOGGER.debug("Service Provider Instance created: {}", instance.getClass());

            //now find any fields that might want dependency injection
            return injectFields(serviceRegistry, instance);

        } catch (IllegalArgumentException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new ServiceManagerException(e.getMessage(), e);
        }
    }

}