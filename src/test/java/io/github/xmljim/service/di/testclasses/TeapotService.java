package io.github.xmljim.service.di.testclasses;

public class TeapotService implements ITeapotService {

    @Override
    public String teapot() {
        return "I'm a little teapot";
    }
}
