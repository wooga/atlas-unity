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

import org.apache.commons.lang.StringEscapeUtils
import spock.lang.Ignore
import spock.lang.IgnoreIf
import spock.lang.Unroll
import spock.util.environment.RestoreSystemProperties
import wooga.gradle.unity.tasks.*

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
            assert result.standardOutput.contains("$expectedCommandlineSwitch ${testFile.path}".trim())
        }


        where:
        property      | taskType      | initialValue         | value               | expectedCommandlineSwitch | checkForFileInCommandline
        "unityPath"   | Activate      | 'file("test/file2")' | 'file("test/file")' | ""                        | true
        "unityPath"   | ReturnLicense | 'file("test/file2")' | 'file("test/file")' | ""                        | true
        "unityPath"   | Test          | 'file("test/file2")' | 'file("test/file")' | ""                        | true
        "unityPath"   | Unity         | 'file("test/file2")' | 'file("test/file")' | ""                        | true
        "unityPath"   | UnityPackage  | 'file("test/file2")' | 'file("test/file")' | ""                        | true

        "projectPath" | Activate      | 'file("test/file2")' | 'file("test/file")' | ""                        | false
        "projectPath" | ReturnLicense | 'file("test/file2")' | 'file("test/file")' | ""                        | false
        "projectPath" | Test          | 'file("test/file2")' | 'file("test/file")' | "-projectPath"            | true
        "projectPath" | Unity         | 'file("test/file2")' | 'file("test/file")' | "-projectPath"            | true
        "projectPath" | UnityPackage  | 'file("test/file2")' | 'file("test/file")' | "-projectPath"            | true

        "logFile"     | Activate      | 'file("test/file2")' | 'file("test/file")' | ""                        | false
        "logFile"     | ReturnLicense | 'file("test/file2")' | 'file("test/file")' | ""                        | false
        "logFile"     | Test          | 'file("test/file2")' | 'file("test/file")' | "-logFile"                | true
        "logFile"     | Unity         | 'file("test/file2")' | 'file("test/file")' | "-logFile"                | true
        "logFile"     | UnityPackage  | 'file("test/file2")' | 'file("test/file")' | "-logFile"                | true

