package io.github.xmljim.service.di.annotations;

import java.lang.annotation.*;

/**
 * Annotation for suppressing methods
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface Generated {
}
