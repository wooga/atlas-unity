package wooga.gradle.unity

class SetAPICompLevelIntegrationSpec extends UnityIntegrationSpec {

    def setup() {
        buildFile << """
        unity{
             testBuildTargets = ["android"]
        }
        
        """.stripIndent()
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
        //def result = runTasksSuccessfully("test", "-x", "unsetAPICompatibilityLevel")

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

    def "writes correct api level to project settings file correctly"() {

    }

    def "return license runs always even when build fails"() {
        given: "a build script with fake test unity location"
        buildFile << """
            unity {
                authentication {
                    username = "test@test.test"
                    password = "testtesttest"
                    serial = "abcdefg"
                }
            }
            
            task (mUnity, type: wooga.gradle.unity.tasks.Test)
            
            task (fail) {
                doLast {
                    exec{
                        commandline "/does/not/exist"
                    }
                }
            }
                     
        """.stripIndent()

        when:
        def result = runTasksWithFailure("mUnity", "fail", "test")

        then:
        result.wasExecuted("activateUnity")
        result.wasExecuted("fail")
        result.wasExecuted("mUnity")
        result.wasExecuted("returnUnityLicense")
        !result.wasExecuted("test")
    }

    def "skips returnUnityLicense when license directory is empty"() {
        given: "a build script with fake test unity location"
        buildFile << """
            unity {
                authentication {
                    username = "test@test.test"
                    password = "testtesttest"
                    serial = "abcdefg"
                }
            }    
            
            returnUnityLicense {
                licenseDirectory = project.file("${File.createTempDir().path.replace('\\','\\\\')}")
            }               
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("test")

        then:
        result.wasExecuted("activateUnity")
        result.wasExecuted("returnUnityLicense")
        result.standardOutput.contains("returnUnityLicense NO-SOURCE") || result.standardOutput.contains("Skipping task ':returnUnityLicense' as it has no source files")
    }

    def "skips activateUnity and returnUnityLicense when authentication is not set"() {
        when:
        def result = runTasksSuccessfully("test")

        then:
        result.wasSkipped("activateUnity")
        result.wasSkipped("returnUnityLicense")
    }

}
