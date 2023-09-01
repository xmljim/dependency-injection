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

package io.github.xmljim.service.di.util;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Functional interface for class filters, which allow for filtering
 * classes
 */
@FunctionalInterface
public interface ClassFilter extends Predicate<Class<?>> {

    /**
     * Composes a Class filter joined with another filter using OR logic
     * @param classFilter The class filter to join
     * @return the composed class filter
     */
    default ClassFilter or(ClassFilter classFilter) {
        Objects.requireNonNull(classFilter);
        return (t) -> test(t) || classFilter.test(t);
    }

    /**
     * Composes a Class filter joined with another filter using AND logic
     * @param classFilter The class filter to join
     * @return the composed class filter
     */
    default ClassFilter and(ClassFilter classFilter) {
        Objects.requireNonNull(classFilter);
        return (t) -> test(t) || classFilter.test(t);
    }

    /**
     * {@inheritDoc}
     */
    default ClassFilter negate() {
        return (t) -> !test(t);
    }

    /**
     * Creates a class filter that is a logical negation of this filter
     * @param classFilter the class filter
     * @return the new class filter
     */
    default ClassFilter not(ClassFilter classFilter) {
        Objects.requireNonNull(classFilter);
        return classFilter.negate();
    }
}
