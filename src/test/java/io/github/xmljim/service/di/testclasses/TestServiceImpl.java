package io.github.xmljim.service.di.testclasses;

import io.github.xmljim.service.di.provider.Provider;
import io.github.xmljim.service.di.registry.ServiceRegistry;
import io.github.xmljim.service.di.service.Services;

import java.util.Optional;

public class TestServiceImpl extends Services {
    public TestServiceImpl(Class<?> serviceClass, ServiceRegistry serviceRegistry, boolean enforceAssignableFrom) {
        super(serviceClass, serviceRegistry, enforceAssignableFrom);
    }

    @Override
    public Optional<Provider> getProvider() {
        return Optional.empty();
    }
}
