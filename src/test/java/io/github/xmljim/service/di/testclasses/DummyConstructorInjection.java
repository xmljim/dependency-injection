package io.github.xmljim.service.di.testclasses;

public class DummyConstructorInjection extends DummyClass {

    private final ITestServiceA testServiceA;
    private final ITeapotService teapotService;

    public DummyConstructorInjection(ITestServiceA testServiceA, ITeapotService teapotService) {
        super();
        this.testServiceA = testServiceA;
        this.teapotService = teapotService;
    }

    public ITestServiceA getTestServiceA() {
        return testServiceA;
    }

    public ITeapotService getTeapotService() {
        return teapotService;
    }
}
