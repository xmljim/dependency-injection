package io.github.xmljim.service.di.test;

import io.github.xmljim.service.di.ServiceManagerException;
import io.github.xmljim.service.di.registry.ServiceRegistries;
import io.github.xmljim.service.di.registry.ServiceRegistry;
import io.github.xmljim.service.di.scanner.Scanners;
import io.github.xmljim.service.di.testclasses.*;
import io.github.xmljim.service.di.util.ClassFilters;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServiceRegistryTest {

    @Test
    @DisplayName("Given a new service registry instance, should return for isLoaded")
    void testIsLoaded() {

        var serviceRegistry = ServiceRegistries.newServiceRegistry();
        assertFalse(serviceRegistry.isLoaded());
    }

    @Test
    @DisplayName("Given a new service registry and load() being called, should return true for isLoaded")
    void testLoad() {
        var serviceRegistry = ServiceRegistries.newServiceRegistry();
        serviceRegistry.load();

        assertTrue(serviceRegistry.isLoaded(Scanners.CLASSPATH));
        assertTrue(serviceRegistry.isLoaded(Scanners.MODULE));
        assertTrue(serviceRegistry.isLoaded());
    }

    @Test
    @DisplayName("Given a service registry, can filter loaded services, and only return the filtered services")
    void testLoadWithFilters() {

        var serviceRegistry = ServiceRegistries.newServiceRegistry();
        serviceRegistry.load(ClassFilters.implementsInterface(MyTestServices.class), ClassFilters.hasServiceProviderAnnotation());
        assertEquals(2, serviceRegistry.services().count());

        assertEquals(2, serviceRegistry.findService(ITestServiceA.class)
            .map(service -> service.getProviders().count()).orElse(0L));
    }

    @Test
    @DisplayName("Given a loaded service registry, can reload with all services")
    void testReload() {

        var serviceRegistry = ServiceRegistries.newServiceRegistry();
        serviceRegistry.load(ClassFilters.implementsInterface(MyTestServices.class), ClassFilters.hasServiceProviderAnnotation());
        assertEquals(2, serviceRegistry.services().count());

        serviceRegistry.reload();
        assertTrue(serviceRegistry.services().count() > 2);
    }

    @Test
    void testReloadWithFilters() {
        var serviceRegistry = ServiceRegistries.newServiceRegistry();
        serviceRegistry.load();
        var serviceCount = serviceRegistry.services().count();

        serviceRegistry.reload(ClassFilters.implementsInterface(MyTestServices.class), ClassFilters.hasServiceProviderAnnotation());
        assertEquals(2, serviceRegistry.services().count());

        assertTrue(serviceCount > serviceRegistry.services().count());
    }

    @Test
    void testGetScanners() {
        var serviceRegistry = ServiceRegistries.newServiceRegistry();
        assertTrue(serviceRegistry.getScanners().stream().anyMatch(name -> name.equals(Scanners.MODULE)));
        assertTrue(serviceRegistry.getScanners().stream().anyMatch(name -> name.equals(Scanners.CLASSPATH)));
    }

    @Test
    void testAppendScanner() {
        var serviceRegistry = ServiceRegistries.newServiceRegistry();
        serviceRegistry.appendScanner("TEST", TestScanner.class);
        assertTrue(serviceRegistry.getScanners().stream().anyMatch(name -> name.equals(Scanners.MODULE)));
        assertTrue(serviceRegistry.getScanners().stream().anyMatch(name -> name.equals(Scanners.CLASSPATH)));
        assertTrue(serviceRegistry.getScanners().stream().anyMatch(name -> name.equals("TEST")));
    }

    @Test
    void testLoadFromScannerInstance() {
        var serviceRegistry = ServiceRegistries.newServiceRegistry();
        var scanner = new TestScanner(ClassFilters.DEFAULT, ClassFilters.DEFAULT, true);
        //serviceRegistry.appendScanner(scanner);

        var result = serviceRegistry.load(scanner);
        assertTrue(result);
    }

    @Test
    void testLoadFromScannerName() {
        var serviceRegistry = ServiceRegistries.newServiceRegistry();
        var scanner = new TestScanner(ClassFilters.DEFAULT, ClassFilters.DEFAULT, true);
        serviceRegistry.appendScanner(scanner);

        var result = serviceRegistry.load(scanner.getName(), ClassFilters.DEFAULT, ClassFilters.DEFAULT, true);
        assertTrue(result);
    }

    @Test
    void testLoadScannerNotFound() {
        var serviceRegistry = ServiceRegistries.newServiceRegistry();
        assertThrows(ServiceManagerException.class, () -> serviceRegistry.load("NOTFOUND",
            ClassFilters.DEFAULT, ClassFilters.DEFAULT, true));
    }

    @Test
    void testFindServices() {
        var serviceRegistry = ServiceRegistries.newServiceRegistry();
        serviceRegistry.load();
        var services = serviceRegistry.findServices(ClassFilters.implementsInterface(MyTestServices.class)
            .or(ClassFilters.implementsInterface(ServiceRegistry.class)));

        assertFalse(services.isEmpty());
        assertTrue(services.stream().anyMatch(service -> service.getServiceClass().equals(ITestServiceA.class)));
        assertTrue(services.stream().anyMatch(service -> service.getServiceClass().equals(IInjectedServiceA.class)));
        assertTrue(services.stream().anyMatch(service -> service.getServiceClass().equals(ServiceRegistry.class)));
        assertTrue(services.stream().noneMatch(service -> service.getServiceClass().equals(ITeapotService.class)));
    }

    @Test
    void testLoadAllServiceProviders() {
        var serviceRegistry = ServiceRegistries.newServiceRegistry();
        serviceRegistry.load();

        var providers = serviceRegistry.loadAllServiceProviders(ITestServiceA.class);
        //TestServiceA, TestServiceNamedA, TestServiceNamedB
        assertTrue(providers.stream().anyMatch(provider -> provider.getClass().equals(TestServiceA.class)));
        assertTrue(providers.stream().anyMatch(provider -> provider.getClass().equals(TestServiceNamedA.class)));
        assertTrue(providers.stream().anyMatch(provider -> provider.getClass().equals(TestServiceNamedB.class)));
    }

    @Test
    public void testLoadClassInjectedNoArgs() {
        var serviceRegistry = ServiceRegistries.newServiceRegistry();
        serviceRegistry.load();

        MyExampleClassNoArgs exampleClass = serviceRegistry.loadClass(MyExampleClassNoArgs.class);
        assertNotNull(exampleClass);
        assertNotNull(exampleClass.getTeapotService());
        assertEquals("I'm a little teapot", exampleClass.getTeapotService().teapot());
    }

    @Test
    public void testLoadClassInjectedWithArgs() {
        var serviceRegistry = ServiceRegistries.newServiceRegistry();
        serviceRegistry.load();
        String name = "Test";
        int repeat = 2;

        MyExampleClassArgs exampleClassArgs = serviceRegistry.loadClass(MyExampleClassArgs.class, name, repeat);
        assertNotNull(exampleClassArgs);
        assertNotNull(exampleClassArgs.getTeapotService());
        assertEquals("I'm a little teapot", exampleClassArgs.getTeapotService().teapot());

        var builder = new StringBuilder();
        for (int i = 0; i < repeat; i++) {
            builder.append(name).append(System.lineSeparator());
        }

        assertEquals(builder.toString(), exampleClassArgs.echoName());
    }

    @Test
    public void testLoadClassInjectedWithArgsExceptionMissingArg() {
        var serviceRegistry = ServiceRegistries.newServiceRegistry();
        serviceRegistry.load();
        String name = "Test";

        var exception = assertThrows(ServiceManagerException.class,
            () -> serviceRegistry.loadClass(MyExampleClassArgs.class, name));

        assertTrue(exception.getMessage().startsWith("No argument value provided for arg"));
    }

    @Test
    public void testLoadClassInjectedWithArgsExceptionWrongType() {
        var serviceRegistry = ServiceRegistries.newServiceRegistry();
        serviceRegistry.load();
        String name = "Test";
        int repeat = 5;

        var exception = assertThrows(ServiceManagerException.class,
            () -> serviceRegistry.loadClass(MyExampleClassArgs.class, repeat, name));

        assertEquals("argument type mismatch", exception.getMessage());
    }

}