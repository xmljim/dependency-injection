/*
 * Copyright 2023 Jim Earley (xml.jim@gmail.com)
 *
 * Licensed under the Apache NON-AI License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://raw.githubusercontent.com/non-ai-licenses/non-ai-licenses/main/NON-AI-APACHE2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.xmljim.service.di.scanner;

import io.github.xmljim.service.di.ServiceManagerException;
import io.github.xmljim.service.di.util.ClassFilter;
import io.github.xmljim.service.di.util.ClassFilters;

import java.lang.reflect.Constructor;
import java.util.Optional;

/**
 * Abstract Scanner implementation designed for extension
 */
public abstract class Scanners implements Scanner {
    private final ClassFilter serviceClassFilter;
    private final ClassFilter providerClassFilter;
    private boolean enforceProviderAssignableFromService;

    /**
     * The module scanner name
     */
    public static final String MODULE = "ModuleScanner";

    /**
     * The classpath scanner name
     */
    public static final String CLASSPATH = "ClasspathScanner";

    /**
     * Constructor
     * @param serviceClassFilter  The service class filter
     * @param providerClassFilter the provider class filter
     */
    public Scanners(ClassFilter serviceClassFilter, ClassFilter providerClassFilter) {
        this.serviceClassFilter = serviceClassFilter;
        this.providerClassFilter = providerClassFilter;
    }

    /**
     * Constructor
     * @param serviceClassFilter                   the service class filter
     * @param providerClassFilter                  the provider class filter
     * @param enforceProviderAssignableFromService apply assignability enforcement between service and provider
     */
    public Scanners(ClassFilter serviceClassFilter, ClassFilter providerClassFilter, boolean enforceProviderAssignableFromService) {
        this(serviceClassFilter, providerClassFilter);
        this.enforceProviderAssignableFromService = enforceProviderAssignableFromService;
    }

    /**
     * Create a default module scanner
     * @param serviceClassFilter                   the service class filter
     * @param providerClassFilter                  the provider class filter
     * @param enforceProviderAssignableFromService apply assignability enforcement between service and provider
     * @return a new module scanner
     */
    public static Scanner newModuleScanner(ClassFilter serviceClassFilter, ClassFilter providerClassFilter,
        boolean enforceProviderAssignableFromService) {

        return newScanner(ModuleScanner.class, serviceClassFilter, providerClassFilter, enforceProviderAssignableFromService);
    }

    /**
     * Create a default classpath scanner
     * @param serviceClassFilter                   the service class filter
     * @param providerClassFilter                  the provider class filter
     * @param enforceProviderAssignableFromService apply assignability enforcement between service and provider
     * @return a new classpath scanner
     */
    public static Scanner newClasspathScanner(ClassFilter serviceClassFilter, ClassFilter providerClassFilter,
        boolean enforceProviderAssignableFromService) {

        return newScanner(ClasspathScanner.class, serviceClassFilter, providerClassFilter, enforceProviderAssignableFromService);
    }

    /**
     * Create a new scanner
     * @param scannerClass                         The scanner class to create. Must extend/implement the
     *                                             {@link Scanner} interface
     * @param serviceClassFilter                   the service class filter
     * @param providerClassFilter                  the provider class filter
     * @param enforceProviderAssignableFromService apply assignability enforcement between service and provider
     * @param <S>                                  The scanner type
     * @return a new scanner
     */
    public static <S extends Scanner> S newScanner(Class<S> scannerClass, ClassFilter serviceClassFilter,
        ClassFilter providerClassFilter, boolean enforceProviderAssignableFromService) {
        try {
            Constructor<S> ctor = scannerClass.getDeclaredConstructor(ClassFilter.class, ClassFilter.class, boolean.class);
            return ctor.newInstance(serviceClassFilter, providerClassFilter, enforceProviderAssignableFromService);
        } catch (Exception e) {
            throw new ServiceManagerException(e.getMessage(), e);
        }
    }

    /**
     * Create a new scanner
     * @param scannerClass the scanner class to create
     * @param <S>          the scanner type
     * @return a new scanner
     */
    public static <S extends Scanner> S newScanner(Class<S> scannerClass) {
        return newScanner(scannerClass, ClassFilters.DEFAULT, ClassFilters.DEFAULT, false);
    }

    /**
     * Utility method for returning the module scanner class
     * @param <S> The scanner class type
     * @return The module scanner class
     */
    @SuppressWarnings("unchecked")
    public static <S extends Scanner> Class<S> getModuleScannerClass() {
        return (Class<S>) ModuleScanner.class;
    }

    /**
     * Utility method for returning the classpath scanner class
     * @param <S> the scanner class type
     * @return the classpath scanner class
     */
    @SuppressWarnings("unchecked")
    public static <S extends Scanner> Class<S> getClasspathScannerClass() {
        return (Class<S>) ClasspathScanner.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClassFilter getServiceClassFilter() {
        return serviceClassFilter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClassFilter getProviderClassFilter() {
        return providerClassFilter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean enforceProviderAssignableFromService() {
        return enforceProviderAssignableFromService;
    }

    /**
     * Load a class from a class name
     * @param className the class name
     * @return the class
     */
    protected synchronized Optional<Class<?>> loadClass(String className) {
        try {
            return Optional.of(Class.forName(className));
        } catch (NoClassDefFoundError | Exception e) {
            return Optional.empty();
            //since we're loading services, there may be cases, particularly with
            //META-INF/services declaration where the service may not be on the classpath
            //As a result, we'll return an Optional.empty() indicating no class was found
            //and let the upstream method determine how to handle this
            //throw new ServiceManagerException("Class not found: " + className + ": " + e.getMessage(), e);
        }
    }
}
