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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.xmljim.service.di.ServiceManagerException;
import io.github.xmljim.service.di.provider.Provider;
import io.github.xmljim.service.di.provider.Providers;
import io.github.xmljim.service.di.registry.ServiceRegistry;
import io.github.xmljim.service.di.service.Service;
import io.github.xmljim.service.di.service.Services;
import io.github.xmljim.service.di.util.ClassFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * Scanner for non-modular resources on a classpath
 */
@SuppressWarnings("unused")
class ClasspathScanner extends Scanners {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClasspathScanner.class);

    private static final String SERVICE_RESOURCE = "META-INF/services/";
    private static final String SERVICE_PATH = "/" + SERVICE_RESOURCE;

    public ClasspathScanner(ClassFilter serviceClassFilter, ClassFilter providerClassFilter, boolean enforceProviderAssignableFromService) {
        super(serviceClassFilter, providerClassFilter, enforceProviderAssignableFromService);
    }

    @Override
    public String getName() {
        return Scanners.CLASSPATH;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean scan(ServiceRegistry serviceRegistry) {
        LOGGER.debug("Start Scan: {}", getName());

        var classLoader = ClassLoader.getSystemClassLoader();
        classLoader.resources(SERVICE_RESOURCE).forEach(resource -> {
            if (isJarFile(resource)) {
                loadJarServices(resource, serviceRegistry);
            } else {
                try {
                    Path localFile = Paths.get(resource.toURI());
                    traverseAndLoadServiceDirectory(localFile, serviceRegistry);
                } catch (URISyntaxException e) { //this is silly. It's from a URL, which makes it a valid URI
                    throw new ServiceManagerException(e.getMessage(), e);
                }
            }
        });

        LOGGER.debug("Scan complete: {}", getName());
        return true;
    }

    /**
     * Pretty straightforward, the jar URLs start with 'jar:'
     * @param url The resource URL
     * @return {@code true} if it's a jar file
     */
    private boolean isJarFile(URL url) {
        return url.toString().startsWith("jar:");
    }

    /**
     * Load the jar file and open the META-INF/services folder for traversal
     * @param jarUrl          The jar resource url
     * @param serviceRegistry The service registry to hold any services found
     */
    private void loadJarServices(URL jarUrl, ServiceRegistry serviceRegistry) {
        try {

            Path pathJar = Paths.get(new URI(jarUrl.getFile().substring(0, jarUrl.getFile().indexOf('!'))));
            LOGGER.debug("Loading Jar file: {}", pathJar);

            //open up the jar file, locate the META-INF/services folder and go
            try (FileSystem jarfs = FileSystems.newFileSystem(pathJar)) {
                Path servicePath = jarfs.getPath(SERVICE_PATH);
                traverseAndLoadServiceDirectory(servicePath, serviceRegistry);
            }
        } catch (Exception e) {
            throw new ServiceManagerException(e.getMessage(), e);
        }
    }

    /**
     * Since the path is contextually attached to the underlying filesystem where it originated from, we
     * can easily traverse the META-INF/services folder for any files.  In this scheme, the service name
     * is the filename, and the contents of the file are the providers, one on each line
     * @param path            The META-INF/services folder
     * @param serviceRegistry The service registry which will hold the services
     */
    @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE")
    private void traverseAndLoadServiceDirectory(Path path, ServiceRegistry serviceRegistry) {
        try (Stream<Path> servicePath = Files.walk(path)) {
            servicePath.filter(Files::isRegularFile).forEach(serviceFile -> {
                loadClass(serviceFile.getFileName().toString())
                    .ifPresentOrElse(svcClass -> {
                        if (getServiceClassFilter().test(svcClass)) {
                            //it's possible that the service was already loaded, so we'll either locate the existing
                            //service or create a new one
                            Service service = serviceRegistry.findService(svcClass)
                                .orElse(Services.newService(svcClass, serviceRegistry, enforceProviderAssignableFromService()));

                            readServiceFile(serviceFile).forEach(line -> {
                                loadClass(line).ifPresentOrElse(providerClass -> {
                                    if (getProviderClassFilter().test(providerClass)) {
                                        //same with service, we only want to append a provider if it doesn't already exist
                                        if (!service.hasProvider(providerClass)) {
                                            Provider provider = Providers.newProvider(service, providerClass);
                                            service.appendProvider(provider);
                                        }
                                    }
                                }, () -> LOGGER.warn("Service provider class not found for service: [service={}, provider={}]",
                                    svcClass, line));
                            });

                            if (service.getProviders().findAny().isPresent()) {
                                LOGGER.debug("Appending service: {}", service);
                                serviceRegistry.appendService(service);
                            }
                        }

                    }, () -> LOGGER.warn("Service class definition not found: {}", serviceFile.getFileName().toString()));
            });
        } catch (Exception e) {
            throw new ServiceManagerException(e.getMessage(), e);
        }
    }

    private List<String> readServiceFile(Path file) {
        try {
            return Files.readAllLines(file)
                .stream()
                .filter(line -> !line.startsWith("#"))
                .toList();
        } catch (IOException ioException) {
            LOGGER.error("Failed to read service file: {}", file);
            return Collections.emptyList();
        }
    }
}
