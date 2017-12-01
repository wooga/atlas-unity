package wooga.gradle.unity

import spock.lang.Unroll
import wooga.gradle.unity.tasks.Activate
import wooga.gradle.unity.tasks.ReturnLicense
import wooga.gradle.unity.tasks.Test
import wooga.gradle.unity.tasks.Unity
import wooga.gradle.unity.tasks.UnityPackage

class BaseBatchModeIntegrationSpec extends UnityIntegrationSpec {

    @Unroll
    def "can set #property for type #taskType with #method"() {
        given: "build file with custom task"
        buildFile << """
            unity {
                authentication {
                    username = "test@test.test"
                    password = "testtesttest"
                    serial = "abcdefg"
                }
            }

            task (mUnity, type: ${taskType.name}) {
               onlyIf = {true}
               $method($value) 
            }
        """.stripIndent()

        if (taskType == UnityPackage) {
            createFile("path/to/some/files")
            buildFile << """
            mUnity.inputFiles "path/to/some/files"
            """
        }

        if (taskType == ReturnLicense) {
            buildFile << """
            mUnity.licenseDirectory = unity.unityLicenseDirectory
            """
        }

        and: "make sure the test file exists"
        def testFile = createFile("test/file")

        when:
        def result = runTasks("mUnity")

        then:
        result.wasExecuted("mUnity")
        result.standardOutput.contains("Starting process 'command '")
        if(checkForFileInCommandline) {
            result.standardOutput.contains("$expectedCommandlineSwitch ${testFile.path}".trim())
        }

        where:
        property      | taskType      | useSetter | value                 | expectedCommandlineSwitch | checkForFileInCommandline
        "unityPath"   | Activate      | false     | 'file("test/file")'   | ""                        | true
        "unityPath"   | Activate      | true      | 'file("test/file")'   | ""                        | true
        "unityPath"   | ReturnLicense | false     | 'file("test/file")'   | ""                        | true
        "unityPath"   | ReturnLicense | true      | 'file("test/file")'   | ""                        | true
        "unityPath"   | Test          | false     | 'file("test/file")'   | ""                        | true
        "unityPath"   | Test          | true      | 'file("test/file")'   | ""                        | true
        "unityPath"   | Unity         | false     | 'file("test/file")'   | ""                        | true
        "unityPath"   | Unity         | true      | 'file("test/file")'   | ""                        | true
        "unityPath"   | UnityPackage  | false     | 'file("test/file")'   | ""                        | true
        "unityPath"   | UnityPackage  | true      | 'file("test/file")'   | ""                        | true

        "projectPath" | Activate      | false     | 'file("test/file")'   | ""                        | false
        "projectPath" | Activate      | true      | 'file("test/file")'   | ""                        | false
        "projectPath" | ReturnLicense | false     | 'file("test/file")'   | ""                        | false
        "projectPath" | ReturnLicense | true      | 'file("test/file")'   | ""                        | false
        "projectPath" | Test          | false     | 'file("test/file")'   | "-projectPath"            | true
        "projectPath" | Test          | true      | 'file("test/file")'   | "-projectPath"            | true
        "projectPath" | Unity         | false     | 'file("test/file")'   | "-projectPath"            | true
        "projectPath" | Unity         | true      | 'file("test/file")'   | "-projectPath"            | true
        "projectPath" | UnityPackage  | false     | 'file("test/file")'   | "-projectPath"            | true
        "projectPath" | UnityPackage  | true      | 'file("test/file")'   | "-projectPath"            | true

        "logFile"     | Activate      | false     | 'file("test/file")'   | ""                        | false
        "logFile"     | Activate      | false     | '{file("test/file")}' | ""                        | false
        "logFile"     | Activate      | true      | 'file("test/file")'   | ""                        | false
        "logFile"     | Activate      | true      | '{file("test/file")}' | ""                        | false
        "logFile"     | ReturnLicense | false     | 'file("test/file")'   | ""                        | false
        "logFile"     | ReturnLicense | false     | '{file("test/file")}' | ""                        | false
        "logFile"     | ReturnLicense | true      | 'file("test/file")'   | ""                        | false
        "logFile"     | ReturnLicense | true      | '{file("test/file")}' | ""                        | false
        "logFile"     | Test          | false     | 'file("test/file")'   | "-logFile"                | true
        "logFile"     | Test          | false     | '{file("test/file")}' | "-logFile"                | true
        "logFile"     | Test          | true      | 'file("test/file")'   | "-logFile"                | true
        "logFile"     | Test          | true      | '{file("test/file")}' | "-logFile"                | true
        "logFile"     | Unity         | false     | 'file("test/file")'   | "-logFile"                | true
        "logFile"     | Unity         | false     | '{file("test/file")}' | "-logFile"                | true
        "logFile"     | Unity         | true      | 'file("test/file")'   | "-logFile"                | true
        "logFile"     | Unity         | true      | '{file("test/file")}' | "-logFile"                | true
        "logFile"     | UnityPackage  | false     | 'file("test/file")'   | "-logFile"                | true
        "logFile"     | UnityPackage  | false     | '{file("test/file")}' | "-logFile"                | true
        "logFile"     | UnityPackage  | true      | 'file("test/file")'   | "-logFile"                | true
        "logFile"     | UnityPackage  | true      | '{file("test/file")}' | "-logFile"                | true

        method = (useSetter) ? "set${property.capitalize()}" : property
    }


}
