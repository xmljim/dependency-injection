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

import io.github.xmljim.service.di.ServiceManagerException;
import io.github.xmljim.service.di.annotations.ServiceProvider;
import io.github.xmljim.service.di.scanner.Scanner;
import io.github.xmljim.service.di.scanner.Scanners;
import io.github.xmljim.service.di.service.Service;
import io.github.xmljim.service.di.util.ClassFilter;
import io.github.xmljim.service.di.util.ServiceLifetime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Stream;

/**
 * The default ServiceRegistry implementation
 */
@SuppressWarnings("unused")
@ServiceProvider(name = "DefaultServiceRegistry", lifetime = ServiceLifetime.SINGLETON)
public class ServiceRegistryImpl extends ServiceRegistries {
    private boolean loaded;

    private final Map<String, Class<? extends Scanner>> scannerMap = new HashMap<>();

    private final Map<String, Boolean> scannerLoadStatus = new HashMap<>();

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final Set<Service> services = new HashSet<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRegistryImpl.class);

    /**
     * Constructor
     */
    public ServiceRegistryImpl() {
        super();
        scannerMap.put(Scanners.MODULE, Scanners.getModuleScannerClass());
        scannerMap.put(Scanners.CLASSPATH, Scanners.getClasspathScannerClass());
    }

    public Set<String> getScanners() {
        return scannerMap.keySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean isLoaded() {
        return loaded;
    }

    public synchronized boolean isLoaded(String scanner) {
        return scannerLoadStatus.getOrDefault(scanner, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void load(ClassFilter serviceFilter, ClassFilter providerFilter) {
        scannerMap.forEach((key, value) -> scannerLoadStatus.put(key,
            Scanners.newScanner(value, serviceFilter, providerFilter, enforceProviderAssignableFromService())
                .scan(this)));

        loaded = scannerLoadStatus.values().stream().allMatch(b -> b);
    }

    @Override
    public synchronized boolean load(String scannerName, ClassFilter serviceFilter, ClassFilter providerFilter,
        boolean enforceProviderAssignableFromService) {


        var scanner = Optional.ofNullable(scannerMap.get(scannerName));
        var loaded = scanner
            .map(s -> Scanners.newScanner(s, serviceFilter, providerFilter, enforceProviderAssignableFromService)
                .scan(this))
            .orElseThrow(() -> new ServiceManagerException("No Scanner found with name %s", scannerName));

        scanner.ifPresent(s -> scannerLoadStatus.put(s.getName(), loaded));
        return loaded;
    }

    @Override
    public synchronized boolean load(Scanner scanner) {
        return scanner.scan(this);
    }

    @Override
    public synchronized void appendScanner(Scanner scanner) {
        scannerMap.put(scanner.getName(), scanner.getClass());
    }

    @Override
    public <S extends Scanner> void appendScanner(String name, Class<S> scannerClass) {
        LOGGER.debug("appending scanner: {} - {}", name, scannerClass);
        scannerMap.put(name, scannerClass);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void reload(ClassFilter serviceFilter, ClassFilter providerFilter) {
        LOGGER.debug("reload services");
        loaded = false;
        clearServices();
        load(serviceFilter, providerFilter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Stream<Service> services() {
        return services.stream();
    }

    /**
     * Remove all stored service references
     */
    public void clearServices() {
        LOGGER.debug("Clearing all services");
        services.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void appendService(Service service) {
        if (services().noneMatch(s -> s.getServiceClass().equals(service.getServiceClass()))) {
            LOGGER.debug("Service Added: {}", service);
            services.add(service);
        }
    }
}
