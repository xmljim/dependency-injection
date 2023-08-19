package io.github.xmljim.service.di.testclasses;

import io.github.xmljim.service.di.annotations.Inject;
import io.github.xmljim.service.di.annotations.ServiceProvider;
import io.github.xmljim.service.di.util.ServiceLifetime;

@ServiceProvider(name = "NamedInjectServiceA", lifetime = ServiceLifetime.TRANSIENT)
public class NamedInjectServiceA implements INamedInjectService {

    @Inject(providerName = "NamedTestServiceA")
    private ITestServiceA testServiceA;

    @Override
    public ITestServiceA getTestServiceA() {
        return testServiceA;
    }
}
