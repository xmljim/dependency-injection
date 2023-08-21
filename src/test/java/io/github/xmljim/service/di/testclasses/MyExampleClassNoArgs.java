package io.github.xmljim.service.di.testclasses;

public class MyExampleClassNoArgs {

    private final ITeapotService teapotService;

    public MyExampleClassNoArgs(ITeapotService teapotService) {
        this.teapotService = teapotService;
    }

    public ITeapotService getTeapotService() {
        return teapotService;
    }
}
