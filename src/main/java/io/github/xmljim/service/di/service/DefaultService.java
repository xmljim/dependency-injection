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

import io.github.xmljim.service.di.annotations.ServiceProvider;
import io.github.xmljim.service.di.provider.Provider;
import io.github.xmljim.service.di.registry.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Default service implementation.
 */
class DefaultService extends Services {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultService.class);

    private final Set<Provider> providers = new HashSet<>();

    @SuppressWarnings("unused")
    public DefaultService(Class<?> serviceClass, ServiceRegistry serviceRegistry) {
        super(serviceClass, serviceRegistry);
    }

    @SuppressWarnings("unused")
    public DefaultService(Class<?> serviceClass, ServiceRegistry serviceRegistry, boolean enforceAssignableFrom) {
        super(serviceClass, serviceRegistry, enforceAssignableFrom);
    }


    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unused")
    @Override
    public Optional<Provider> getProvider() {
        LOGGER.debug("Get Provider");
        Comparator<Provider> comparePriority = Comparator.comparingInt(p -> p.getProviderClass().getAnnotation(ServiceProvider.class).priority());
        var provider = getProviders()
            .filter(p -> p.getProviderClass().isAnnotationPresent(ServiceProvider.class)) //preferred option
            .max(comparePriority)
            .or(() -> getProviders().findFirst());

        LOGGER.debug("Provider found: {}", provider);
        return provider;
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
    public void appendProvider(Provider provider) {
        providers.add(provider);
    }

}
