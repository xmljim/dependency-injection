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

package io.github.xmljim.service.di.inject;

/**
 * Service class for dependency injection
 */
public interface Injector {

    /**
     * Create a new class instance.
     * <p>
     * Will interrogate the class' constructors, giving priority to
     * a constructor decorated with the {@link io.github.xmljim.service.di.annotations.DependencyInjection}
     * annotation, or to a constructor with all parameters that have a service registered, or
     * a zero-argument constructor.
     * </p>
     * <p>
     * If the constructor contains parameters (i.e., injectable services), instances of these
     * services are created and used as the parameter values when invoking the constructor.
     * </p>
     * <p>
     * Once the instance is created, it will interrogate the class' any annotated field with the
     * {@link io.github.xmljim.service.di.annotations.Inject} annotation and retrieves the
     * service instance values for each.
     * </p>
     * @param instanceClass the instance class to create
     * @param <T>           The instance class type
     * @return A new class instance
     */
    <T> T createInstance(Class<T> instanceClass);


    /**
     * Create a new class instance containing a mixture of injectable services and "static" parameter values.
     * <p>
     * <strong>IMPORTANT:</strong> The constructor <em>must have</em> a
     * {@link io.github.xmljim.service.di.annotations.DependencyInjection}
     * annotation.
     * </p>
     * <p>
     * The constructor's parameters must follow the following rules
     * </p>
     * <ol>
     *     <li>All injectable parameters must appear in sequence from the beginning of the parameter list</li>
     *     <li>All other "static" arguments must follow injectable parameters</li>
     * </ol>
     * <p>
     *     Parameter values are supplied by first interrogating the parameter's type. If the parameter can be
     *     supplied from a registered service, an instance of that service is created. If no service is found
     *     each argument is taken from the supplied arguments in order.
     * </p>
     * @param instanceClass The instance class to create
     * @param args          Any number of "static" parameter values
     * @param <T>           The instance type
     * @return a new class instance
     */
    <T> T createInstanceWithArgs(Class<T> instanceClass, Object... args);
}
