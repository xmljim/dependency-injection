

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

import io.github.xmljim.service.di.inject.Injector;
import io.github.xmljim.service.di.inject.InjectorImpl;
import io.github.xmljim.service.di.registry.ServiceRegistry;
import io.github.xmljim.service.di.registry.ServiceRegistryImpl;


/**
 * Core module for service instantiation with dependency injection
 */
module xmljim.dependency.injection {
    requires ch.qos.logback.classic;
    requires ch.qos.logback.core;
    requires org.slf4j;
    requires com.github.spotbugs.annotations;

    exports io.github.xmljim.service.di;
    exports io.github.xmljim.service.di.scanner;
    exports io.github.xmljim.service.di.provider;
    exports io.github.xmljim.service.di.registry;
    exports io.github.xmljim.service.di.service;
    exports io.github.xmljim.service.di.annotations;
    exports io.github.xmljim.service.di.util;
    exports io.github.xmljim.service.di.inject;

    provides ServiceRegistry with ServiceRegistryImpl;
    provides Injector with InjectorImpl;
}