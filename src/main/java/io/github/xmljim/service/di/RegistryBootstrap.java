package io.github.xmljim.service.di;

import io.github.xmljim.service.di.provider.Provider;
import io.github.xmljim.service.di.provider.Providers;
import io.github.xmljim.service.di.registry.ServiceRegistries;
import io.github.xmljim.service.di.registry.ServiceRegistry;
import io.github.xmljim.service.di.scanner.Scanner;
import io.github.xmljim.service.di.service.Service;
import io.github.xmljim.service.di.service.Services;
import io.github.xmljim.service.di.util.ClassFilter;
import io.github.xmljim.service.di.util.ClassFilters;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Bootstraps a Service Registry using the configured {@link Options}
 */
public class RegistryBootstrap {
    /**
     * Bootstrap the ServiceRegistry with the provided {@link Options}
     * @param optionsSupplier The Options to supply to the boostrap
     */
    public static void load(Supplier<Options> optionsSupplier) {
        ServiceRegistries.clear();
        var options = optionsSupplier.get();

        options.getServiceRegistryImplementation().ifPresent(ServiceRegistries::setUseServiceRegistry);
        options.getServiceImplementation().ifPresent(Services::setUseServiceClass);
        options.getProviderImplementation().ifPresent(Providers::setUseProviderClass);

        var serviceRegistry = ServiceRegistries.newServiceRegistry(options.getEnforceAssignability());
        options.getScanners().forEach(serviceRegistry::appendScanner);

        if (options.getLoadRegistry()) {
            serviceRegistry.load(options.getServiceClassFilter().orElse(ClassFilters.DEFAULT),
                options.getProviderClassFilter().orElse(ClassFilters.DEFAULT));
        }

    }

    /**
     * Boostrap the ServiceRegistry using the default {@link Options}
     */
    public static void load() {
        load(Options::useDefaults);
    }

    /**
     * Bootstrap options for initializing the service registry.
     * <p>
     * The options are:
     * </p>
     * <ul>
     *     <li>{@code serviceImplementation}:  If you wish to use your own {@link Service} implementation for holding
     *     service classes, you can specify the class here. This will be used to create all Service instances.
     *     Otherwise, it will use the default implementation</li>
     *     <li>{@code providerImplementation}: If you want to use your own {@link Provider} implementation for
     *     holding provider classes, you can specify the class here. This will be used to create all Provider instances.
     *     If not set, the default implementation will be used</li>
     *     <li>{@code serviceRegistryImplementation}: If you want to use your own {@link ServiceRegistry}
     *     implementation, specify that class here. This will be used to create service registry instances. Otherwise,
     *     the default ServiceRegistry implementation will be used</li>
     *     <li>{@code enforceAssignability}: Allows you to specify if all service provider classes must be assignable
     *     from the service class during load. By default, this is set to {@code false}</li>
     *     <li>{@code serviceClassFilter}: Specifies a filter to apply for loading services. If not set, it will use
     *     the default filter, {@link ClassFilters#DEFAULT} (load them all). This will only be applied if
     *     {@code loadRegistry} is set to {@code true}</li>
     *     <li>{@code providerClassFilter}: Specifies a filter to apply for loading service providers. If not set,
     *     it will use the default filter, {@link ClassFilters#DEFAULT} (load them all). This will only be applied if
     *     {@code loadRegistry} is set to {@code true}</li>
     *     <li><em>Custom Scanners</em>: This allow other libraries that want to leverage the Service Registry to
     *     add any additional {@link Scanner} implementations required for that library. </li>
     *     <li>{@code loadRegistry}: Load the registry with all defined scanners and filters after initialization. This
     *     is set the {@code true} by default.  However, if you wish only initialize the registry without load, set this
     *     value to {@code false}</li>
     * </ul>
     */
    public static class Options {
        private Class<? extends Service> serviceImplementation;
        private Class<? extends Provider> providerImplementation;
        private Class<? extends ServiceRegistry> serviceRegistryImplementation;
        private final Map<String, Class<? extends Scanner>> scannerMap = new HashMap<>();
        private ClassFilter serviceClassFilter;
        private ClassFilter providerClassFilter;
        private boolean enforceAssignability = false;
        private boolean loadRegistry = true;

        /**
         * Private constructor. Not intended for general use
         */
        private Options() {
            //no public instantiation.  Use useDefaults() or configure()
        }

        /**
         * Return the default options
         * @return
         */
        public static Options useDefaults() {
            return new Options();
        }

        /**
         * Configure the service registry options before launching
         * @return a new {@link Options.Builder}
         */
        public static Builder configure() {
            return new Builder();
        }

