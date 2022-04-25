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

package wooga.gradle.unity.tasks

import com.wooga.gradle.PlatformUtils
import spock.lang.Unroll

import wooga.gradle.unity.UnityIntegrationSpec
import wooga.gradle.unity.UnityTaskIntegrationSpec
import wooga.gradle.unity.models.APICompatibilityLevel
import wooga.gradle.unity.utils.ProjectSettingsFile

class SetAPICompLevelTaskIntegrationSpec extends UnityIntegrationSpec {

    @Unroll
    def "writes api level with type #type to project settings file correctly #expectedAPICompatibilityLevel"() {
        given: "a valid api compatibility level to set"
        buildFile << """
        ${testTaskName}.${invocation}             
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("test", "-x", "unsetAPICompatibilityLevel")

        then:
        !result.wasSkipped("test")
        result.wasExecuted("setAPICompatibilityLevel")
        !result.wasExecuted("unsetAPICompatibilityLevel")
        def expectedValueMap = APICompatibilityLevel.toMap(expectedAPICompatibilityLevel)
        result.standardOutput.contains("Setting API compatibility level to ${expectedValueMap}")

        where:

        method                     | rawValue                                 | expectedValue                        | type
        _                          | APICompatibilityLevel.net_4_6            | _                                    | "APICompatibilityLevel"
        _                          | APICompatibilityLevel.net_4_6            | _                                    | "Provider<APICompatibilityLevel>"
        _                          | APICompatibilityLevel.net_4_6.toString() | APICompatibilityLevel.net_4_6        | "String"
        _                          | "net2_0"                                 | APICompatibilityLevel.net_2_0        | "String"
        _                          | "net4_6"                                 | APICompatibilityLevel.net_4_6        | "String"
        _                          | "net2_0_subset"                          | APICompatibilityLevel.net_2_0_subset | "String"
        "setApiCompatibilityLevel" | APICompatibilityLevel.net_4_6            | _                                    | "APICompatibilityLevel"
        "setApiCompatibilityLevel" | APICompatibilityLevel.net_4_6            | _                                    | "Provider<APICompatibilityLevel>"
        "setApiCompatibilityLevel" | APICompatibilityLevel.net_4_6.toString() | APICompatibilityLevel.net_4_6        | "String"
        "setApiCompatibilityLevel" | APICompatibilityLevel.net_4_6.toString() | APICompatibilityLevel.net_4_6        | "Provider<String>"

        property = 'apiCompatibilityLevel'
        testTaskName = "setAPICompatibilityLevel"

        value = (type != _) ? wrapValueBasedOnType(rawValue, type.toString(), { String type ->
            switch (type) {
                case "APICompatibilityLevel":
                    return "wooga.gradle.unity.models.APICompatibilityLevel.${rawValue}"
                    break
                default:
                    return rawValue.toString()
            }
        }) : rawValue
        expectedAPICompatibilityLevel = (expectedValue != _) ? expectedValue : rawValue
        defaultAPICompatibilityLevel = APICompatibilityLevel.defaultLevel
        testValue = (expectedValue == _) ? rawValue : expectedValue
        escapedValue = (value instanceof String) ? PlatformUtils.escapedPath(value) : value
        invocation = (method != _) ? "${method}(${escapedValue})" : "${property} = ${escapedValue}"
    }

    def "sets the api level onto the project settings file, then unsets it"() {
        given: "a valid api compatibility level to set"
        buildFile << """
            unity {
               apiCompatibilityLevel = "${expectedAPICompatibilityLevel.toString()}" 
            }
            
        """.stripIndent()

        and: "ensure that the api compatibility level isn't the default"
        def projectSettings = new ProjectSettingsFile(projectSettingsFile)
        def previousAPICompLevelMap = projectSettings.getAPICompatibilityLevelPerPlatform()
        assert previousAPICompLevelMap != null

        when:
        def result = runTasksSuccessfully("test")

        then:
        !result.wasSkipped("test")
        result.wasExecuted("setAPICompatibilityLevel")
        result.wasExecuted("unsetAPICompatibilityLevel")

        def expectedValueMap = APICompatibilityLevel.toMap(expectedAPICompatibilityLevel)
        result.standardOutput.contains("Setting API compatibility level to ${expectedValueMap}")
        result.standardOutput.contains("Setting API compatibility level to ${previousAPICompLevelMap}")

        def updatedProjectSettings = new ProjectSettingsFile(projectSettingsFile)
        def currentAPICompLevelMap = updatedProjectSettings.getAPICompatibilityLevelPerPlatform()
        assert previousAPICompLevelMap == currentAPICompLevelMap

        where:
        expectedAPICompatibilityLevel = APICompatibilityLevel.net_4_6
        defaultAPICompatibilityLevel = APICompatibilityLevel.net_2_0_subset
    }

    def "skips if the api level is the same as the default"() {
        given: "a valid api compatibility level to set"
        buildFile << """
            unity {
               apiCompatibilityLevel = "${expectedAPICompatibilityLevel.toString()}" 
            }
            
        """.stripIndent()

        and:
        def projectSettingsFile = setProjectSettingsFile()
        def projectSettings = new ProjectSettingsFile(projectSettingsFile)
        def previousAPICompLevelMap = projectSettings.getAPICompatibilityLevelPerPlatform()
        assert previousAPICompLevelMap != null
        def apiCompLevelMap = APICompatibilityLevel.toMap(expectedAPICompatibilityLevel)

        assert previousAPICompLevelMap == apiCompLevelMap

        when:
        def result = runTasksSuccessfully("test")

        then:
        !result.wasSkipped("test")
        result.wasSkipped("setAPICompatibilityLevel")
        result.wasSkipped("unsetAPICompatibilityLevel")

        where:
        expectedAPICompatibilityLevel = APICompatibilityLevel.defaultLevel
    }
}

