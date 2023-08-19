package io.github.xmljim.service.di.test;

import io.github.xmljim.service.di.provider.Providers;
import io.github.xmljim.service.di.service.Service;

public class TestProviderImpl extends Providers {
    /**
     * Constructor to initialize service and provider class
     * @param service       the service that holds this provider
     * @param providerClass the provider class
     */
    public TestProviderImpl(Service service, Class<?> providerClass) {
        super(service, providerClass);
    }

    @Override
    public <T> T getInstance() {
        return null;
    }
}
