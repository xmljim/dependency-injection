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

package io.github.xmljim.service.di;


import io.github.xmljim.service.di.registry.ServiceRegistries;
import io.github.xmljim.service.di.registry.ServiceRegistry;
import io.github.xmljim.service.di.scanner.Scanner;
import io.github.xmljim.service.di.service.Service;
import io.github.xmljim.service.di.util.ClassFilter;
import io.github.xmljim.service.di.util.ClassFilters;

import java.util.Set;

/**
 * Singleton facade around a {@link ServiceRegistry}
 */
public class ServiceManager {

    private static ServiceManager instance;

    private final ServiceRegistry serviceRegistry;

    private ServiceManager() {
        instance = this;


        //bootstrap the service registry, but don't load it
        RegistryBootstrap.load(() -> RegistryBootstrap.Options
            .configure()
            .loadRegistry(false)
            .enforceAssignability(true)
            .build());


        serviceRegistry = ServiceRegistries.getInstance();
    }

    /**
     * Get or create a new ServiceManager.
     * @return A ServiceManager
     */
    private static synchronized ServiceManager getInstance() {
        return instance == null ? new ServiceManager() : instance;
    }

    /**
     * Load the {@link ServiceRegistry}. By default, loads all default services
     */
    public static synchronized void load() {
        if (!getInstance().serviceRegistry.isLoaded()) {
            getInstance().serviceRegistry.load();
        }
    }

    /**
     * Load the {@link ServiceRegistry} with a filtered set of services and providers.
     * @param serviceFilter  The service class filter
     * @param providerFilter The provider class filter
     * @see ClassFilters for samples of filters that can be used
     */
    public static synchronized void load(ClassFilter serviceFilter, ClassFilter providerFilter) {
        if (!getInstance().serviceRegistry.isLoaded()) {
            getInstance().serviceRegistry.load(serviceFilter, providerFilter);
        }
    }

    public static synchronized boolean load(String scannerName, ClassFilter serviceFilter, ClassFilter providerFilter) {
        return getInstance().serviceRegistry.load(scannerName, serviceFilter, providerFilter,
            getInstance().serviceRegistry.enforceProviderAssignableFromService());
    }

    public static synchronized boolean load(Scanner scanner) {
        return getInstance().serviceRegistry.load(scanner);
    }

    public static synchronized void appendScanner(Scanner scanner) {
        getInstance().serviceRegistry.appendScanner(scanner);
    }

    public static synchronized void setEnforceAssignability(boolean enforceAssignability) {
        getInstance().serviceRegistry.setEnforceProviderAssignableFromService(enforceAssignability);
    }

    /**
     * Interrogate the {@link ServiceRegistry} for the existence of a given service
     * @param serviceClass The service class to locate
     * @param <S>          The service class type
     * @return {@code true} if the service exists; {@code false} otherwise
     */
    public static synchronized <S> boolean hasService(Class<S> serviceClass) {
        return getInstance().serviceRegistry.hasService(serviceClass);
    }

    /**
     * Retrieve all services that match ClassFilter criteria
     * @param serviceClassFilter The service ClassFilter
     * @return a set of services that match a given criteria
     */
    public static synchronized Set<Service> findServices(ClassFilter serviceClassFilter) {
        return getInstance().serviceRegistry.findServices(serviceClassFilter);
    }

    /**
     * Load an instance of the service provider associated with this service
     * @param serviceClass the service class
     * @param <S>          The service type
     * @return an instance of the designated service provider class
     */
    public static synchronized <S, T> T loadService(Class<S> serviceClass) {
        return getInstance().loadServiceInstance(serviceClass);
    }

    /**
     * Load a service instance using a named provider
     * @param serviceClass The service class
     * @param providerName The provider name
     * @param <S>          The service type
     * @return an instance of the designated service provider class
     */
    public static synchronized <S, T> T loadService(Class<S> serviceClass, String providerName) {
        return getInstance().loadServiceInstance(serviceClass, providerName);
    }

    /**
     * Retrieve and instantiate all service providers for a given service
     * @param serviceClass the service class
     * @param <S>          the service class type
     * @param <T>          the service provider types
     * @return A set of service provider instances
     */
    public static synchronized <S, T> Set<T> loadAllServices(Class<S> serviceClass) {
        return getInstance().loadAllServiceInstances(serviceClass);
    }

    /**
     * Specifies whether the underlying {@link ServiceRegistry} has been loaded
     * @return {@code true} if the registry has been loaded; {@code false} otherwise
     */
    public static boolean isLoaded() {
        return getInstance().serviceRegistry.isLoaded();
    }

    /**
     * Returns whether a scanner has been run and loaded into the registry
     * @param scanner The scanner name
     * @return {@code true} if the scanner has been run; false if the scanner does not exist, or has not run. To determine
     *     if a scanner exists on the registry, use {@link #getScanners()} to locate all known scanners.
     */
    public static boolean isLoaded(String scanner) {
        return getInstance().serviceRegistry.isLoaded(scanner);
    }

    /**
     * Retrieve all known scanners in this registry
     * @return A set of all scanner names
     */
    public static Set<String> getScanners() {
        return getInstance().serviceRegistry.getScanners();
    }

    /**
     * Clear and reload the {@link ServiceRegistry} using default filters (loads all services)
     */
    public static synchronized void reloadRegistry() {
        getInstance().serviceRegistry.reload();
    }

    /**
     * Clear and reload the {@link ServiceRegistry} using the specified service and provider filters
     * @param serviceFilter  the service class filter
     * @param providerFilter the provider class filter
     */
    public static synchronized void reloadRegistry(ClassFilter serviceFilter, ClassFilter providerFilter) {
        getInstance().serviceRegistry.reload(serviceFilter, providerFilter);
    }

    /**
     * Internal utility class for locating a service and instantiating a provider
     * @param serviceClass The service class
     * @param <T>          The return type
     * @param <S>          The service type
     * @return The provider instance
     */
    public synchronized <T, S> T loadServiceInstance(Class<S> serviceClass) {
        if (!isLoaded()) {
            load();
        }

        return serviceRegistry.loadServiceProvider(serviceClass);
    }

    /**
     * Internal utility class for locating a service and instantiating a provider using a named provider
     * @param serviceClass The service class
     * @param providerName The provider name
     * @param <T>          The return type
     * @param <S>          The service type
     * @return The provider instance
     */
    public synchronized <T, S> T loadServiceInstance(Class<S> serviceClass, String providerName) {
        if (!isLoaded()) {
            load();
        }

        return serviceRegistry.loadServiceProvider(serviceClass, providerName);
    }

    /**
     * Load all service providers for a given service
     * @param serviceClass the service class
     * @param <S>          the service type
     * @return a set of service provider instances
     */
    public synchronized <S, T> Set<T> loadAllServiceInstances(Class<S> serviceClass) {
        return serviceRegistry.loadAllServiceProviders(serviceClass);
    }


    /**
     * Utility method for testing. Do not use
     */
    public synchronized static void newInstance() {
        instance = null;

        new ServiceManager();
    }
}
