/*
 * Copyright 2018-2020 Wooga GmbH
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

package wooga.gradle.unity.utils


import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory

import java.util.concurrent.Callable


class PropertyLookup {
    /**
     * Provided environment keys
     */
    final List<String> environmentKeys
    /**
     * Provided property keys
     */
    final List<String> propertyKeys
    /**
     * If it can't find the value from either environment or property map, it will return this one
     */
    private final Object defaultValue

    /**
     * @return The default value for this property
     */
    Object getDefaultValue() {
        extractValue(defaultValue)
    }

    /**
     * If set, a prefix to apply to all keys during lookup
     */
    String prefix = ""

    PropertyLookup(List<String> environmentKeys, List<String> propertyKeys, Object defaultValue) {
        this.environmentKeys = environmentKeys
        this.propertyKeys = propertyKeys
        this.defaultValue = defaultValue
    }

    PropertyLookup(List<String> environmentKeys, String propertyKey, Object defaultValue) {
        this(environmentKeys, [propertyKey], defaultValue)
    }

    PropertyLookup(String environmentKey, List<String> propertyKeys, Object defaultValue) {
        this([environmentKey], propertyKeys, defaultValue)
    }

    PropertyLookup(String environmentKey, String propertyKey, Object defaultValue) {
        this([environmentKey], [propertyKey], defaultValue)
    }

    PropertyLookup(Object defaultValue) {
        this([], [], defaultValue)
    }

    /**
     * First, if a 'properties' map is provided (such as through a gradle.properties file), it will look for it there by key
     * Second, it will look in the environment (either provided or the one from the System) by a key
     * If it still hasn't been found, will return a default value (provided during construction)
     * 1
     * @return The value of this property, by first looking it up in a hierarchy.
     *
     */
    Object getValue(Map<String, ?> properties, Map<String, ?> environment = null) {

        // First, we look among properties
        for (key in propertyKeys) {
            if (properties.containsKey(key)) {
                return extractValue(properties.get("${prefix}${key}".toString()))
            }
        }

        // Second, among the environment
        environment = environment ?: System.getenv()
        for (key in environmentKeys) {
            if (environment.containsKey(key)) {
                return extractValue(environment.get("${prefix}${key}".toString()))
            }
        }

        // Fallback to the provided default value
        getDefaultValue()
    }

    private static Object extractValue(Object value) {
        if (value == null) {
            return null
        }
        if (value instanceof Closure) {
            value = value.call()
        } else if (value instanceof Callable) {
            value = ((Callable) value).call()
        }
        value
    }

    Object getValue(Project project) {
        getValue(project.properties, null)
    }

    Object getValue() {
        getValue(null, null)
    }

    Boolean getValueAsBoolean(Map<String, ?> properties, Map<String, ?> env = null) {
        def rawValue = getValue(properties, env)
        if (rawValue) {
            rawValue = rawValue.toString().toLowerCase()
            rawValue = (rawValue == "1" || rawValue == "yes") ? "true" : rawValue
            return Boolean.valueOf(rawValue)
        }
        return false
    }

    String getValueAsString(Map<String, ?> properties, Map<String, ?> environment = null) {
        getValue(properties, environment) as String
    }

//    <T> Provider<T> getProvider(ProviderFactory factory, Map<String, ?> properties, Map<String, ?> env = null) {
//        factory.provider({
//            getValue(properties) as T
//        })
//    }

    Provider<String> getStringValueProvider(ProviderFactory factory, Map<String, ?> properties, Map<String, ?> env = null) {
        factory.provider({
            getValueAsString(properties)
        })
    }

    Provider<Boolean> getBooleanValueProvider(ProviderFactory factory, Map<String, ?> properties, Map<String, ?> env = null) {
        factory.provider({
            getValueAsBoolean(properties)
        })
    }

    Provider<RegularFile> getFileValueProvider(ProviderFactory factory, ProjectLayout layout, Map<String, ?> properties, Map<String, ?> env = null) {
        layout.buildDirectory.file(
                factory.provider({
                    getValueAsString(properties, env)
                })
        )
    }

    Provider<Directory> getDirectoryValueProvider(ProviderFactory factory, ProjectLayout layout, Map<String, ?> properties, Map<String, ?> env = null) {
        layout.buildDirectory.dir(
                factory.provider({
                    getValueAsString(properties, env)
                })
        )
    }

    Provider<String> getStringValueProvider(Project project) {
        getStringValueProvider(project.getProviders(), project.properties, System.getenv())
    }

    Provider<Boolean> getBooleanValueProvider(Project project) {
        getBooleanValueProvider(project.getProviders(), project.properties, System.getenv())
    }

    Provider<RegularFile> getFileValueProvider(Project project) {
        getFileValueProvider(project.providers, project.layout, project.properties, System.getenv())
    }

    Provider<Directory> getDirectoryValueProvider(Project project) {
        getDirectoryValueProvider(project.providers, project.layout, project.properties, System.getenv())
    }

    static String envNameFromProperty(String extensionName, String property) {
        "${extensionName.toUpperCase()}_${property.replaceAll(/([A-Z])/, "_\$1").toUpperCase()}"
    }

    static String convertPropertyToEnvName(String property) {
        property.replaceAll(/([A-Z.])/, '_$1').replaceAll(/[.]/, '').toUpperCase()
    }

}
