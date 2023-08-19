package io.github.xmljim.service.di.testclasses;

import io.github.xmljim.service.di.annotations.ServiceProvider;
import io.github.xmljim.service.di.util.ServiceLifetime;

@ServiceProvider(name = "NamedTestServiceB", lifetime = ServiceLifetime.TRANSIENT, priority = 100)
public class TestServiceNamedB implements ITestServiceA {
    @Override
    public String getName() {
        return "I am TestServiceNamedB for ITestServiceA";
    }
}
