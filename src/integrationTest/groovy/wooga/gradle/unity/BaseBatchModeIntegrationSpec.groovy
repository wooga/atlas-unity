package wooga.gradle.unity

import nebula.test.IntegrationSpec
import org.apache.commons.lang.StringEscapeUtils
import spock.lang.Unroll
import wooga.gradle.unity.tasks.Activate
import wooga.gradle.unity.tasks.ReturnLicense
import wooga.gradle.unity.tasks.Test
import wooga.gradle.unity.tasks.Unity
import wooga.gradle.unity.tasks.UnityPackage

class BaseBatchModeIntegrationSpec extends UnityIntegrationSpec {


    def setup() {
        buildFile << """
            unity {
                authentication {
                    username = "test@test.test"
                    password = "testtesttest"
                    serial = "abcdefg"
                }
            }
        """.stripIndent()
    }

    @Unroll
    def "can set #property for type #taskType with #method"() {
        given: "a custom build task"
        buildFile << """
            task (mUnity, type: ${taskType.name}) {
                onlyIf = {true}
                $method($value)
            }
        """.stripIndent()

        and: "custom setting based on type"
        if (taskType == UnityPackage) {
            createFile("path/to/some/files")
            buildFile << """
            mUnity.inputFiles "path/to/some/files"
            """
        }

        if (taskType == ReturnLicense) {
            buildFile << """
            mUnity.licenseDirectory = projectDir
            """
        }

        and: "make sure the test file exists"
        def testFile = createFile("test/file")

        when:
        def result = runTasks("mUnity")

        then:
        result.wasExecuted("mUnity")
        result.standardOutput.contains("Starting process 'command '")
        if (checkForFileInCommandline) {
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

    @Unroll
    def "can get #property for type #taskType"() {
        given: "a custom build task"
        buildFile << """
            task (mUnity, type: ${taskType.name}) {
                onlyIf = {true}
                $property = $initialValue
            }
            
            task (mUnity2, type: ${taskType.name}) {
                $property = $value
            }

            mUnity.$property = mUnity2.$property
        """.stripIndent()

        and: "custom setting based on type"
        if (taskType == UnityPackage) {
            createFile("path/to/some/files")
            buildFile << """
            mUnity.inputFiles "path/to/some/files"
            """
        }

        if (taskType == ReturnLicense) {
            buildFile << """
            mUnity.licenseDirectory = projectDir
            """
        }

        and: "make sure the test file exists"
        def testFile = createFile("test/file")

        when:
        def result = runTasks("mUnity")

        then:
        result.wasExecuted("mUnity")
        result.standardOutput.contains("Starting process 'command '")
        if (checkForFileInCommandline) {
            result.standardOutput.contains("$expectedCommandlineSwitch ${testFile.path}".trim())
        }

        where:
        property         | taskType      | initialValue         | value               | expectedCommandlineSwitch | checkForFileInCommandline
        "unityPath"      | Activate      | 'file("test/file2")' | 'file("test/file")' | ""                        | true
        "unityPath"      | ReturnLicense | 'file("test/file2")' | 'file("test/file")' | ""                        | true
        "unityPath"      | Test          | 'file("test/file2")' | 'file("test/file")' | ""                        | true
        "unityPath"      | Unity         | 'file("test/file2")' | 'file("test/file")' | ""                        | true
        "unityPath"      | UnityPackage  | 'file("test/file2")' | 'file("test/file")' | ""                        | true

        "projectPath"    | Activate      | 'file("test/file2")' | 'file("test/file")' | ""                        | false
        "projectPath"    | ReturnLicense | 'file("test/file2")' | 'file("test/file")' | ""                        | false
        "projectPath"    | Test          | 'file("test/file2")' | 'file("test/file")' | "-projectPath"            | true
        "projectPath"    | Unity         | 'file("test/file2")' | 'file("test/file")' | "-projectPath"            | true
        "projectPath"    | UnityPackage  | 'file("test/file2")' | 'file("test/file")' | "-projectPath"            | true

        "logFile"        | Activate      | 'file("test/file2")' | 'file("test/file")' | ""                        | false
        "logFile"        | ReturnLicense | 'file("test/file2")' | 'file("test/file")' | ""                        | false
        "logFile"        | Test          | 'file("test/file2")' | 'file("test/file")' | "-logFile"                | true
        "logFile"        | Unity         | 'file("test/file2")' | 'file("test/file")' | "-logFile"                | true
        "logFile"        | UnityPackage  | 'file("test/file2")' | 'file("test/file")' | "-logFile"                | true

        "redirectStdOut" | Activate      | 'false'              | 'true'              | "-logFile"                | true
        "redirectStdOut" | ReturnLicense | 'false'              | 'true'              | "-logFile"                | true
        "redirectStdOut" | Test          | 'false'              | 'true'              | "-logFile"                | true
        "redirectStdOut" | Unity         | 'false'              | 'true'              | "-logFile"                | true
        "redirectStdOut" | UnityPackage  | 'false'              | 'true'              | "-logFile"                | true
    }
}

//TODO create a base test case for all tasks and run them against it. It is quite complicated to setup a generic test.
class BatchModeIntegrationSpec extends IntegrationSpec {

    def setup() {
        buildFile << """
            group = 'test'
            ${applyPlugin(UnityPlugin)}
            
            unity.projectPath = null

            task (unitySetup, type: ${Unity.name}) {
                args "-createProject ${escapedPath(projectDir.path)}"
                redirectStdOut = false
            }

        """.stripIndent()
    }

    def escapedPath(String path) {
        String osName = System.getProperty("os.name").toLowerCase()
        if (osName.contains("windows")) {
            return StringEscapeUtils.escapeJava(path)
        }
        path
    }

    @Unroll
    def "redirects unity log to stdout when redirectStdOut is set to true for #taskType"() {
        given: "a custom build task"
        buildFile << """
            task (mUnity, type: ${taskType.name}) {
                dependsOn unitySetup
                redirectStdOut = false
            }
        """.stripIndent()

        when:
        def result = runTasks("mUnity")

        then:
        !result.standardOutput.contains("Next license update check is after")

        when:
        buildFile << """
            mUnity.redirectStdOut = true
        """.stripIndent()
        result = runTasksSuccessfully("mUnity")

        then:
        result.standardOutput.contains("Next license update check is after")

        where:
        taskType      | _
        Unity         | _
    }

    def "redirects unity log to stdout and custom logfile if provided"() {
        given: "a custom log file location"
        def logFile = File.createTempFile("log","out")
        and: "a custom build task"
        buildFile << """
            task (mUnity, type: ${Unity.name}) {
                dependsOn unitySetup
                redirectStdOut = true
                logFile = file('${escapedPath(logFile.path)}')
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("mUnity")

        then:
        result.standardOutput.contains("Next license update check is after")
        logFile.text.contains("Next license update check is after")
    }
}
