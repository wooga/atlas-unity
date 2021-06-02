/*
 * Copyright 2021 Wooga GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package wooga.gradle.unity.traits

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input

trait ArgumentsSpec {

    private final Provider<List<String>> arguments

    /**
     * @return Required arguments, which are supplied through a provider
     */
    @Input
    Provider<List<String>> getArguments() {
        arguments
    }

    private final ListProperty<String> additionalArguments

    /**
     * @return Additional arguments which can be added
     */
    @Input
    ListProperty<String> getAdditionalArguments() {
        additionalArguments
    }

    void setAdditionalArguments(Iterable<String> value) {
        additionalArguments.set(value)
    }

    void setAdditionalArguments(String value) {
        additionalArguments.set([value])
    }

    void setAdditionalArguments(Provider<? extends Iterable<String>> value) {
        additionalArguments.set(value)
    }

    void argument(String value) {
        additionalArguments.add(value)
    }

    void arguments(Iterable<String> value) {
        additionalArguments.addAll(value)
    }

    void arguments(String... value) {
        arguments(value.toList())
    }

    /**
     * @return Retrieves {@code arguments} and {@code additionalArguments}
     */
    List<String> getAllArguments() {
        List<String> result = new ArrayList<String>()

        if (arguments.present) {
            arguments.get().each {
                result << it
            }
        }

        if (additionalArguments.present) {
            additionalArguments.get().each {
                result << it
            }
        }
        result
    }

}