        /**
         * Using existing options, allow other libraries that have transitive dependencies on
         * the Service Registry to configure their options on top of yours.  This may override
         * some settings
         * @param options The existing options
         * @return a new {@link Options.Builder}
         */
        public static Builder merge(Options options) {
            return new Builder(options);
        }

        private <S extends Service> void setServiceImplementation(Class<S> serviceImplementation) {
            this.serviceImplementation = serviceImplementation;
        }

        private <P extends Provider> void setProviderImplementation(Class<P> providerImplementation) {
            this.providerImplementation = providerImplementation;
        }

        private <R extends ServiceRegistry> void setServiceRegistryImplementation(Class<R> registryImplementation) {
            this.serviceRegistryImplementation = registryImplementation;
        }

        private void setEnforceAssignability(boolean enforceAssignability) {
            this.enforceAssignability = enforceAssignability;
        }

        private void setServiceClassFilter(ClassFilter serviceClassFilter) {
            this.serviceClassFilter = serviceClassFilter;
        }

        private void setProviderClassFilter(ClassFilter providerClassFilter) {
            this.providerClassFilter = providerClassFilter;
        }

        private <S extends Scanner> void appendScanner(String scannerName, Class<S> scannerClass) {
            scannerMap.put(scannerName, scannerClass);
        }

        private void setLoadRegistry(boolean loadRegistry) {
            this.loadRegistry = loadRegistry;
        }

        /**
         * Return the {@link Provider} implementation, if set.  If not set, the default
         * Provider implementation will be used
         * @return An optional of the provider class. If set, this will be applied to
         *     all new {@link Provider} instances. Otherwise, the default implementation will
         *     be used.
         */
        public Optional<Class<? extends Provider>> getProviderImplementation() {
            return Optional.ofNullable(this.providerImplementation);
        }

        /**
         * Return the {@link Service} implementation, if set.  If not set, the default
         * Service implementation will be used
         * @return An optional of the service class. If set, this will be applied to
         *     all new {@link Service} instances. Otherwise, the default implementation will
         *     be used.
         */
        public Optional<Class<? extends Service>> getServiceImplementation() {
            return Optional.ofNullable(this.serviceImplementation);
        }

        /**
         * Return the {@link ServiceRegistry} implementation class, if set. If not set, the default
         * Service Registry implementation will be used.
         * @return An optional of the service registry class. If set, this will be applied to
         *     all new {@link ServiceRegistry} implementations. Otherwise, the default implementation will be used.
         */
        public Optional<Class<? extends ServiceRegistry>> getServiceRegistryImplementation() {
            return Optional.ofNullable(this.serviceRegistryImplementation);
        }

        /**
         * Return the Service {@link ClassFilter} to apply for filtering service classes.
         * @return A {@link ClassFilter}. This is set to {@link ClassFilters#DEFAULT} (all services) by default.
         */
        public Optional<ClassFilter> getServiceClassFilter() {
            return Optional.ofNullable(this.serviceClassFilter);
        }

        /**
         * Return the Provider {@link ClassFilter} to apply for filtering all service provider classes
         * @return A {@link ClassFilter}. This is set to {@link ClassFilters#DEFAULT} (all service providers) by default.
         */
        public Optional<ClassFilter> getProviderClassFilter() {
            return Optional.ofNullable(this.providerClassFilter);
        }

        /**
         * Return whether to enforce assignability between service and provider classes. This will be applied
         * to the {@link ServiceRegistry}, meaning that it will percolate to the {@link Provider} from each
         * {@link Scanner}. This is set to {@link false} by default.
         * @return The flag for enforcing assignability
         */
        public boolean getEnforceAssignability() {
            return this.enforceAssignability;
        }

        /**
         * Returns a map of  {@link Scanner} classes to append to the {@link ServiceRegistry}. These will be
         * appended after initialization of the Service Registry, but before any services are loaded.
         * @return A map contain a collection of scanner names and scanner classes to be appended to the
         *     {@link ServiceRegistry}
         */
        public Map<String, Class<? extends Scanner>> getScanners() {
            return this.scannerMap;
        }

        /**
         * Flag to tell the Bootstrap launcher to load the registry
         * @return If {@code true}, it will load the new {@link ServiceRegistry} instance with this configuration;
         *     otherwise, it will not load any services. The default is {@code true}
         */
        public boolean getLoadRegistry() {
            return this.loadRegistry;
        }

        /**
         * Options Builder implementation. Can only be accessed from {@link Options#configure()}
         */
        public static class Builder {
            private final Options options;

            private Builder() {
                options = new Options();
            }

            private Builder(Options options) {
                this.options = options;
            }

