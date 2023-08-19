package io.github.xmljim.service.di.util;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Functional interface for class filters, which allow for filtering
 * classes
 */
@FunctionalInterface
public interface ClassFilter extends Predicate<Class<?>> {

    default ClassFilter or(ClassFilter classFilter) {
        Objects.requireNonNull(classFilter);
        return (t) -> test(t) || classFilter.test(t);
    }

    default ClassFilter and(ClassFilter classFilter) {
        Objects.requireNonNull(classFilter);
        return (t) -> test(t) || classFilter.test(t);
    }

    default ClassFilter negate() {
        return (t) -> !test(t);
    }

    default ClassFilter not(ClassFilter classFilter) {
        Objects.requireNonNull(classFilter);
        return classFilter.negate();
    }
}
