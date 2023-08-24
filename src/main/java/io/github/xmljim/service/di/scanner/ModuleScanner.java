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

package io.github.xmljim.service.di.scanner;

import io.github.xmljim.service.di.ServiceManagerException;
import io.github.xmljim.service.di.provider.Provider;
import io.github.xmljim.service.di.provider.Providers;
import io.github.xmljim.service.di.registry.ServiceRegistry;
import io.github.xmljim.service.di.service.Service;
import io.github.xmljim.service.di.service.Services;
import io.github.xmljim.service.di.util.ClassFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.module.ModuleDescriptor;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Scanner for locating services and providers in modules
 */
@SuppressWarnings("unused")
class ModuleScanner extends Scanners {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModuleScanner.class);

    public static final String NAME = "ModuleScanner";

    /**
     * Constructor
     * @param serviceClassFilter                   the service class filter
     * @param providerClassFilter                  the provider class filter
     * @param enforceProviderAssignableFromService apply assignability enforcement between service and provider
     */
    public ModuleScanner(ClassFilter serviceClassFilter, ClassFilter providerClassFilter, boolean enforceProviderAssignableFromService) {
        super(serviceClassFilter, providerClassFilter, enforceProviderAssignableFromService);
    }

    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Scan modules for any {@code provides} declarations
     * @param registry The service registry to append services
     * @return true if the scanner completed successfully.
     */
    @Override
    public synchronized boolean scan(ServiceRegistry registry) {
        LOGGER.debug("Start Scan: {}", getName());
        ModuleLayer moduleLayer = ModuleLayer.boot();


        moduleLayer.modules().forEach(module -> {
            LOGGER.debug("Scanning module: {}", module.getName());
            var moduleDescriptor = module.getDescriptor();

            Set<ModuleDescriptor.Provides> services = moduleDescriptor.provides().stream()
                .filter(p -> getServiceClassFilter().test(loadClass(p.service())
                    .orElseThrow(() -> new ServiceManagerException("Service not found: {}", p.service()))))
                .collect(Collectors.toSet());


            services.forEach(provides -> {
                Class<?> serviceClass = loadClass(provides.service())
                    .orElseThrow(() -> new ServiceManagerException("Service provider not found: {}", provides.service()));
                Service service = findOrGetService(serviceClass, registry);


                provides.providers().stream()
                    .map(provider -> loadClass(provider)
                        .orElseThrow(() -> new ServiceManagerException("Could not load provider: {}", provider)))
                    .filter(getProviderClassFilter())
                    .forEach(providerClass -> {
                        Provider provider = Providers.newProvider(service, providerClass);
                        LOGGER.debug("Append Service Provider: {}", provider);
                        service.appendProvider(provider);
                    });

                LOGGER.debug("Appending Service: {}", service);
                registry.appendService(service);
            });
        });

        return true;
    }

    private Service findOrGetService(Class<?> serviceClass, ServiceRegistry serviceRegistry) {
        return serviceRegistry.findService(serviceClass)
            .orElse(Services.newService(serviceClass, serviceRegistry, enforceProviderAssignableFromService()));
    }

}
