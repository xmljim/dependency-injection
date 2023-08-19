package io.github.xmljim.service.di.util;

import io.github.xmljim.service.di.annotations.ServiceProvider;
import io.github.xmljim.service.di.scanner.Scanner;

import java.lang.annotation.Annotation;

/**
 * Utility class containing common class filters to be applied by a {@link Scanner};
 */
public final class ClassFilters {

    private ClassFilters() {
        //not intended for instantiation
    }

    /**
     * Default filter. Returns all classes
     */
    public static final ClassFilter DEFAULT = c -> true;

    /**
     * Return only classes that implement an interface class
     * @param clazz The interface class each scanned class must implement
     * @return The predicate to use in the filter
     */
    public static ClassFilter implementsInterface(Class<?> clazz) {
        return clazz::isAssignableFrom;
        //Arrays.stream(c.getInterfaces()).anyMatch(iface -> iface.isAssignableFrom(clazz));
    }

    /**
     * Filter provider classes that have been decorated with the {@link ServiceProvider} annotation
     * @return the predicate to apply
     */
    public static ClassFilter hasServiceProviderAnnotation() {
        return hasAnnotation(ServiceProvider.class);
    }


    /**
     * Filter any class that has been decorated with a specific annotation
     * @param annotationClass The annotation class
     * @return the predicate to apply
     */
    public static ClassFilter hasAnnotation(Class<? extends Annotation> annotationClass) {
        return c -> c.isAnnotationPresent(annotationClass);
    }
}
