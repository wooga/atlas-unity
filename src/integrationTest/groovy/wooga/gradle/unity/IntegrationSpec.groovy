/*
 * Copyright 2018 Wooga GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package wooga.gradle.unity

import nebula.test.functional.ExecutionResult
import org.junit.Rule
import org.junit.contrib.java.lang.system.EnvironmentVariables
import org.junit.contrib.java.lang.system.ProvideSystemProperty

class IntegrationSpec extends nebula.test.IntegrationSpec{

    @Rule
    ProvideSystemProperty properties = new ProvideSystemProperty("ignoreDeprecations", "true")

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables()

    def escapedPath(String path) {
        String osName = System.getProperty("os.name").toLowerCase()
        if (osName.contains("windows")) {
            return StringEscapeUtils.escapeJava(path)
        }
        path
    }

    static String osPath(String path) {
        String osName = System.getProperty("os.name").toLowerCase()
        if (osName.contains("windows")) {
            path = path.startsWith('/') ? "c:" + path : path
        }
        new File(path).path
    }

    def setup() {
        def gradleVersion = System.getenv("GRADLE_VERSION")
        if (gradleVersion) {
            this.gradleVersion = gradleVersion
            fork = true
        }
        environmentVariables.clear(UnityPluginConsts.REDIRECT_STDOUT_ENV_VAR)
    }

    enum PropertyLocation {
        none, script, property, env

        String reason() {
            switch (this) {
                case script:
                    return "value is provided in script"
                case property:
                    return "value is provided in props"
                case env:
                    return "value is set in env"
                default:
                    return "no value was configured"
            }
        }
    }

    String envNameFromProperty(String extensionName, String property) {
        "${extensionName.toUpperCase()}_${property.replaceAll(/([A-Z])/, "_\$1").toUpperCase()}"
    }


    Boolean outputContains(ExecutionResult result, String message) {
        result.standardOutput.contains(message) || result.standardError.contains(message)
    }

    String wrapValueBasedOnType(Object rawValue, Class type, Closure<String> fallback = null) {
        wrapValueBasedOnType(rawValue, type.simpleName, fallback)
    }

    String wrapValueBasedOnType(Object rawValue, String type, Closure<String> fallback = null) {
        def value
        def rawValueEscaped = String.isInstance(rawValue) ? "'${rawValue}'" : rawValue
        def subtypeMatches = type =~ /(?<mainType>\w+)<(?<subType>[\w<>]+)>/
        def subType = (subtypeMatches.matches()) ? subtypeMatches.group("subType") : null
        type = (subtypeMatches.matches()) ? subtypeMatches.group("mainType") : type
        switch (type) {
            case "Closure":
                if (subType) {
                    value = "{${wrapValueBasedOnType(rawValue, subType, fallback)}}"
                } else {
                    value = "{$rawValueEscaped}"
                }
                break
            case "Callable":
                value = "new java.util.concurrent.Callable<${rawValue.class.typeName}>() {@Override ${rawValue.class.typeName} call() throws Exception { $rawValueEscaped }}"
                break
            case "Object":
                value = "new Object() {@Override String toString() { ${rawValueEscaped}.toString() }}"
                break
            case "Provider":
                switch (subType) {
                    case "RegularFile":
                        value = "project.layout.file(${wrapValueBasedOnType(rawValue, "Provider<File>", fallback)})"
                        break
                    case "Directory":
                        value = "project.provider({def d = project.objects.directoryProperty();d.set(${wrapValueBasedOnType(rawValue, "File", fallback)});d.get()})"
                        break
                    default:
                        value = "project.provider(${wrapValueBasedOnType(rawValue, "Closure<${subType}>", fallback)})"
                        break
                }
                break
            case "String":
                value = "${escapedPath(rawValueEscaped.toString())}"
                break
            case "String[]":
                value = "'${rawValue.collect { it }.join(",")}'.split(',')"
                break
            case "File":
                value = "new File('${escapedPath(rawValue.toString())}')"
                break
            case "String...":
                value = "${rawValue.collect { '"' + it + '"' }.join(", ")}"
                break
            case "List":
                value = "[${rawValue.collect { '"' + it + '"' }.join(", ")}]"
                break
            case "Map":
                value = "[" + rawValue.collect { k, v -> "${wrapValueBasedOnType(k, k.getClass(), fallback)} : ${wrapValueBasedOnType(v, v.getClass(), fallback)}" }.join(", ") + "]"
                value = value == "[]" ? "[:]" : value
                break
            default:
                value = (fallback) ? fallback.call(type) : rawValue
        }
        value
    }
}
