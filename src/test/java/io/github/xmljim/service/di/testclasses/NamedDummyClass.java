package io.github.xmljim.service.di.testclasses;

import io.github.xmljim.service.di.annotations.ServiceProvider;
import io.github.xmljim.service.di.util.ServiceLifetime;

import java.util.UUID;

@ServiceProvider(name = "NamedDummy", lifetime = ServiceLifetime.SINGLETON)
public class NamedDummyClass implements IDummyInterface {

    private final UUID uuid;

    public NamedDummyClass() {
        uuid = UUID.randomUUID();
    }

    @Override
    public UUID identity() {
        return uuid;
    }
}
