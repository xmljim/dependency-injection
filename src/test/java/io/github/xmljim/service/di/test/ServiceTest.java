package io.github.xmljim.service.di.test;

import io.github.xmljim.service.di.provider.Providers;
import io.github.xmljim.service.di.registry.ServiceRegistries;
import io.github.xmljim.service.di.service.Services;
import io.github.xmljim.service.di.testclasses.DummyClass;
import io.github.xmljim.service.di.testclasses.IDummyInterface;
import io.github.xmljim.service.di.testclasses.ITeapotService;
import io.github.xmljim.service.di.testclasses.NamedDummyClass;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiceTest {

    @Test
    void appendProvider() {
        var serviceRegistry = ServiceRegistries.newServiceRegistry();
        var service = Services.newService(IDummyInterface.class, serviceRegistry);
        var provider = Providers.newProvider(service, NamedDummyClass.class);
        service.appendProvider(provider);

        assertTrue(service.hasProvider(NamedDummyClass.class));

    }

    @Test
    void getProvider() {
        var serviceRegistry = ServiceRegistries.newServiceRegistry();
        var service = Services.newService(IDummyInterface.class, serviceRegistry);
        var provider = Providers.newProvider(service, NamedDummyClass.class);
        service.appendProvider(provider);
        //add a second one
        service.appendProvider(Providers.newProvider(service, DummyClass.class));

        assertTrue(service.getProvider("NamedDummy").isPresent());
        //shows that even though we have two providers, the one with the @ServiceProvider annotation
        //will be given preference
        assertTrue(service.getProvider().map(p -> p.getName().equals("NamedDummy")).orElse(false));

        //but just to show we _could_ get the unannotated instance, we'll query by the provider class name
        assertTrue(service.getProvider(DummyClass.class.getName()).isPresent());
    }

    @Test
    void getServiceClass() {
        var serviceRegistry = ServiceRegistries.newServiceRegistry();
        var service = Services.newService(IDummyInterface.class, serviceRegistry);

        assertEquals(IDummyInterface.class, service.getServiceClass());
    }

    @Test
    void enforceAssignableFromProvider() {

        //we'll try it from two angles: the service registry and the service itself

        var serviceRegistry = ServiceRegistries.newServiceRegistry(true);
        var service = Services.newService(IDummyInterface.class, serviceRegistry);
        assertTrue(service.enforceAssignableFromProvider());

        //now let's override it for another service
        var service2 = Services.newService(ITeapotService.class, serviceRegistry, false);
        assertFalse(service2.enforceAssignableFromProvider());

        //to show there's nothing up our sleeves, show that the service registry's enforcement is true
        assertTrue(service.enforceAssignableFromProvider());
    }
}