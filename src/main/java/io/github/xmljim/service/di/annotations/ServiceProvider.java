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
