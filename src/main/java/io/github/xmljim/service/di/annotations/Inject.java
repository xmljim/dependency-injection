package io.github.xmljim.service.di.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for any fields that should be injected with a service at initialization
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Inject {

    /**
     * Specify the specific provider instance value to use
     * @return the provider name. An empty value indicates that the service
     */
    String providerName() default "";
}
