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

package io.github.xmljim.service.di;

import io.github.xmljim.service.di.annotations.Generated;

/**
 * Core exception class for service manager
 */
public class ServiceManagerException extends RuntimeException {
    /**
     * Constructor
     */
    @Generated
    public ServiceManagerException() {
    }

    /**
     * Constructor
     * @param message the message
     */
    @Generated
    public ServiceManagerException(String message) {
        super(message);
    }

    /**
     * Create a formatted exception message
     * @param message the message template
     * @param args    the parameterized values
     */
    @Generated
    public ServiceManagerException(String message, Object... args) {
        this(message.formatted(args));
    }

    /**
     * Constructor
     * @param message message
     * @param cause   cause
     */
    @Generated
    public ServiceManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor
     * @param cause cause
     */
    @Generated
    public ServiceManagerException(Throwable cause) {
        super(cause);
    }
}
