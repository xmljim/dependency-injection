package io.github.xmljim.service.di.testclasses;

import io.github.xmljim.service.di.annotations.Inject;

public class DummyInjected extends DummyClass {

    public DummyInjected() {
        super();
    }

    @Inject
    private ITeapotService teapotService;

    @Inject
    private ITestServiceA testServiceA;

    public ITeapotService getTeapotService() {
        return teapotService;
    }

    public ITestServiceA getTestServiceA() {
        return testServiceA;
    }
}
