package io.github.xmljim.service.di.testclasses;

import io.github.xmljim.service.di.annotations.DependencyInjection;

public class InjectedServiceA implements IInjectedServiceA {

    private ITeapotService teapotService;

    public InjectedServiceA() {

    }

    @DependencyInjection
    public InjectedServiceA(ITeapotService teapotService) {
        this.teapotService = teapotService;
    }

    @Override
    public boolean getInjected() {
        return teapotService != null;
    }

    @Override
    public String saySomething() {
        return teapotService.teapot();
    }
}
