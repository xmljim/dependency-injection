package io.github.xmljim.service.di.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for Service Provider classes that require other services to be injected into the class's
 * constructor.
 * <p>Java Services require a no-arg constructor to be present on the provider class. This
 * provides a mechanism to circumvent the requirement by allowing a provider class to meet the basic requirement
 * of including a no-arg constructor <em>and</em> another constructor providing arguments that reference
 * other services to be injected.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.CONSTRUCTOR)
public @interface DependencyInjection {
}