            /**
             * Specify the {@link Service} implementation class to use. Only set this value if you do not want to use
             * the default implementation.
             * @param serviceImplementation The implementation class to use. Must not be null.
             * @param <S>                   The class type extending {@link Service}
             * @return The builder
             */
            public <S extends Service> Builder serviceImplementation(Class<S> serviceImplementation) {
                options.setServiceImplementation(serviceImplementation);
                return this;
            }

            /**
             * Specify the {@link Provider} implementation class to use. Only set this value if you do not want to use
             * the default implementation
             * @param providerImplementation The implementation class to use. Must not be null
             * @param <P>                    The class type extending {@link Provider}
             * @return The builder
             */
            public <P extends Provider> Builder providerImplementation(Class<P> providerImplementation) {
                options.setProviderImplementation(providerImplementation);
                return this;
            }

            /**
             * Specify the {@link ServiceRegistry} implementation class to use. Only set this value if you do not want
             * to use the default implementation
             * @param registryImplementation The implementation class to use. Must not be null
             * @param <R>                    the class type extending the {@link ServiceRegistry}
             * @return the builder
             */
            public <R extends ServiceRegistry> Builder serviceRegistryImplementation(Class<R> registryImplementation) {
                options.setServiceRegistryImplementation(registryImplementation);
                return this;
            }

            /**
             * Specify whether the {@link ServiceRegistry} should enforce assignability between service and provider
             * classes.  By default, this value is set to {@code false}
             * @param enforceAssignability The flag for assignability. Will be applied to the new ServiceRegistry
             *                             instance
             * @return the builder
             */
            public Builder enforceAssignability(boolean enforceAssignability) {
                options.setEnforceAssignability(enforceAssignability);
                return this;
            }

            /**
             * Specify the {@link ClassFilter} to apply for locating all service classes. By default, this is
             * set to {@link ClassFilters#DEFAULT} (all classes). It does this by
             * @param serviceClassFilter The filter to apply for locating service classes
             * @return the builder.
             */
            public Builder serviceClassFilter(ClassFilter serviceClassFilter) {
                //the logic here attempts to override the default filter.  So if the current
                //filter is the default, replace it with the new filter
                if (options.getServiceClassFilter().isPresent()) {
                    if (serviceClassFilter.equals(ClassFilters.DEFAULT)) {
                        //setting broad scope
                        options.setServiceClassFilter(serviceClassFilter);
                    } else {
                        if (!options.getServiceClassFilter().get().equals(ClassFilters.DEFAULT)) {
                            options.setServiceClassFilter(options.getProviderClassFilter().orElseThrow(() -> new RuntimeException("bad"))
                                .or(serviceClassFilter));
                        }
                    }

                } else {
                    options.setServiceClassFilter(serviceClassFilter);
                }

                return this;
            }

            /**
             * Specify the {@link ClassFilter} to apply for locating all service provider classes. By default, this is
             * set to {@link ClassFilters#DEFAULT} (all classes).
             * @param providerClassFilter The filter to apply for locating service provider classes
             * @return the builder.
             */
            public Builder providerClassFilter(ClassFilter providerClassFilter) {
                //the logic here attempts to override the default filter.  So if the current
                //filter is the default, replace it with the new filter
                if (options.getProviderClassFilter().isPresent()) {
                    if (providerClassFilter.equals(ClassFilters.DEFAULT)) {
                        //setting broad scope
                        options.setProviderClassFilter(providerClassFilter);
                    } else {
                        if (!options.getProviderClassFilter().get().equals(ClassFilters.DEFAULT)) {
                            Predicate<Class<?>> original = options.getProviderClassFilter().get();
                            Predicate<Class<?>> composed = original.or(providerClassFilter);

                            options.setProviderClassFilter(options.getProviderClassFilter().orElseThrow(() -> new RuntimeException("bad"))
                                .or(providerClassFilter));
                        }
                    }

                } else {
                    options.setProviderClassFilter(providerClassFilter);
                }

                return this;
            }

            /**
             * Append a scanner class to use for all load and reload operations from the {@link ServiceRegistry}
             * @param name         The scanner name. Should match ({@link Scanner#getName()}
             * @param scannerClass The scanner class to use. Must not be null
             * @return the builder
             */
            public Builder appendScanner(String name, Class<? extends Scanner> scannerClass) {
                options.appendScanner(name, scannerClass);
                return this;
            }

            /**
             * Specifies whether to load the registry with the settings provided
             * @param loadRegistry the flag to indicate whether to load the repository after initialization
             * @return If {@code true}, the Bootstrap will load the registry, otherwise, it will wait until a
             *     request is made to a {@code load} method
             */
            public Builder loadRegistry(boolean loadRegistry) {
                options.setLoadRegistry(loadRegistry);
                return this;
            }

            /**
             * Build the options
             * @return a new Options instance
             */
            public Options build() {
                return options;
            }
        }

    }


}
