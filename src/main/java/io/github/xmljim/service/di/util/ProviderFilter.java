package io.github.xmljim.service.di.util;

import io.github.xmljim.service.di.provider.Provider;

import java.util.function.Predicate;

/**
 * Predicate Provider filter
 */
@FunctionalInterface
public interface ProviderFilter extends Predicate<Provider> {
}
