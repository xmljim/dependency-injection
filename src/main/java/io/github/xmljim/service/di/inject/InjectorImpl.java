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

/**
 * Injector Implementation
 */
@ServiceProvider(name = "Injector", lifetime = ServiceLifetime.SINGLETON)
public class InjectorImpl implements Injector {
    private static final Logger LOGGER = LoggerFactory.getLogger(InjectorImpl.class);
    private final ServiceRegistry serviceRegistry;

    /**
     * Constructor
     */
    @Generated
    public InjectorImpl() {
        //no-op
        throw new ServiceManagerException("Cannot instantiate default zero-argument constructor due to dependency");
    }

    /**
     * Constructor
     * @param serviceRegistry the service registry
     */
    @DependencyInjection
    public InjectorImpl(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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