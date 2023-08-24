import io.github.xmljim.service.di.inject.Injector;
import io.github.xmljim.service.di.inject.InjectorImpl;
import io.github.xmljim.service.di.registry.ServiceRegistry;
import io.github.xmljim.service.di.registry.ServiceRegistryImpl;

/**
 * Core module for service instantiation with dependency injection
 */
module xmljim.dependency.injection {
    requires ch.qos.logback.classic;
    requires ch.qos.logback.core;
    requires org.slf4j;
    requires com.github.spotbugs.annotations;

    exports io.github.xmljim.service.di;
    exports io.github.xmljim.service.di.scanner;
    exports io.github.xmljim.service.di.provider;
    exports io.github.xmljim.service.di.registry;
    exports io.github.xmljim.service.di.service;
    exports io.github.xmljim.service.di.annotations;
    exports io.github.xmljim.service.di.util;
    exports io.github.xmljim.service.di.inject;

    provides ServiceRegistry with ServiceRegistryImpl;
    provides Injector with InjectorImpl;
}