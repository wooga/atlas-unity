package wooga.gradle.unity

import spock.lang.Unroll
import wooga.gradle.unity.utils.internal.ProjectSettings

class SetAPICompLevelIntegrationSpec extends UnityIntegrationSpec {

    def setup() {
        buildFile << """
        unity{
             testBuildTargets = ["android"]
        }
        
        """.stripIndent()
    }

    @Unroll
    def "writes api level with type #type to project settings file correctly #expectedAPICompatibilityLevel"() {
        given: "a valid api compatibility level to set"
        buildFile << """
        ${taskName}.${invocation} 
            
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

        property                | method                     | rawValue                                | expectedValue                | type
        'apiCompatibilityLevel' | _                          | APICompatibilityLevel.net4_6            | _                            | "APICompatibilityLevel"
        'apiCompatibilityLevel' | _                          | APICompatibilityLevel.net4_6            | _                            | "Closure<APICompatibilityLevel>"
        'apiCompatibilityLevel' | _                          | APICompatibilityLevel.net4_6.toString() | APICompatibilityLevel.net4_6 | "String"
        'apiCompatibilityLevel' | _                          | APICompatibilityLevel.net4_6.toString() | APICompatibilityLevel.net4_6 | "Closure<String>"
        'apiCompatibilityLevel' | _                          | APICompatibilityLevel.net4_6.value      | APICompatibilityLevel.net4_6 | "Integer"
        'apiCompatibilityLevel' | _                          | APICompatibilityLevel.net4_6.value      | APICompatibilityLevel.net4_6 | "Closure<Integer>"

        'apiCompatibilityLevel' | "apiCompatibilityLevel" | APICompatibilityLevel.net4_6            | _                            | "APICompatibilityLevel"
        'apiCompatibilityLevel' | "apiCompatibilityLevel" | APICompatibilityLevel.net4_6            | _                            | "Closure<APICompatibilityLevel>"
        'apiCompatibilityLevel' | "apiCompatibilityLevel" | APICompatibilityLevel.net4_6.toString() | APICompatibilityLevel.net4_6 | "String"
        'apiCompatibilityLevel' | "apiCompatibilityLevel" | APICompatibilityLevel.net4_6.toString() | APICompatibilityLevel.net4_6 | "Closure<String>"
        'apiCompatibilityLevel' | "apiCompatibilityLevel" | APICompatibilityLevel.net4_6.value      | APICompatibilityLevel.net4_6 | "Integer"
        'apiCompatibilityLevel' | "apiCompatibilityLevel" | APICompatibilityLevel.net4_6.value      | APICompatibilityLevel.net4_6 | "Closure<Integer>"

        taskName = "setAPICompatibilityLevel"
        value = (type != _) ? wrapValueBasedOnType(rawValue, type.toString(), { String type ->
            switch (type) {
                case "APICompatibilityLevel":
                    return "wooga.gradle.unity.APICompatibilityLevel.${rawValue}"
                    break
                default:
                    return rawValue.toString()
            }
        }) : rawValue
        expectedAPICompatibilityLevel = (expectedValue != _) ? expectedValue : rawValue
        defaultAPICompatibilityLevel = APICompatibilityLevel.net2_0_subset
        testValue = (expectedValue == _) ? rawValue : expectedValue
        escapedValue = (value instanceof String) ? escapedPath(value) : value
        invocation = (method != _) ? "${method}(${escapedValue})" : "${property} = ${escapedValue}"
    }

    def "sets the api level onto the project settings file, then unsets it"() {
        given: "a valid api compatibility level to set"
        buildFile << """
            unity {
               setApiCompatibilityLevel("${expectedAPICompatibilityLevel.toString()}") 
            }
            
        """.stripIndent()

        and: "ensure that the api compatibility level isn't the default"
        def projectSettings = new ProjectSettings(settings)
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

        def updatedProjectSettings = new ProjectSettings(settings)
        def currentAPICompLevelMap = updatedProjectSettings.getAPICompatibilityLevelPerPlatform()
        assert previousAPICompLevelMap == currentAPICompLevelMap

        where:
        expectedAPICompatibilityLevel = APICompatibilityLevel.net4_6
        defaultAPICompatibilityLevel = APICompatibilityLevel.net2_0_subset
    }

    def "skips if the api level is the same as the default"() {
        given: "a valid api compatibility level to set"
        buildFile << """
            unity {
               setApiCompatibilityLevel("${expectedAPICompatibilityLevel.toString()}") 
            }
            
        """.stripIndent()

        and:
        def projectSettings = new ProjectSettings(settings)
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
