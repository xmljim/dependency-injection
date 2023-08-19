package io.github.xmljim.service.di.testclasses;

import io.github.xmljim.service.di.annotations.Inject;

public class DummyComboInjection extends DummyClass {

    private final ITestServiceA testServiceA;

    @Inject
    private ITeapotService teapotService;

    public DummyComboInjection(ITestServiceA testServiceA) {
        super();
        this.testServiceA = testServiceA;
    }

    public ITestServiceA getTestServiceA() {
        return testServiceA;
    }

    public ITeapotService getTeapotService() {
        return teapotService;
    }
}
