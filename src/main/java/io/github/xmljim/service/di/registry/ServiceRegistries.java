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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.xmljim.service.di.ServiceManagerException;
import io.github.xmljim.service.di.annotations.Generated;
import io.github.xmljim.service.di.provider.Provider;
import io.github.xmljim.service.di.provider.Providers;
import io.github.xmljim.service.di.service.Service;
import io.github.xmljim.service.di.service.Services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Abstract ServiceRegistry implementation designed for extension. Also contains
 * static methods for creating instances.
 */
@SuppressWarnings("unused")
public abstract class ServiceRegistries implements ServiceRegistry {

    private static final Class<? extends ServiceRegistry> DEFAULT = ServiceRegistryImpl.class;
    private static Class<? extends ServiceRegistry> useServiceRegistry;
    private static ServiceRegistry instance;
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRegistries.class);
    private final Set<Service> serviceSet = new HashSet<>();
    private boolean enforceProviderAssignableFromService;

    /**
     * Create a new default ServiceRegistry
     * @return a new default ServiceRegistry instance
     */
    public static ServiceRegistry newServiceRegistry() {
        return newServiceRegistry(getUseServiceRegistry());
    }

    /**
     * Create a new default ServiceRegistry and apply enforcement for service/provider assignability
     * @param enforceProviderAssignableFromService the assignability flag
     * @return a new default ServiceRegistry
     */
    public static ServiceRegistry newServiceRegistry(boolean enforceProviderAssignableFromService) {
        LOGGER.debug("Creating a new Service Registry");
        var serviceRegistry = newServiceRegistry(getUseServiceRegistry());
        serviceRegistry.setEnforceProviderAssignableFromService(enforceProviderAssignableFromService);
        return serviceRegistry;
    }

    /**
     * Extension method for initializing a defined ServiceRegistry class.  The concrete class must have
     * a default or zero-argument constructor
     * @param serviceRegistryClass The service registry class. Must extend/implement {@link ServiceRegistry}
     * @param <S>                  The service registry type
     * @return a new ServiceRegistry
     */
    @SuppressWarnings("unchecked")
    public static <S extends ServiceRegistry> S newServiceRegistry(Class<S> serviceRegistryClass) {
        try {
            Constructor<S> ctor = serviceRegistryClass.getConstructor();
            var result = ctor.newInstance();
            instance = result;
            return result;
        } catch (Exception e) {
            //should never get here
            throw new ServiceManagerException(e.getMessage(), e);
        }
    }

    @Generated
    /**
     * Provided for {@link io.github.xmljim.service.di.RegistryBootstrap} to inject a registry
     * class
     */
    public static void setUseServiceRegistry(Class<? extends ServiceRegistry> serviceRegistry) {
        useServiceRegistry = serviceRegistry;
    }

    private static Class<? extends ServiceRegistry> getUseServiceRegistry() {
        return useServiceRegistry == null ? DEFAULT : useServiceRegistry;
    }

    @SuppressFBWarnings("MS_EXPOSE_REP")
    public static ServiceRegistry getInstance() {
        return instance;
    }

    public static void clear() {
        instance = null;
        setUseServiceRegistry(null);
        Providers.setUseProviderClass(null);
        Services.setUseServiceClass(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void appendService(Service service) {
        if (serviceSet.stream().noneMatch(s -> s.getServiceClass().equals(service.getServiceClass()))) {
            LOGGER.debug("Service Added: {}", service);
            serviceSet.add(service);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Stream<Service> services() {
        return serviceSet.stream();
    }

    /**
     * Remove all stored service references
     */
    public void clearServices() {
        LOGGER.debug("Clearing all services");
        serviceSet.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T, S> T loadServiceProvider(Class<S> serviceClass) {
        LOGGER.debug("Creating Service Provider instance for service: {}", serviceClass);
        return (T) findService(serviceClass).flatMap(Service::getProvider)
            .map(Provider::getInstance)
            .orElseThrow(() -> new ServiceManagerException("Provider for Service could not be instantiated: %s", serviceClass));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T, S> T loadServiceProvider(Class<S> serviceClass, String providerName) {
        LOGGER.debug("Loading Service Provider for service, using provider name: [Service={}, Provider Name={}]",
            serviceClass, providerName);
        return (T) findService(serviceClass).flatMap(s -> s.getProvider(providerName))
            .map(Provider::getInstance)
            .orElseThrow(() -> new ServiceManagerException("Provider with name %s not found for service %s", providerName, serviceClass));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <S, T> Set<T> loadAllServiceProviders(Class<S> serviceClass) {
        LOGGER.debug("Loading All Service Providers for service: [Service={}]",
            serviceClass);
        return (Set<T>) findService(serviceClass).map(s -> s.getProviders().map(Provider::getInstance).collect(Collectors.toSet()))
            .orElseThrow(() -> new ServiceManagerException("Service [%s] not found", serviceClass));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean enforceProviderAssignableFromService() {
        return enforceProviderAssignableFromService;
    }

    /**
     * enforce assignability
     * @param enforceAssignability enforce assignability
     */
    public void setEnforceProviderAssignableFromService(boolean enforceAssignability) {
        this.enforceProviderAssignableFromService = enforceAssignability;
    }
}
