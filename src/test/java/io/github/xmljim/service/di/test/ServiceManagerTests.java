package io.github.xmljim.service.di.test;

import io.github.xmljim.service.di.RegistryBootstrap;
import io.github.xmljim.service.di.ServiceManager;
import io.github.xmljim.service.di.registry.ServiceRegistries;
import io.github.xmljim.service.di.testclasses.*;
import io.github.xmljim.service.di.util.ClassFilters;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestClassOrder(value = ClassOrderer.OrderAnnotation.class)
public class ServiceManagerTests {


    @Test
    @Order(1)
    @DisplayName("Given a service manager, and a load is invoked, should return true after completing load")
    void testLoadManager() {
        ServiceManager.newInstance();
        ServiceManager.load();
        assertTrue(ServiceManager.isLoaded());
        assertTrue(ServiceManager.hasService(ITestServiceA.class));
        assertTrue(ServiceManager.hasService(IInjectedServiceA.class));
        assertTrue(ServiceManager.hasService(ITeapotService.class));

    }

    @Test
    @Order(2)
    @DisplayName("Given a service manager, and load is invoked with filters, should only services that meet filter criteria")
    void testLoadWithFilters() {
        ServiceManager.newInstance();
        assertFalse(ServiceManager.isLoaded());
        ServiceManager.load(ClassFilters.implementsInterface(MyTestServices.class), ClassFilters.hasServiceProviderAnnotation());
        assertTrue(ServiceManager.isLoaded());
        assertTrue(ServiceManager.hasService(ITestServiceA.class));
        assertTrue(ServiceManager.hasService(IInjectedServiceA.class));
        assertFalse(ServiceManager.hasService(ITeapotService.class));
    }

    @Test
    @Order(3)
    @DisplayName("Given a loaded service manager, reload should return isLoaded=true with the expected services")
    void testReload() {
        ServiceManager.newInstance();
        assertFalse(ServiceManager.isLoaded());
        ServiceManager.load(ClassFilters.implementsInterface(MyTestServices.class), ClassFilters.hasServiceProviderAnnotation());
        assertTrue(ServiceManager.isLoaded());
        assertTrue(ServiceManager.hasService(ITestServiceA.class));
        assertTrue(ServiceManager.hasService(IInjectedServiceA.class));
        assertFalse(ServiceManager.hasService(ITeapotService.class));


        ServiceManager.reloadRegistry();
        assertTrue(ServiceManager.isLoaded());
        assertTrue(ServiceManager.hasService(ITestServiceA.class));
        assertTrue(ServiceManager.hasService(IInjectedServiceA.class));
        assertTrue(ServiceManager.hasService(ITeapotService.class));
    }

    @Test
    @Order(4)
    @DisplayName("Given a loaded service manager, reload with filters should return isLoaded=true with the expected services")
    void testReloadWithFilters() {
        ServiceManager.newInstance();
        ServiceManager.load();
        assertTrue(ServiceManager.isLoaded());
        assertTrue(ServiceManager.hasService(ITestServiceA.class));
        assertTrue(ServiceManager.hasService(IInjectedServiceA.class));
        assertTrue(ServiceManager.hasService(ITeapotService.class));


        ServiceManager.reloadRegistry(ClassFilters.implementsInterface(MyTestServices.class), ClassFilters.hasServiceProviderAnnotation());
        assertTrue(ServiceManager.isLoaded());
        assertTrue(ServiceManager.hasService(ITestServiceA.class));
        assertTrue(ServiceManager.hasService(IInjectedServiceA.class));
        assertFalse(ServiceManager.hasService(ITeapotService.class));
    }

    @Test
    @Order(5)
    @DisplayName("Given a service manager and load is called, should return true for any service loaded")
    void testHasService() {
        ServiceManager.newInstance();
        ServiceManager.load();
        assertTrue(ServiceManager.hasService(ITeapotService.class));
        assertTrue(ServiceManager.hasService(ITestServiceA.class));
        assertTrue(ServiceManager.hasService(IInjectedServiceA.class));
        assertFalse(ServiceManager.hasService(IDummyInterface.class));
    }

    @Test
    @Order(6)
    @DisplayName("Given a service manager, and loadService is called, should return the service provider implementation that " +
        "does not include any injected services")
    void testSimpleService() {
        ServiceManager.newInstance();
        ServiceManager.load();
        ITestServiceA serviceA = ServiceManager.loadService(ITestServiceA.class);
        assertNotNull(serviceA);
        assertEquals("I am TestServiceNamedB for ITestServiceA", serviceA.getName());
    }

    @Test
    @Order(7)
    @DisplayName("Given a service manager, a loadService is called, should return a service provider that includes " +
        "injected services")
    void testInjectedService() {
        RegistryBootstrap.load();
        var serviceRegistry = ServiceRegistries.getInstance();
        IInjectedServiceA injectedService = serviceRegistry.loadServiceProvider(IInjectedServiceA.class);
        assertNotNull(injectedService);
        assertTrue(injectedService.getInjected());
        System.out.println(injectedService.saySomething());
    }

    @Test
    @Order(8)
    @DisplayName("Given a service manager and a named provider, should return the expected provider")
    void testLoadNamedProvider() {
        ServiceManager.newInstance();
        ServiceManager.load();

        //The test uses a control which chooses the default provider (which is not "NamedTestServiceA",
        // it's actually "NamedTestServiceB" due to priority
        ITestServiceA testServiceControl = ServiceManager.loadService(ITestServiceA.class);
        String name = testServiceControl.getName();

        ITestServiceA testServiceA = ServiceManager.loadService(ITestServiceA.class, "NamedTestServiceA");
        assertNotNull(testServiceA);
        assertEquals("I am TestServiceNamedA, for ITestServiceA", testServiceA.getName());

        //should be different values between default and named providers
        assertNotEquals(name, testServiceA.getName());

    }
}
