package io.github.xmljim.service.di.testclasses;

import io.github.xmljim.service.di.annotations.DependencyInjection;

public class MyExampleClassArgs {

    private final ITeapotService teapotService;
    private final String name;
    private final int repeat;

    @DependencyInjection
    public MyExampleClassArgs(ITeapotService teapotService, String name, int repeat) {
        this.teapotService = teapotService;
        this.name = name;
        this.repeat = repeat;
    }

    public ITeapotService getTeapotService() {
        return teapotService;
    }

    public String getName() {
        return name;
    }

    public String echoName() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < repeat; i++) {
            builder.append(getName()).append(System.lineSeparator());
        }
        return builder.toString();
    }
}
