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

package io.github.xmljim.service.di.internal;

import io.github.xmljim.service.di.ServiceManagerException;
import io.github.xmljim.service.di.annotations.DependencyInjection;
import io.github.xmljim.service.di.annotations.Inject;
import io.github.xmljim.service.di.annotations.ServiceProvider;
import io.github.xmljim.service.di.registry.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.*;

public class ClassUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClassUtils.class);

    @SuppressWarnings("unchecked")
    public static <T> Constructor<T> findConstructor(Class<?> thisClass, ServiceRegistry serviceRegistry) {
        List<Constructor<?>> allConstructors = Arrays.stream(thisClass.getConstructors()).toList();

        var validConstructors = allConstructors.stream()
            .filter(ctor -> isInjectable(serviceRegistry, ctor.getParameterTypes()))
            .filter(ctor -> Modifier.isPublic(ctor.getModifiers())).toList();

        var diConstructor = validConstructors.stream().filter(ctor -> ctor.getAnnotation(DependencyInjection.class) != null)
            .findFirst();

        LOGGER.debug("Pick constructor to create new provider instance: {}", diConstructor);

        return (Constructor<T>) diConstructor.orElse(validConstructors.stream().findFirst()
            .orElseThrow(() -> new ServiceManagerException("No valid constructor found")));
    }

    public static boolean isInjectable(ServiceRegistry serviceRegistry, Class<?>... parameterClass) {
        return parameterClass.length == 0 || Arrays.stream(parameterClass).allMatch(serviceRegistry::hasService);
    }

    /**
     * Internal utility for loading additional services required for this provider
     * @param serviceType The service type
     * @param <T>         the service instance type
     * @return a service instance
     */
    @SuppressWarnings("unchecked")
    private static <T> T loadServiceInstance(ServiceRegistry serviceRegistry, Class<?> serviceType) {
        return ServiceRegistry.class.isAssignableFrom(serviceType) ? (T) serviceRegistry
            : serviceRegistry.loadServiceProvider(serviceType);

    }

    /**
     * After the instance has been instantiated, look for any fields on the class instance that
     * have been decorated with the {@link Inject} annotation. For each of these, create/get an instance of that
     * service and assign it to the field
     * @param instance The current service provider instance
     * @param <T>      The service provider type
     * @return the current service provider instance with all requested dependency injected fields assigned
     */
    public static <T> T injectFields(ServiceRegistry serviceRegistry, T instance) {
        //var fields = instance.getClass().getDeclaredFields();

        Arrays.stream(instance.getClass().getDeclaredFields()).filter(field -> field.isAnnotationPresent(Inject.class))
            .forEach(field -> {
                LOGGER.debug("Injecting service into field: {}", field.getName());
                Inject inject = field.getAnnotation(Inject.class);

                var fieldInstance = inject.providerName().isEmpty() ? loadServiceInstance(serviceRegistry, field.getType()) :
                    loadServiceInstance(serviceRegistry, field.getType(), inject.providerName());
                try {
                    field.trySetAccessible();
                    field.set(instance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new ServiceManagerException(e.getMessage(), e);
                }
            });
        return instance;
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
    private static <T> T loadServiceInstance(ServiceRegistry serviceRegistry, Class<?> serviceType, String name) {
        return ServiceRegistry.class.isAssignableFrom(serviceType) ? (T) serviceRegistry
            : serviceRegistry.loadServiceProvider(serviceType, name);
    }

    /**
     * Using a constructor, iterate through each parameter, initialize a service instance as the parameter
     * value.
     * @param constructor The constructor to interrogate
     * @param <P>         The provider constructor type
     * @return A list containing parameter values mapped to service instance for each parameter
     */
    public static <P> List<?> getParameterValues(Constructor<P> constructor, ServiceRegistry serviceRegistry) {
        LOGGER.debug("Inject constructor parameter values: {}", constructor);
        return Arrays.stream(constructor.getParameters())
            .map(param -> param.isAnnotationPresent(ServiceProvider.class) ?
                loadServiceInstance(serviceRegistry, param.getType(), param.getAnnotation(ServiceProvider.class).name()) :
                loadServiceInstance(serviceRegistry, param.getType()))
            .toList();
    }

    public static <P> List<Object> getParameterValues(Constructor<P> constructor, Deque<Object> otherArgs, ServiceRegistry serviceRegistry) {
        List<Object> paramValues = new ArrayList<>();

        Arrays.stream(constructor.getParameters()).forEach(param -> {
            if (isInjectable(serviceRegistry, param.getType())) {
                var paramValue = param.isAnnotationPresent(ServiceProvider.class) ?
                    loadServiceInstance(serviceRegistry, param.getType(), param.getAnnotation(ServiceProvider.class).name()) :
                    loadServiceInstance(serviceRegistry, param.getType());
                paramValues.add(paramValue);
            } else {
                try {
                    paramValues.add(otherArgs.pop());
                } catch (NoSuchElementException | EmptyStackException e) {
                    throw new ServiceManagerException("No argument value provided for %s (type: %s)", param.getName(), param.getType());
                }
            }
        });

        return paramValues;
    }
}