//        "redirectStdOut" | Activate      | 'false'              | 'true'              | "-logFile"                | true
//        "redirectStdOut" | ReturnLicense | 'false'              | 'true'              | "-logFile"                | true
//        "redirectStdOut" | Test          | 'false'              | 'true'              | "-logFile"                | true
//        "redirectStdOut" | Unity         | 'false'              | 'true'              | "-logFile"                | true
//        "redirectStdOut" | UnityPackage  | 'false'              | 'true'              | "-logFile"                | true
    }

    //I run this test only in macOS due to the path encoding issues.
    @IgnoreIf({ System.getProperty("os.name").toLowerCase().contains("windows") })
    @Unroll
    @RestoreSystemProperties
    def "calls unity with correct logFile parameter with tasktype #taskType.name when redirectStdOut: #redirectStdOut logFilePath: #logFilePath unityVersion: #unityVersion os: #os"() {


        given: "a custom build task"
        buildFile << """
            task (mUnity, type: ${taskType.name}) {
                onlyIf = {true}
                redirectStdOut = ${redirectStdOut}
            }
        """.stripIndent()

        if (logFilePath) {
            buildFile << """
            mUnity.logFile = ${logFilePath}
            """.stripIndent()
        }

        and: "mocked operating syster"
        System.setProperty('os.name', os)

        and: "mocked unity version"
        createFile("gradle.properties") << "defaultUnityTestVersion=${unityVersion}"

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
        def cmdSwitch = expectedCommandlineSwitch.replace("#{DEFAULT_LOG_FILE_PATH}", new File(projectDir, "build/logs").path).replace("#{PROVIDED_LOG_FILE_PATH}", new File(projectDir, "test/file").path)
        result.standardOutput.contains("${cmdSwitch}".trim())

        where:
        taskType      | redirectStdOut | logFilePath         | unityVersion | expectedCommandlineSwitch            | os
        Unity         | true           | null                | "2018.1.0f1" | "-logFile"                           | "Mac OS X"
        Unity         | true           | null                | "2018.1.0f1" | "-logFile"                           | "Linux"
        Unity         | true           | null                | "2018.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Windows 7"
        Unity         | true           | null                | "2019.1.0f1" | "-logFile -"                         | "Mac OS X"
        Unity         | true           | null                | "2019.1.0f1" | "-logFile -"                         | "Linux"
        Unity         | true           | null                | "2019.1.0f1" | "-logFile -"                         | "Windows 7"
        Unity         | true           | 'file("test/file")' | "2018.1.0f1" | "-logFile"                           | "Mac OS X"
        Unity         | true           | 'file("test/file")' | "2018.1.0f1" | "-logFile"                           | "Linux"
        Unity         | true           | 'file("test/file")' | "2018.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Windows 7"
        Unity         | true           | 'file("test/file")' | "2019.1.0f1" | "-logFile -"                         | "Mac OS X"
        Unity         | true           | 'file("test/file")' | "2019.1.0f1" | "-logFile -"                         | "Linux"
        Unity         | true           | 'file("test/file")' | "2019.1.0f1" | "-logFile -"                         | "Windows 7"
        Unity         | false          | null                | "2018.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Mac OS X"
        Unity         | false          | null                | "2018.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Linux"
        Unity         | false          | null                | "2018.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Windows 7"
        Unity         | false          | null                | "2019.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Mac OS X"
        Unity         | false          | null                | "2019.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Linux"
        Unity         | false          | null                | "2019.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Windows 7"
        Unity         | false          | 'file("test/file")' | "2018.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Mac OS X"
        Unity         | false          | 'file("test/file")' | "2018.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Linux"
        Unity         | false          | 'file("test/file")' | "2018.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Windows 7"
        Unity         | false          | 'file("test/file")' | "2019.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Mac OS X"
        Unity         | false          | 'file("test/file")' | "2019.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Linux"
        Unity         | false          | 'file("test/file")' | "2019.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Windows 7"

        Activate      | true           | null                | "2018.1.0f1" | "-logFile"                           | "Mac OS X"
        Activate      | true           | null                | "2018.1.0f1" | "-logFile"                           | "Linux"
        Activate      | true           | null                | "2018.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Windows 7"
        Activate      | true           | null                | "2019.1.0f1" | "-logFile -"                         | "Mac OS X"
        Activate      | true           | null                | "2019.1.0f1" | "-logFile -"                         | "Linux"
        Activate      | true           | null                | "2019.1.0f1" | "-logFile -"                         | "Windows 7"
        Activate      | true           | 'file("test/file")' | "2018.1.0f1" | "-logFile"                           | "Mac OS X"
        Activate      | true           | 'file("test/file")' | "2018.1.0f1" | "-logFile"                           | "Linux"
        Activate      | true           | 'file("test/file")' | "2018.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Windows 7"
        Activate      | true           | 'file("test/file")' | "2019.1.0f1" | "-logFile -"                         | "Mac OS X"
        Activate      | true           | 'file("test/file")' | "2019.1.0f1" | "-logFile -"                         | "Linux"
        Activate      | true           | 'file("test/file")' | "2019.1.0f1" | "-logFile -"                         | "Windows 7"
        Activate      | false          | null                | "2018.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Mac OS X"
        Activate      | false          | null                | "2018.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Linux"
        Activate      | false          | null                | "2018.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Windows 7"
        Activate      | false          | null                | "2019.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Mac OS X"
        Activate      | false          | null                | "2019.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Linux"
        Activate      | false          | null                | "2019.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Windows 7"
        Activate      | false          | 'file("test/file")' | "2018.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Mac OS X"
        Activate      | false          | 'file("test/file")' | "2018.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Linux"
        Activate      | false          | 'file("test/file")' | "2018.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Windows 7"
        Activate      | false          | 'file("test/file")' | "2019.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Mac OS X"
        Activate      | false          | 'file("test/file")' | "2019.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Linux"
        Activate      | false          | 'file("test/file")' | "2019.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Windows 7"

        ReturnLicense | true           | null                | "2018.1.0f1" | "-logFile"                           | "Mac OS X"
        ReturnLicense | true           | null                | "2018.1.0f1" | "-logFile"                           | "Linux"
        ReturnLicense | true           | null                | "2018.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Windows 7"
        ReturnLicense | true           | null                | "2019.1.0f1" | "-logFile -"                         | "Mac OS X"
        ReturnLicense | true           | null                | "2019.1.0f1" | "-logFile -"                         | "Linux"
        ReturnLicense | true           | null                | "2019.1.0f1" | "-logFile -"                         | "Windows 7"
        ReturnLicense | true           | 'file("test/file")' | "2018.1.0f1" | "-logFile"                           | "Mac OS X"
        ReturnLicense | true           | 'file("test/file")' | "2018.1.0f1" | "-logFile"                           | "Linux"
        ReturnLicense | true           | 'file("test/file")' | "2018.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Windows 7"
        ReturnLicense | true           | 'file("test/file")' | "2019.1.0f1" | "-logFile -"                         | "Mac OS X"
        ReturnLicense | true           | 'file("test/file")' | "2019.1.0f1" | "-logFile -"                         | "Linux"
        ReturnLicense | true           | 'file("test/file")' | "2019.1.0f1" | "-logFile -"                         | "Windows 7"
        ReturnLicense | false          | null                | "2018.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Mac OS X"
        ReturnLicense | false          | null                | "2018.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Linux"
        ReturnLicense | false          | null                | "2018.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Windows 7"
        ReturnLicense | false          | null                | "2019.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Mac OS X"
        ReturnLicense | false          | null                | "2019.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Linux"
        ReturnLicense | false          | null                | "2019.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Windows 7"
        ReturnLicense | false          | 'file("test/file")' | "2018.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Mac OS X"
        ReturnLicense | false          | 'file("test/file")' | "2018.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Linux"
        ReturnLicense | false          | 'file("test/file")' | "2018.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Windows 7"
        ReturnLicense | false          | 'file("test/file")' | "2019.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Mac OS X"
        ReturnLicense | false          | 'file("test/file")' | "2019.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Linux"
        ReturnLicense | false          | 'file("test/file")' | "2019.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Windows 7"

        UnityPackage  | true           | null                | "2018.1.0f1" | "-logFile"                           | "Mac OS X"
        UnityPackage  | true           | null                | "2018.1.0f1" | "-logFile"                           | "Linux"
        UnityPackage  | true           | null                | "2018.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Windows 7"
        UnityPackage  | true           | null                | "2019.1.0f1" | "-logFile -"                         | "Mac OS X"
        UnityPackage  | true           | null                | "2019.1.0f1" | "-logFile -"                         | "Linux"
        UnityPackage  | true           | null                | "2019.1.0f1" | "-logFile -"                         | "Windows 7"
        UnityPackage  | true           | 'file("test/file")' | "2018.1.0f1" | "-logFile"                           | "Mac OS X"
        UnityPackage  | true           | 'file("test/file")' | "2018.1.0f1" | "-logFile"                           | "Linux"
        UnityPackage  | true           | 'file("test/file")' | "2018.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Windows 7"
        UnityPackage  | true           | 'file("test/file")' | "2019.1.0f1" | "-logFile -"                         | "Mac OS X"
        UnityPackage  | true           | 'file("test/file")' | "2019.1.0f1" | "-logFile -"                         | "Linux"
        UnityPackage  | true           | 'file("test/file")' | "2019.1.0f1" | "-logFile -"                         | "Windows 7"
        UnityPackage  | false          | null                | "2018.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Mac OS X"
        UnityPackage  | false          | null                | "2018.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Linux"
        UnityPackage  | false          | null                | "2018.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Windows 7"
        UnityPackage  | false          | null                | "2019.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Mac OS X"
        UnityPackage  | false          | null                | "2019.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Linux"
        UnityPackage  | false          | null                | "2019.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Windows 7"
        UnityPackage  | false          | 'file("test/file")' | "2018.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Mac OS X"
        UnityPackage  | false          | 'file("test/file")' | "2018.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Linux"
        UnityPackage  | false          | 'file("test/file")' | "2018.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Windows 7"
        UnityPackage  | false          | 'file("test/file")' | "2019.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Mac OS X"
        UnityPackage  | false          | 'file("test/file")' | "2019.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Linux"
        UnityPackage  | false          | 'file("test/file")' | "2019.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Windows 7"

        Test          | true           | null                | "2018.1.0f1" | "-logFile"                           | "Mac OS X"
        Test          | true           | null                | "2018.1.0f1" | "-logFile"                           | "Linux"
        Test          | true           | null                | "2018.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Windows 7"
        Test          | true           | null                | "2019.1.0f1" | "-logFile -"                         | "Mac OS X"
        Test          | true           | null                | "2019.1.0f1" | "-logFile -"                         | "Linux"
        Test          | true           | null                | "2019.1.0f1" | "-logFile -"                         | "Windows 7"
        Test          | true           | 'file("test/file")' | "2018.1.0f1" | "-logFile"                           | "Mac OS X"
        Test          | true           | 'file("test/file")' | "2018.1.0f1" | "-logFile"                           | "Linux"
        Test          | true           | 'file("test/file")' | "2018.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Windows 7"
        Test          | true           | 'file("test/file")' | "2019.1.0f1" | "-logFile -"                         | "Mac OS X"
        Test          | true           | 'file("test/file")' | "2019.1.0f1" | "-logFile -"                         | "Linux"
        Test          | true           | 'file("test/file")' | "2019.1.0f1" | "-logFile -"                         | "Windows 7"
        Test          | false          | null                | "2018.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Mac OS X"
        Test          | false          | null                | "2018.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Linux"
        Test          | false          | null                | "2018.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Windows 7"
        Test          | false          | null                | "2019.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Mac OS X"
        Test          | false          | null                | "2019.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Linux"
        Test          | false          | null                | "2019.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Windows 7"
        Test          | false          | 'file("test/file")' | "2018.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Mac OS X"
        Test          | false          | 'file("test/file")' | "2018.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Linux"
        Test          | false          | 'file("test/file")' | "2018.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Windows 7"
        Test          | false          | 'file("test/file")' | "2019.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Mac OS X"
        Test          | false          | 'file("test/file")' | "2019.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Linux"
        Test          | false          | 'file("test/file")' | "2019.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Windows 7"
    }

    @Unroll
    def "set log category with #method to #value"() {
        given:
        buildFile << """
        task (mUnity, type: ${Unity.name})
        """.stripIndent()

        if (value) {
            buildFile << """
            mUnity.$method("${value}")
            """.stripIndent()
        }

        when:
        def result = runTasksSuccessfully("mUnity")

        then:
        result.wasExecuted("mUnity")
        def resultPath = new File(projectDir, "/build/logs/${path}").getPath()
        result.standardOutput.contains("-logFile ${resultPath}")

        where:
        value        | useSetter | path
        "helloworld" | true      | "helloworld/mUnity.log"
        "helloworld" | false     | "helloworld/mUnity.log"
        ""           | true      | "mUnity.log"
        ""           | false     | "mUnity.log"
        null         | true      | "mUnity.log"
        null         | false     | "mUnity.log"

        method = (useSetter) ? "setLogCategory" : "logCategory"

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
    @Ignore("test occasionally fails on jenkins")
    //test occasionally fails on jenkins
    //@IgnoreIf({ System.getProperty("os.name").toLowerCase().contains("windows") })
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
        result = runTasks("mUnity")

        then:
        result.standardOutput.contains("Next license update check is after")

        where:
        taskType | _
        Unity    | _
    }

    @Ignore("test occasionally fails on jenkins")
    @IgnoreIf({ System.getProperty("os.name").toLowerCase().contains("windows") })
    def "redirects unity log to stdout and custom logfile if provided"() {
        given: "a custom log file location"
        def logFile = File.createTempFile("log", "out")
        and: "a custom build task"
        buildFile << """
            task (mUnity, type: ${Unity.name}) {
                dependsOn unitySetup
                redirectStdOut = true
                logFile = file('${escapedPath(logFile.path)}')
            }
        """.stripIndent()

        when:
        def result = runTasks("mUnity")

        then:
        result.standardOutput.contains("Next license update check is after")
        logFile.text.contains("Next license update check is after")
    }
}
