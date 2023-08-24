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

package io.github.xmljim.service.di.service;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.xmljim.service.di.annotations.Generated;
import io.github.xmljim.service.di.provider.Provider;
import io.github.xmljim.service.di.registry.ServiceRegistry;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Abstract Service class intended to be extended
 */
@SuppressWarnings("unused")
public abstract class Services implements Service {

    private static Class<? extends Service> useService;
    private static final Class<? extends Service> DEFAULT = DefaultService.class;
    private final Class<?> serviceClass;
    private final Set<Provider> providers = new HashSet<>();
    private final ServiceRegistry serviceRegistry;
    private boolean enforceAssignableFrom;

    /**
     * Initialize a service.  By default, {@link #enforceAssignableFromProvider()} will be {@code false}
     * @param serviceClass    the service class
     * @param serviceRegistry the service registry
     */
    public Services(Class<?> serviceClass, ServiceRegistry serviceRegistry) {
        this.serviceClass = serviceClass;
        this.serviceRegistry = serviceRegistry;
        setEnforceAssignableFrom(serviceRegistry.enforceProviderAssignableFromService());
    }

    /**
     * Initialize a service.
     * @param serviceClass          the service class
     * @param serviceRegistry       the service registry
     * @param enforceAssignableFrom flag to enforce provider assignability to a service class (i.e.,
     *                              the provider must inherit or implement the service class
     */
    public Services(Class<?> serviceClass, ServiceRegistry serviceRegistry, boolean enforceAssignableFrom) {
        this(serviceClass, serviceRegistry);
        setEnforceAssignableFrom(enforceAssignableFrom);
    }

    public static void setUseServiceClass(Class<? extends Service> serviceClass) {
        useService = serviceClass;
    }

    private static Class<? extends Service> getUseService() {
        return useService == null ? DEFAULT : useService;
    }

    /**
     * Create a new Service
     * @param serviceClass    The service class
     * @param serviceRegistry The service registry
     * @return a new Service instance using the default Service implementation class
     */
    public static Service newService(Class<?> serviceClass, ServiceRegistry serviceRegistry) {
        return newService(getUseService(), serviceClass, serviceRegistry, serviceRegistry.enforceProviderAssignableFromService());
    }

    /**
     * Create a new Service
     * @param serviceClass          the service class
     * @param serviceRegistry       the service registry
     * @param enforceAssignableFrom flag to enforce provider assignability to a service class (i.e.,
     *                              the provider must inherit or implement the service class
     * @return a new Service instance using the default Service implementation class
     */
    public static Service newService(Class<?> serviceClass, ServiceRegistry serviceRegistry, boolean enforceAssignableFrom) {
        return newService(getUseService(), serviceClass, serviceRegistry, enforceAssignableFrom);
    }

    public static <S extends Service> Service newService(Class<S> serviceImpl, Class<?> serviceClass, ServiceRegistry serviceRegistry,
        boolean enforceAssignableFrom) {

        try {
            Constructor<S> ctor = serviceImpl.getConstructor(Class.class, ServiceRegistry.class, boolean.class);
            return ctor.newInstance(serviceClass, serviceRegistry, enforceAssignableFrom);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getServiceClass() {
        return serviceClass;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressFBWarnings("EI_EXPOSE_REP")
    @Override
    public ServiceRegistry getServiceRegistry() {
        return serviceRegistry;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Provider> getProvider(String name) {
        return getProviders().filter(provider -> provider.getName().equals(name)).findFirst();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void appendProvider(Provider provider) {
        providers.add(provider);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean enforceAssignableFromProvider() {
        return enforceAssignableFrom;
    }

    /**
     * Set assignability enforcement
     * @param assignableFrom the flag to enforce assignability
     */
    public void setEnforceAssignableFrom(boolean assignableFrom) {
        enforceAssignableFrom = assignableFrom;
    }

    /**
     * Return a stream of all providers
     * @return The Provider stream
     */
    public Stream<Provider> getProviders() {
        return providers.stream();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasProvider(Class<?> providerClass) {
        return getProviders().anyMatch(p -> p.getProviderClass().equals(providerClass));
    }

    @Override
    @Generated
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultService service = (DefaultService) o;
        return Objects.equals(serviceClass, service.getServiceClass())
            && Objects.equals(providers.stream(), service.getProviders())
            && Objects.equals(serviceRegistry, service.getServiceRegistry());
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(serviceClass);
    }

    @Override
    @Generated
    public String toString() {
        return "Service{" +
            "serviceClass=" + serviceClass +
            '}';
    }

}
