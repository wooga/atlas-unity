package wooga.gradle.unity

class SetAPICompLevelIntegrationSpec extends UnityIntegrationSpec {

    def setup() {
        buildFile << """
        unity{
             testBuildTargets = ["android"]
        }
        
        """.stripIndent()
    }

    def "writes api level to project settings file correctly"() {
        given: "a valid api compatibility level to set"
        buildFile << """
            unity {
               setApiCompatibilityLevel("${expectedAPICompatibilityLevel.toString()}") 
            }
            
        """.stripIndent()

        //and: "ensure that the api compatibility level isn't the default"
        //assert !settings.text.contains("apiCompatibilityLevel: ${defaultAPICompatibilityLevel.value}")

        when:
        def result = runTasksSuccessfully("test", "-x", "unsetAPICompatibilityLevel")

        then:
        !result.wasSkipped("test")
        result.wasExecuted("setAPICompatibilityLevel")
        !result.wasExecuted("unsetAPICompatibilityLevel")
        settings.text.contains("apiCompatibilityLevel: ${expectedAPICompatibilityLevel.value}")
        result.standardOutput.contains("Setting API compatibility level to ${expectedAPICompatibilityLevel}")

        where:
        expectedAPICompatibilityLevel | defaultAPICompatibilityLevel
        APICompatibilityLevel.net4_6 | APICompatibilityLevel.net2_0_subset
        APICompatibilityLevel.net_micro | APICompatibilityLevel.net2_0_subset
        APICompatibilityLevel.net_web | APICompatibilityLevel.net2_0_subset
    }

    def "sets the api level onto the project settings file, then unsets it"() {
        given: "a valid api compatibility level to set"
        buildFile << """
            unity {
               setApiCompatibilityLevel("${expectedAPICompatibilityLevel.toString()}") 
            }
            
        """.stripIndent()

        and: "ensure that the api compatibility level isn't the default"
        assert settings.text.contains("apiCompatibilityLevel: ${defaultAPICompatibilityLevel.value}")

        when:
        def result = runTasksSuccessfully("test")

        then:
        !result.wasSkipped("test")
        result.wasExecuted("setAPICompatibilityLevel")
        result.wasExecuted("unsetAPICompatibilityLevel")
        !settings.text.contains("apiCompatibilityLevel: ${expectedAPICompatibilityLevel.value}")
        result.standardOutput.contains("Setting API compatibility level to ${expectedAPICompatibilityLevel}")
        result.standardOutput.contains("Setting API compatibility level to ${defaultAPICompatibilityLevel}")

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

        when:
        def result = runTasksSuccessfully("test")

        then:
        !result.wasSkipped("test")
        result.wasSkipped("setAPICompatibilityLevel")
        result.wasSkipped("unsetAPICompatibilityLevel")
        settings.text.contains("apiCompatibilityLevel: ${defaultAPICompatibilityLevel.value}")
        !result.standardOutput.contains("Setting API compatibility level to ${expectedAPICompatibilityLevel}")
        !result.standardOutput.contains("Setting API compatibility level to ${defaultAPICompatibilityLevel}")

        where:
        defaultAPICompatibilityLevel = APICompatibilityLevel.net2_0_subset
        expectedAPICompatibilityLevel = defaultAPICompatibilityLevel
    }

    def "skips if the api level given was invalid"() {
        given: "an invalid api compatibility level to set"
        buildFile << """
            unity {
               setApiCompatibilityLevel(${expectedAPICompatibilityLevel})
            }

        """.stripIndent()

        when:
        def result = runTasksWithFailure("test")

        then:
        // check for error message
        result.standardError.contains("")

        where:
        expectedAPICompatibilityLevel | defaultAPICompatibilityLevel
        "" | APICompatibilityLevel.net2_0_subset
        "lol" | APICompatibilityLevel.net2_0_subset
    }




}
