package io.github.xmljim.service.di.testclasses;

import io.github.xmljim.service.di.provider.Provider;
import io.github.xmljim.service.di.registry.ServiceRegistry;
import io.github.xmljim.service.di.service.Services;

import java.util.Optional;
import java.util.stream.Stream;

public class TestServiceImpl extends Services {
    public TestServiceImpl(Class<?> serviceClass, ServiceRegistry serviceRegistry, boolean enforceAssignableFrom) {
        super(serviceClass, serviceRegistry, enforceAssignableFrom);
    }

    @Override
    public Optional<Provider> getProvider() {
        return Optional.empty();
    }

    /**
     * Return a stream of all providers
     * @return The Provider stream
     */
    public Stream<Provider> getProviders() {
        return Stream.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void appendProvider(Provider provider) {

    }
}
