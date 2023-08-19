package io.github.xmljim.service.di.testclasses;

import java.util.UUID;

public class DummyClass implements IDummyInterface {

    private final UUID uuid;

    public DummyClass() {
        uuid = UUID.randomUUID();
    }

    @Override
    public UUID identity() {
        return uuid;
    }
}
