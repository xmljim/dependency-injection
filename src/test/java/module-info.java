import io.github.xmljim.service.di.testclasses.*;

module xmljim.dependency.injection.test {

    requires xmljim.dependency.injection;
    requires org.junit.jupiter.api;
    requires org.junit.jupiter.engine;

    opens io.github.xmljim.service.di.test;
    opens io.github.xmljim.service.di.testclasses;
    exports io.github.xmljim.service.di.testclasses; //needed for services

    provides ITeapotService with TeapotService;
    provides ITestServiceA with TestServiceA, TestServiceNamedA, TestServiceNamedB; //you can have multiple providers
    provides IInjectedServiceA with InjectedServiceA;

}