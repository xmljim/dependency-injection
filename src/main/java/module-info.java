import io.github.xmljim.service.di.registry.ServiceRegistry;
import io.github.xmljim.service.di.registry.ServiceRegistryImpl;

/**
 * Core module for service instantiation with dependency injection
 */
module xmljim.dependency.injection {
    requires ch.qos.logback.classic;
    requires ch.qos.logback.core;
    requires org.slf4j;

    exports io.github.xmljim.service.di;
    exports io.github.xmljim.service.di.scanner;
    exports io.github.xmljim.service.di.provider;
    exports io.github.xmljim.service.di.registry;
    exports io.github.xmljim.service.di.service;
    exports io.github.xmljim.service.di.annotations;
    exports io.github.xmljim.service.di.util;

    provides ServiceRegistry with ServiceRegistryImpl;
}