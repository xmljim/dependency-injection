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

package io.github.xmljim.service.di.provider;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.xmljim.service.di.ServiceManagerException;
import io.github.xmljim.service.di.annotations.Generated;
import io.github.xmljim.service.di.service.Service;
import io.github.xmljim.service.di.util.ServiceLifetime;

import java.lang.reflect.Constructor;
import java.util.Objects;

/**
 * Abstract Provider implementation. It is intended to be extended
 */
public abstract class Providers implements Provider {

    private static final Class<? extends Provider> DEFAULT = DefaultProvider.class;
    private static Class<? extends Provider> useProvider;
    private final Class<?> providerClass;
    private final Service service;
    private ServiceLifetime serviceLifetime;
    private String name;

    /**
     * Create a new provider
     * @param service       The service that will hold this provider
     * @param providerClass The provider class
     * @return a new default provider instance
     */
    public static Provider newProvider(Service service, Class<?> providerClass) {
        //return new DefaultProvider(service, providerClass);
        return newProvider(getUseProvider(), service, providerClass);
    }

    /**
     * Extension method for creating a provider instance using a defined Provider implementation.
     * <p>
     * The only requirement for any implementation class to use this method is that it must have a constructor
     * containing
     * {@link Service} and {@link Class} parameters in that order
     * </p>
     * @param implClass     The implementation class. It must extend {@link Provider}
     * @param service       The service that will hold this provider
     * @param providerClass The provider class
     * @param <P>           The provider implementation class type
     * @return a provider instance using the defined class
     */
    public static <P extends Provider> P newProvider(Class<P> implClass, Service service, Class<?> providerClass) {
        try {
            Constructor<P> ctor = implClass.getDeclaredConstructor(Service.class, Class.class);
            return ctor.newInstance(service, providerClass);
        } catch (Exception e) {
            throw new ServiceManagerException(e.getMessage(), e);
        }
    }

    /**
     * Statically set the Provider class type
     * @param provider the provider class type
     */
    public static void setUseProviderClass(Class<? extends Provider> provider) {
        useProvider = provider;
    }

    private static Class<? extends Provider> getUseProvider() {
        return useProvider == null ? DEFAULT : useProvider;
    }

    /**
     * Constructor to initialize service and provider class
     * @param service       the service that holds this provider
     * @param providerClass the provider class
     */
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public Providers(Service service, Class<?> providerClass) {
        this.service = service;
        this.providerClass = providerClass;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getProviderClass() {
        return providerClass;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServiceLifetime getServiceLifetime() {
        return serviceLifetime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setServiceLifetime(ServiceLifetime lifetime) {
        this.serviceLifetime = lifetime;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressFBWarnings("EI_EXPOSE_REP")
    @Override
    public Service getService() {
        return service;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Set the provider name
     * @param name the name to apply to this provider
     */
    public void setName(String name) {
        this.name = name;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @Generated
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultProvider provider = (DefaultProvider) o;
        return Objects.equals(service, provider.getService())
            && Objects.equals(providerClass, provider.getProviderClass())
            && Objects.equals(name, provider.getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(service, providerClass, name);
    }

    @Override
    @Generated
    public String toString() {
        return "Provider{" +
            "service=" + service +
            ", providerClass=" + providerClass +
            ", name='" + name + '\'' +
            ", lifetime=" + serviceLifetime +
            '}';
    }
}
