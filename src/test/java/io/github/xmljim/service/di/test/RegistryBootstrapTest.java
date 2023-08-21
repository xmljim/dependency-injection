package io.github.xmljim.service.di.test;

import io.github.xmljim.service.di.RegistryBootstrap;
import io.github.xmljim.service.di.ServiceManagerException;
import io.github.xmljim.service.di.registry.ServiceRegistries;
import io.github.xmljim.service.di.registry.ServiceRegistry;
import io.github.xmljim.service.di.testclasses.*;
import io.github.xmljim.service.di.util.ClassFilters;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistryBootstrapTest {


    @Test
    void load() {
        ServiceRegistries.clear();
        RegistryBootstrap.load();
        assertTrue(ServiceRegistries.getInstance().isLoaded());
        ServiceRegistries.clear();
    }

    @Test
    void testLoad() {
        ServiceRegistries.clear();
        var serviceFilter = ClassFilters.implementsInterface(MyTestServices.class)
            .or(ClassFilters.implementsInterface(ServiceRegistry.class));

        var providerFilter = ClassFilters.hasServiceProviderAnnotation();
        var options = RegistryBootstrap.Options.configure()
            .enforceAssignability(true)
            .loadRegistry(false)
            .appendScanner("TEST", TestScanner.class)
            .providerImplementation(TestProviderImpl.class)
            .providerClassFilter(providerFilter)
            .serviceImplementation(TestServiceImpl.class)
            .serviceClassFilter(serviceFilter)
            .serviceRegistryImplementation(TestServiceRegistryImpl.class)

            .build();

        RegistryBootstrap.load(() -> options);
        assertFalse(ServiceRegistries.getInstance().isLoaded());
        assertEquals(TestServiceRegistryImpl.class, ServiceRegistries.getInstance().getClass());
        ServiceRegistries.clear();
    }

    @Test
    void testCreateDefaultOptions() {
        var defaultOptions = RegistryBootstrap.Options.useDefaults();

        assertTrue(defaultOptions.getScanners().isEmpty());
        assertFalse(defaultOptions.getEnforceAssignability());
        assertTrue(defaultOptions.getLoadRegistry());
        assertTrue(defaultOptions.getProviderClassFilter().isEmpty());
        assertTrue(defaultOptions.getServiceClassFilter().isEmpty());
        assertTrue(defaultOptions.getProviderImplementation().isEmpty());
        assertTrue(defaultOptions.getServiceImplementation().isEmpty());
        assertTrue(defaultOptions.getServiceRegistryImplementation().isEmpty());
        assertTrue(defaultOptions.getServiceDefinitions().isEmpty());
    }

    @Test
    void testConfigureOptions() {
        var serviceFilter = ClassFilters.implementsInterface(MyTestServices.class)
            .or(ClassFilters.implementsInterface(ServiceRegistry.class));

        var providerFilter = ClassFilters.hasServiceProviderAnnotation();


        var options = RegistryBootstrap.Options.configure()
            .enforceAssignability(true)
            .loadRegistry(false)
            .appendScanner("TEST", TestScanner.class)
            .providerImplementation(TestProviderImpl.class)
            .providerClassFilter(providerFilter)
            .serviceImplementation(TestServiceImpl.class)
            .serviceClassFilter(serviceFilter)
            .serviceRegistryImplementation(TestServiceRegistryImpl.class)
            .build();


        assertTrue(options.getScanners().containsKey("TEST"));
        assertTrue(options.getEnforceAssignability());
        assertFalse(options.getLoadRegistry());
        assertTrue(options.getProviderClassFilter().map(provider -> provider.equals(providerFilter))
            .orElseThrow());
        assertTrue(options.getServiceClassFilter().map(service -> service.equals(serviceFilter))
            .orElseThrow());
        assertTrue(options.getProviderImplementation().map(providerImpl -> providerImpl.equals(TestProviderImpl.class))
            .orElseThrow());
        assertTrue(options.getServiceImplementation().map(serviceImpl -> serviceImpl.equals(TestServiceImpl.class))
            .orElseThrow());
        assertTrue(options.getServiceRegistryImplementation().map(registryImpl -> registryImpl.equals(TestServiceRegistryImpl.class))
            .orElseThrow());

        assertTrue(options.getServiceDefinitions().isEmpty());
    }

    @Test
    void testAppendServiceNoAssignabilityEnforcement() {
        var options = RegistryBootstrap.Options.configure()
            .appendService(IDummyInterface.class, TeapotService.class)
            .loadRegistry(false)
            .build();

        RegistryBootstrap.load(() -> options);

        var serviceRegistry = ServiceRegistries.getInstance();
        assertTrue(serviceRegistry.hasService(IDummyInterface.class));

        var provider = serviceRegistry.loadServiceProvider(IDummyInterface.class);

        assertEquals(provider.getClass(), TeapotService.class);
        ServiceRegistries.clear();
    }

    @Test
    void testAppendServiceWithAssignabilityEnforcement() {
        var options = RegistryBootstrap.Options.configure()
            .appendService(IDummyInterface.class, DummyClass.class)
            .loadRegistry(true)
            .build();

        RegistryBootstrap.load(() -> options);

        var serviceRegistry = ServiceRegistries.getInstance();
        assertTrue(serviceRegistry.hasService(IDummyInterface.class));

        var provider = serviceRegistry.loadServiceProvider(IDummyInterface.class);

        assertEquals(provider.getClass(), DummyClass.class);
        ServiceRegistries.clear();

    }

    @Test
    void testAppendServiceWithAssignabilityEnforcementException() {
        var options = RegistryBootstrap.Options.configure()
            .enforceAssignability(true)
            .appendService(IDummyInterface.class, TeapotService.class)
            .loadRegistry(false);


        var exception = assertThrows(ServiceManagerException.class, options::build);
        System.out.println(exception.getMessage());
        ServiceRegistries.clear();
    }
}