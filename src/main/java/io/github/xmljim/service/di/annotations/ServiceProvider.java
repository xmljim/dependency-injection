package io.github.xmljim.service.di.annotations;

import io.github.xmljim.service.di.util.ServiceLifetime;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Utility annotation for service providers. Underlying requests for a provider will give preference
 * to any class with this annotation. However, it's not required.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.PARAMETER})
public @interface ServiceProvider {
    /**
     * The provider name
     * @return The provider name
     */
    String name();

    /**
     * Specifies the service provider's lifetime. If set to {@link ServiceLifetime#SINGLETON}, the first instance
     * of this provider will be stored for future requests
     * @return the service lifetime
     */
    ServiceLifetime lifetime();

    /**
     * Specify a tie-breaker priority for selecting a provider instance. The highest number wins
     * @return the priority
     */
    int priority() default 1;
}
