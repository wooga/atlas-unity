package wooga.gradle.unity.tasks

import com.wooga.spock.extensions.unity.UnityPluginTestOptions
import wooga.gradle.unity.UnityTaskIntegrationSpec

class ReturnLicenseTaskIntegrationSpec extends UnityTaskIntegrationSpec<ReturnLicense> {

    @UnityPluginTestOptions(addMockTask = false, disableAutoActivateAndLicense = false)
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

    @UnityPluginTestOptions(addMockTask = false, disableAutoActivateAndLicense = false)
    def "skips returnUnityLicense when license directory is empty"() {
        given: "a build script with fake test unity location"
        buildFile << """
            unity {
                authentication.username = "test@test.test"
                authentication.password = "testtesttest"
                authentication.serial = "abcdefg"
            }    

            task (testo, type: wooga.gradle.unity.tasks.Test)
            
            returnUnityLicense {
                licenseDirectory = project.file("${File.createTempDir().path.replace('\\', '\\\\')}")
            }               
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("testo")

        then:
        result.wasExecuted("activateUnity")
        result.wasExecuted("returnUnityLicense")
        result.standardOutput.contains("returnUnityLicense NO-SOURCE") || result.standardOutput.contains("Skipping task ':returnUnityLicense' as it has no source files")
    }

    def "run returnUnityLicense from cli"() {
        when:
        def result = runTasksSuccessfully("returnUnityLicense")

        then:
        result.wasExecuted("returnUnityLicense")
        !result.wasExecuted("activateUnity")
    }

    def "skips returnUnityLicense when autoReturnLicense is false"() {
        given: "a build script with fake test unity location"
        buildFile << """
            unity {
                    authentication.username = "test@test.test"
                    authentication.password = "testtesttest"
                    authentication.serial = "abcdefg"
                
                autoActivateUnity = true
                autoReturnLicense = false
            }    
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("test")

        then:
        result.wasExecuted("activateUnity")
        result.wasExecuted("test")
        result.wasSkipped("returnUnityLicense")
    }
}
