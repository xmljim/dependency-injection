package io.github.xmljim.service.di.test;

import io.github.xmljim.service.di.ServiceManagerException;
import io.github.xmljim.service.di.annotations.ServiceProvider;
import io.github.xmljim.service.di.provider.Provider;
import io.github.xmljim.service.di.provider.Providers;
import io.github.xmljim.service.di.registry.ServiceRegistries;
import io.github.xmljim.service.di.registry.ServiceRegistry;
import io.github.xmljim.service.di.service.Service;
import io.github.xmljim.service.di.service.Services;
import io.github.xmljim.service.di.testclasses.*;
import io.github.xmljim.service.di.util.ServiceLifetime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ProviderTests {

    @Test
    @DisplayName("Given an unannotated provider class, should return a provider instance with default name and lifetime")
    void testCreateDefaultProvider() {
        ServiceRegistry serviceRegistry = ServiceRegistries.newServiceRegistry();
        Service service = Services.newService(IDummyInterface.class, serviceRegistry);
        Provider provider = Providers.newProvider(service, DummyClass.class);

        assertNotNull(provider);
        assertEquals(DummyClass.class, provider.getProviderClass());
        assertEquals(DummyClass.class.getName(), provider.getName());
        assertEquals(ServiceLifetime.TRANSIENT, provider.getServiceLifetime());
    }

    @Test
    @DisplayName("Given an annotated provider class, should return a provider with the specified name and lifetime")
    void testCreateNamedProvider() {
        ServiceRegistry serviceRegistry = ServiceRegistries.newServiceRegistry();
        Service service = Services.newService(IDummyInterface.class, serviceRegistry);
        Provider provider = Providers.newProvider(service, NamedDummyClass.class);

        assertTrue(NamedDummyClass.class.isAnnotationPresent(ServiceProvider.class));
        ServiceProvider serviceProvider = NamedDummyClass.class.getAnnotation(ServiceProvider.class);
        assertNotNull(serviceProvider);

        assertNotNull(provider);
        assertEquals(NamedDummyClass.class, provider.getProviderClass());
        assertEquals(serviceProvider.name(), provider.getName());
        assertEquals(serviceProvider.lifetime(), provider.getServiceLifetime());
    }

    @Test
    @DisplayName("Given an unannotated provider, should return new instances for each getInstance request")
    void testGetUnnamedTransientProviderInstance() {
        ServiceRegistry serviceRegistry = ServiceRegistries.newServiceRegistry();
        Service service = Services.newService(IDummyInterface.class, serviceRegistry);
        Provider provider = Providers.newProvider(service, DummyClass.class);


        IDummyInterface firstInstance = provider.getInstance();
        assertNotNull(firstInstance);
        UUID firstUuid = firstInstance.identity();

        IDummyInterface secondInstance = provider.getInstance();
        assertNotNull(secondInstance);
        UUID secondUuid = secondInstance.identity();

        assertNotEquals(firstUuid, secondUuid);
    }

    @Test
    @DisplayName("Given an annotated provider, should return the same instance for all getInstance requests")
    void testGetNamedSingletonProviderInstance() {
        ServiceRegistry serviceRegistry = ServiceRegistries.newServiceRegistry();
        Service service = Services.newService(IDummyInterface.class, serviceRegistry);
        Provider provider = Providers.newProvider(service, NamedDummyClass.class);

        IDummyInterface firstInstance = provider.getInstance();
        assertNotNull(firstInstance);
        UUID firstUuid = firstInstance.identity();

        IDummyInterface secondInstance = provider.getInstance();
        assertNotNull(secondInstance);
        UUID secondUuid = secondInstance.identity();

        assertEquals(firstUuid, secondUuid);
    }

    @Test
    @DisplayName("Given a provider with fields requiring injection, should return instance with fields with injected instances")
    void testGetInjectedProviderInstance() {

        ServiceRegistry serviceRegistry = ServiceRegistries.newServiceRegistry();

        //test service a
        Service serviceA = Services.newService(ITestServiceA.class, serviceRegistry);
        Provider providerA = Providers.newProvider(serviceA, TestServiceA.class);
        serviceA.appendProvider(providerA);
        serviceRegistry.appendService(serviceA);

        //teapot service
        Service serviceTeapot = Services.newService(ITeapotService.class, serviceRegistry);
        Provider teapot = Providers.newProvider(serviceTeapot, TeapotService.class);
        serviceTeapot.appendProvider(teapot);
        serviceRegistry.appendService(serviceTeapot);

        //now the fun class
        Service dummyService = Services.newService(IDummyInterface.class, serviceRegistry);
        Provider dummyProvider = Providers.newProvider(dummyService, DummyInjected.class);
        dummyService.appendProvider(dummyProvider);
        serviceRegistry.appendService(dummyService);

        IDummyInterface testService = dummyProvider.getInstance();
        assertNotNull(testService);

        var castedDummyService = (DummyInjected) testService;

        assertNotNull(castedDummyService.getTestServiceA());
        assertNotNull(castedDummyService.getTeapotService());
        assertNotNull(castedDummyService.identity());

        assertEquals("My Name is Test Service A", castedDummyService.getTestServiceA().getName());
        assertEquals("I'm a little teapot", castedDummyService.getTeapotService().teapot());
    }

    @Test
    @DisplayName("Given a provider with constructor dependencies, should return instance with non-null dependencies")
    void testGetConstructorInjectedProviderInstance() {
        ServiceRegistry serviceRegistry = ServiceRegistries.newServiceRegistry();

        //test service a
        Service serviceA = Services.newService(ITestServiceA.class, serviceRegistry);
        Provider providerA = Providers.newProvider(serviceA, TestServiceA.class);
        serviceA.appendProvider(providerA);
        serviceRegistry.appendService(serviceA);

        //teapot service
        Service serviceTeapot = Services.newService(ITeapotService.class, serviceRegistry);
        Provider teapot = Providers.newProvider(serviceTeapot, TeapotService.class);
        serviceTeapot.appendProvider(teapot);
        serviceRegistry.appendService(serviceTeapot);

        //now the fun class
        Service dummyService = Services.newService(IDummyInterface.class, serviceRegistry);
        Provider dummyProvider = Providers.newProvider(dummyService, DummyConstructorInjection.class);
        dummyService.appendProvider(dummyProvider);
        serviceRegistry.appendService(dummyService);

        IDummyInterface testService = dummyProvider.getInstance();
        assertNotNull(testService);

        var castedDummyService = (DummyConstructorInjection) testService;

        assertNotNull(castedDummyService.getTestServiceA());
        assertNotNull(castedDummyService.getTeapotService());
        assertNotNull(castedDummyService.identity());

        assertEquals("My Name is Test Service A", castedDummyService.getTestServiceA().getName());
        assertEquals("I'm a little teapot", castedDummyService.getTeapotService().teapot());
    }

    @Test
    @DisplayName("Given a provider with a combination of constructor and field dependencies, should return instance with all dependencies")
    void testGetComboInjectedProviderInstance() {
        ServiceRegistry serviceRegistry = ServiceRegistries.newServiceRegistry();

        //test service a
        Service serviceA = Services.newService(ITestServiceA.class, serviceRegistry);
        Provider providerA = Providers.newProvider(serviceA, TestServiceA.class);
        serviceA.appendProvider(providerA);
        serviceRegistry.appendService(serviceA);

        //teapot service
        Service serviceTeapot = Services.newService(ITeapotService.class, serviceRegistry);
        Provider teapot = Providers.newProvider(serviceTeapot, TeapotService.class);
        serviceTeapot.appendProvider(teapot);
        serviceRegistry.appendService(serviceTeapot);

        //now the fun class
        Service dummyService = Services.newService(IDummyInterface.class, serviceRegistry);
        Provider dummyProvider = Providers.newProvider(dummyService, DummyComboInjection.class);
        dummyService.appendProvider(dummyProvider);
        serviceRegistry.appendService(dummyService);

        IDummyInterface testService = dummyProvider.getInstance();
        assertNotNull(testService);

        var castedDummyService = (DummyComboInjection) testService;

        assertNotNull(castedDummyService.getTestServiceA());
        assertNotNull(castedDummyService.getTeapotService());
        assertNotNull(castedDummyService.identity());

        assertEquals("My Name is Test Service A", castedDummyService.getTestServiceA().getName());
        assertEquals("I'm a little teapot", castedDummyService.getTeapotService().teapot());
    }

    @Test
    @DisplayName("Given a service that does not enforce assignability, should create provider")
    void testDoNotEnforceAssignableProvider() {
        ServiceRegistry serviceRegistry = ServiceRegistries.newServiceRegistry();

        Service service = Services.newService(ITeapotService.class, serviceRegistry);
        Provider nonAssignable = Providers.newProvider(service, DummyClass.class);
        service.appendProvider(nonAssignable);

        assertNotNull(nonAssignable);
    }

    @Test
    @DisplayName("Given a service that enforces assignability, should throw exception")
    void testEnforceAssignableProvider() {
        ServiceRegistry serviceRegistry = ServiceRegistries.newServiceRegistry();

        Service service = Services.newService(ITeapotService.class, serviceRegistry, true);

        assertThrows(ServiceManagerException.class, () -> Providers.newProvider(service, DummyClass.class));
    }

    @Test
    void testNamedInjectFieldsProviderInstance() {
        ServiceRegistry serviceRegistry = ServiceRegistries.newServiceRegistry();

        Service service = Services.newService(INamedInjectService.class, serviceRegistry, true);
        Provider providerA = Providers.newProvider(service, NamedInjectServiceA.class);
        service.appendProvider(providerA);

        Provider providerB = Providers.newProvider(service, NamedInjectServiceB.class);
        service.appendProvider(providerB);

        serviceRegistry.appendService(service);

        Service testService = Services.newService(ITestServiceA.class, serviceRegistry);
        Provider testAProvider = Providers.newProvider(testService, TestServiceNamedA.class);
        testService.appendProvider(testAProvider);

        Provider testBProvider = Providers.newProvider(testService, TestServiceNamedB.class);
        testService.appendProvider(testBProvider);

        serviceRegistry.appendService(testService);


        INamedInjectService testAService = providerA.getInstance();
        assertNotNull(testAService.getTestServiceA());
        assertEquals("I am TestServiceNamedA, for ITestServiceA", testAService.getTestServiceA().getName());


        INamedInjectService testBService = providerB.getInstance();
        assertNotNull(testBService.getTestServiceA());
        assertEquals("I am TestServiceNamedB for ITestServiceA", testBService.getTestServiceA().getName());
    }
}
