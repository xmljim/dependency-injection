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

import io.github.xmljim.service.di.registry.ServiceRegistry;
import io.github.xmljim.service.di.util.ClassFilter;

import java.util.function.Predicate;

/**
 * Scanner to locate all services
 */
@SuppressWarnings({"unused", "SameReturnValue"})
public interface Scanner {

    String getName();

    /**
     * Scan and register services
     * @param serviceRegistry The service registry to append services
     * @return {@code true} if the scanner completes successfully
     */
    boolean scan(ServiceRegistry serviceRegistry);

    /**
     * The service class filter to apply for the services to register
     * @return a {@link Predicate} holding the criteria to evaluate for each service class
     */
    ClassFilter getServiceClassFilter();

    /**
     * The provider class filter to apply for the providers to append to a service
     * @return a {@link Predicate} holding the criteria to evaluate for each provider class
     */
    ClassFilter getProviderClassFilter();

    /**
     * Enforce assignability
     * @return enforce assignability
     */
    boolean enforceProviderAssignableFromService();
}
