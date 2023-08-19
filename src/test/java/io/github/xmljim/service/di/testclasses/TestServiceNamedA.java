package io.github.xmljim.service.di.testclasses;

import io.github.xmljim.service.di.annotations.ServiceProvider;
import io.github.xmljim.service.di.util.ServiceLifetime;

@ServiceProvider(name = "NamedTestServiceA", lifetime = ServiceLifetime.TRANSIENT)
public class TestServiceNamedA implements ITestServiceA {


    @Override
    public String getName() {
        return "I am TestServiceNamedA, for ITestServiceA";
    }
}
