package io.github.xmljim.service.di.testclasses;

import io.github.xmljim.service.di.annotations.Inject;
import io.github.xmljim.service.di.annotations.ServiceProvider;
import io.github.xmljim.service.di.util.ServiceLifetime;

@ServiceProvider(name = "NamedInjectServiceB", lifetime = ServiceLifetime.TRANSIENT)
public class NamedInjectServiceB implements INamedInjectService {

    @Inject(providerName = "NamedTestServiceB")
    private ITestServiceA testServiceA;

    @Override
    public ITestServiceA getTestServiceA() {
        return testServiceA;
    }
}
