
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

/**
 * <p>
 * Root package for service instantiation with dependency injection.
 * Java's built-in Service architecture provides an easy, lightweight and flexible mechanism for creating
 * services. Starting with Java 6, services could be defined using {@code META-INF/services}; Starting in Java 9,
 * services could be declared in the {@code module-info.class} file ({@code Provides {service} with {provider}}).
 * Java's {@link java.util.ServiceLoader} class provides a relatively straightforward way to access these declared
 * services.
 * </p>
 * <p>
 * However, there are some limitations. First, ServiceLoader only supports service provider classes that
 * contain a zero-argument constructor (or include a static {@code provider} method.
 * Likewise, service declarations (via both the {@code META-INF/service} and {@code module-info.class}) will complain
 * if any service provider class does not contain a default or zero-argument constructor.
 * </p>
 * <p>
 * Spring has a robust dependency mechanism, meaning that classes can have other components and services injected
 * into them at runtime.  However not all applications use or require Spring, which has a pretty large footprint.
 * Hence the need for a library that can support service loading <em>with</em> dependency injection.
 * </p>
 * <p>
 * The principle differences with the built-in {@link java.util.ServiceLoader}
 * are:
 * </p>
 *
 * <ul>
 *     <li>
 *         {@link java.util.ServiceLoader} lazy-loads services at runtime every time a service is requested with some caching. In other
 *         words, each request for a service scans the classpath or module path for the service declaration to initialize
 *         the service class. ServiceManger scans and caches references to all services and providers before first use.
 *         Then it will retrieve and instantiate the service class from it's registry, meaning that first request may be
 *         slower, but subsequent requests will be faster since it doesn't require a class scan
 *     </li>
 *     <li>
 *         {@link java.util.ServiceLoader} only supports classes with a zero-argument constructor (or default constructor).
 *         This limits a service providers ability to include other services, other than to initialize these in code.
 *         Since all services have been prefetched, it's possible to create rich services that have other services
 *         injected at creation time (dependency injection).
 *     </li>
 *     <li>
 *         You have full control over the services that you store in the registry. You can either load every declared
 *         services, or filter and cache only the services you need for your application
 *     </li>
 *     <li>
 *         For services that have more than one provider, you can provide a name to distinguish between provides
 *         and specify the provider to use
 *     </li>
 *     <li>
 *         Every provider can have a defined service lifetime. By default, all services are <em>transient</em>,
 *         meaning that a new instance is created for each request for that service.  However using the
 *         {@link io.github.xmljim.service.di.annotations.ServiceProvider} annotation, you can designate a provider as a <em>singleton</em>, meaning that it
 *         will only be created once, and used across all requests for that service. It's up to you to handle
 *         thread safety/concurrency for singleton instances.
 *     </li>
 *     <li>
 *         The ServiceLoader enforces assignability between the service and provider classes, <em>unless</em> the
 *         provider class contains a static, no-arg {@code provider()} method (and a default or no-arg constructor).
 *         With ServiceManager, you have full control over assignability in either the {@link io.github.xmljim.service.di.registry.ServiceRegistry}
 *         or the {@link io.github.xmljim.service.di.service.Service} layer.  This provides another degree of freedom
 *         to design applications that take advantage of service providers that are of a different type.
 *     </li>
 * </ul>
 * <p>
 *     No services are registered until the {@link io.github.xmljim.service.di.ServiceManager#load()} or
 *     {@link io.github.xmljim.service.di.ServiceManager#load(ClassFilter, ClassFilter)}
 *     method is called. Once the registry is populated, subsequent calls to {@link io.github.xmljim.service.di.ServiceManager#loadService(Class)}
 *     or {@link io.github.xmljim.service.di.ServiceManager#loadService(Class, String)} will create service instances
 * </p>
 */

package io.github.xmljim.service.di;

import io.github.xmljim.service.di.util.ClassFilter;