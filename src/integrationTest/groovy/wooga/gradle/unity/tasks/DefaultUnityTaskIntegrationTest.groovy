/*
 * Copyright 2018-2021 Wooga GmbH
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

package wooga.gradle.unity.tasks

import wooga.gradle.unity.UnityPathResolution
import wooga.gradle.unity.UnityPluginTestOptions
import wooga.gradle.unity.UnityTaskIntegrationTest

class DefaultUnityTaskIntegrationTest extends UnityTaskIntegrationTest<Unity> {

    @UnityPluginTestOptions(unityPath = UnityPathResolution.Default)
    def "creates unity project"() {
        given: "path to future project"
        def project_path = "build/test_project"

        and: "a pre installed unity editor"
        def unityPath = getUnityPath()
        environmentVariables.set("UNITY_PATH", unityPath)

        and: "a build script"
        appendToMockTask("createProject = \"${project_path}\"",
                // We need to select a valid build target before loading
                "buildTarget = \"Android\"")

        when:
        def result = runTasksSuccessfully(mockTaskName)

        then:
        result.standardOutput.contains("Starting process 'command '${unityPath}'")
        fileExists(project_path)
        fileExists(project_path, "Assets")
        fileExists(project_path, "Library")
        fileExists(project_path, "ProjectSettings")
    }

//
//    //I run this test only in macOS due to the path encoding issues.
//    @IgnoreIf({ System.getProperty("os.name").toLowerCase().contains("windows") })
//    @Unroll
//    @RestoreSystemProperties
//    def "calls unity with correct logFile parameter with tasktype #taskType.name when redirectStdOut: #redirectStdOut logFilePath: #logFilePath unityVersion: #unityVersion os: #os"() {
//
//
//        given: "a custom build task"
//        buildFile << """
//            task (mUnity, type: ${taskType.name}) {
//                onlyIf = {true}
//                redirectStdOut = ${redirectStdOut}
//            }
//        """.stripIndent()
//
//        if (logFilePath) {
//            buildFile << """
//            mUnity.logFile = ${logFilePath}
//            """.stripIndent()
//        }
//
//        and: "mocked operating syster"
//        System.setProperty('os.name', os)
//
//        and: "mocked unity version"
//        createFile("gradle.properties") << "defaultUnityTestVersion=${unityVersion}"
//
//        and: "custom setting based on type"
//        if (taskType == UnityPackage) {
//            createFile("path/to/some/files")
//            buildFile << """
//            mUnity.inputFiles "path/to/some/files"
//            """
//        }
//
//        if (taskType == ReturnLicense) {
//            buildFile << """
//            mUnity.licenseDirectory = projectDir
//            """
//        }
//
//        and: "make sure the test file exists"
//        def testFile = createFile("test/file")
//
//        when:
//        def result = runTasks("mUnity")
//
//        then:
//        result.wasExecuted("mUnity")
//        result.standardOutput.contains("Starting process 'command '")
//        def cmdSwitch = expectedCommandlineSwitch.replace("#{DEFAULT_LOG_FILE_PATH}", new File(projectDir, "build/logs").path).replace("#{PROVIDED_LOG_FILE_PATH}", new File(projectDir, "test/file").path)
//        result.standardOutput.contains("${cmdSwitch}".trim())
//
//        where:
//        taskType      | redirectStdOut | logFilePath         | unityVersion | expectedCommandlineSwitch            | os
//        Unity         | true  | null                | "2018.1.0f1" | "-logFile"                           | "Mac OS X"
//        Unity         | true  | null                | "2018.1.0f1" | "-logFile"                           | "Linux"
//        Unity         | true  | null                | "2018.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Windows 7"
//        Unity         | true  | null                | "2019.1.0f1" | "-logFile -"                         | "Mac OS X"
//        Unity         | true  | null                | "2019.1.0f1" | "-logFile -"                         | "Linux"
//        Unity         | true  | null                | "2019.1.0f1" | "-logFile -"                         | "Windows 7"
//        Unity         | true  | 'file("test/file")' | "2018.1.0f1" | "-logFile"                           | "Mac OS X"
//        Unity         | true  | 'file("test/file")' | "2018.1.0f1" | "-logFile"                           | "Linux"
//        Unity         | true  | 'file("test/file")' | "2018.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Windows 7"
//        Unity         | true  | 'file("test/file")' | "2019.1.0f1" | "-logFile -"                         | "Mac OS X"
//        Unity         | true  | 'file("test/file")' | "2019.1.0f1" | "-logFile -"                         | "Linux"
//        Unity         | true  | 'file("test/file")' | "2019.1.0f1" | "-logFile -"                         | "Windows 7"
//        Unity         | false | null                | "2018.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Mac OS X"
//        Unity         | false | null                | "2018.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Linux"
//        Unity         | false | null                | "2018.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Windows 7"
//        Unity         | false | null                | "2019.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Mac OS X"
//        Unity         | false | null                | "2019.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Linux"
//        Unity         | false | null                | "2019.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Windows 7"
//        Unity         | false | 'file("test/file")' | "2018.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Mac OS X"
//        Unity         | false | 'file("test/file")' | "2018.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Linux"
//        Unity         | false | 'file("test/file")' | "2018.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Windows 7"
//        Unity         | false | 'file("test/file")' | "2019.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Mac OS X"
//        Unity         | false | 'file("test/file")' | "2019.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Linux"
//        Unity         | false | 'file("test/file")' | "2019.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Windows 7"
//
//        Activate      | true  | null                | "2018.1.0f1" | "-logFile"                           | "Mac OS X"
//        Activate      | true  | null                | "2018.1.0f1" | "-logFile"                           | "Linux"
//        Activate      | true  | null                | "2018.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Windows 7"
//        Activate      | true  | null                | "2019.1.0f1" | "-logFile -"                         | "Mac OS X"
//        Activate      | true  | null                | "2019.1.0f1" | "-logFile -"                         | "Linux"
//        Activate      | true  | null                | "2019.1.0f1" | "-logFile -"                         | "Windows 7"
//        Activate      | true  | 'file("test/file")' | "2018.1.0f1" | "-logFile"                           | "Mac OS X"
//        Activate      | true  | 'file("test/file")' | "2018.1.0f1" | "-logFile"                           | "Linux"
//        Activate      | true  | 'file("test/file")' | "2018.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Windows 7"
//        Activate      | true  | 'file("test/file")' | "2019.1.0f1" | "-logFile -"                         | "Mac OS X"
//        Activate      | true  | 'file("test/file")' | "2019.1.0f1" | "-logFile -"                         | "Linux"
//        Activate      | true  | 'file("test/file")' | "2019.1.0f1" | "-logFile -"                         | "Windows 7"
//        Activate      | false | null                | "2018.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Mac OS X"
//        Activate      | false | null                | "2018.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Linux"
//        Activate      | false | null                | "2018.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Windows 7"
//        Activate      | false | null                | "2019.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Mac OS X"
//        Activate      | false | null                | "2019.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Linux"
//        Activate      | false | null                | "2019.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Windows 7"
//        Activate      | false | 'file("test/file")' | "2018.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Mac OS X"
//        Activate      | false | 'file("test/file")' | "2018.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Linux"
//        Activate      | false | 'file("test/file")' | "2018.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Windows 7"
//        Activate      | false | 'file("test/file")' | "2019.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Mac OS X"
//        Activate      | false | 'file("test/file")' | "2019.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Linux"
//        Activate      | false | 'file("test/file")' | "2019.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Windows 7"
//
//        ReturnLicense | true  | null                | "2018.1.0f1" | "-logFile"                           | "Mac OS X"
//        ReturnLicense | true  | null                | "2018.1.0f1" | "-logFile"                           | "Linux"
//        ReturnLicense | true  | null                | "2018.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Windows 7"
//        ReturnLicense | true  | null                | "2019.1.0f1" | "-logFile -"                         | "Mac OS X"
//        ReturnLicense | true  | null                | "2019.1.0f1" | "-logFile -"                         | "Linux"
//        ReturnLicense | true  | null                | "2019.1.0f1" | "-logFile -"                         | "Windows 7"
//        ReturnLicense | true  | 'file("test/file")' | "2018.1.0f1" | "-logFile"                           | "Mac OS X"
//        ReturnLicense | true  | 'file("test/file")' | "2018.1.0f1" | "-logFile"                           | "Linux"
//        ReturnLicense | true  | 'file("test/file")' | "2018.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Windows 7"
//        ReturnLicense | true  | 'file("test/file")' | "2019.1.0f1" | "-logFile -"                         | "Mac OS X"
//        ReturnLicense | true  | 'file("test/file")' | "2019.1.0f1" | "-logFile -"                         | "Linux"
//        ReturnLicense | true  | 'file("test/file")' | "2019.1.0f1" | "-logFile -"                         | "Windows 7"
//        ReturnLicense | false | null                | "2018.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Mac OS X"
//        ReturnLicense | false | null                | "2018.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Linux"
//        ReturnLicense | false | null                | "2018.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Windows 7"
//        ReturnLicense | false | null                | "2019.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Mac OS X"
//        ReturnLicense | false | null                | "2019.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Linux"
//        ReturnLicense | false | null                | "2019.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Windows 7"
//        ReturnLicense | false | 'file("test/file")' | "2018.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Mac OS X"
//        ReturnLicense | false | 'file("test/file")' | "2018.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Linux"
//        ReturnLicense | false | 'file("test/file")' | "2018.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Windows 7"
//        ReturnLicense | false | 'file("test/file")' | "2019.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Mac OS X"
//        ReturnLicense | false | 'file("test/file")' | "2019.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Linux"
//        ReturnLicense | false | 'file("test/file")' | "2019.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Windows 7"
//
//        UnityPackage  | true  | null                | "2018.1.0f1" | "-logFile"                           | "Mac OS X"
//        UnityPackage  | true  | null                | "2018.1.0f1" | "-logFile"                           | "Linux"
//        UnityPackage  | true  | null                | "2018.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Windows 7"
//        UnityPackage  | true  | null                | "2019.1.0f1" | "-logFile -"                         | "Mac OS X"
//        UnityPackage  | true  | null                | "2019.1.0f1" | "-logFile -"                         | "Linux"
//        UnityPackage  | true  | null                | "2019.1.0f1" | "-logFile -"                         | "Windows 7"
//        UnityPackage  | true  | 'file("test/file")' | "2018.1.0f1" | "-logFile"                           | "Mac OS X"
//        UnityPackage  | true  | 'file("test/file")' | "2018.1.0f1" | "-logFile"                           | "Linux"
//        UnityPackage  | true  | 'file("test/file")' | "2018.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Windows 7"
//        UnityPackage  | true  | 'file("test/file")' | "2019.1.0f1" | "-logFile -"                         | "Mac OS X"
//        UnityPackage  | true  | 'file("test/file")' | "2019.1.0f1" | "-logFile -"                         | "Linux"
//        UnityPackage  | true  | 'file("test/file")' | "2019.1.0f1" | "-logFile -"                         | "Windows 7"
//        UnityPackage  | false | null                | "2018.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Mac OS X"
//        UnityPackage  | false | null                | "2018.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Linux"
//        UnityPackage  | false | null                | "2018.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Windows 7"
//        UnityPackage  | false | null                | "2019.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Mac OS X"
//        UnityPackage  | false | null                | "2019.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Linux"
//        UnityPackage  | false | null                | "2019.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Windows 7"
//        UnityPackage  | false | 'file("test/file")' | "2018.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Mac OS X"
//        UnityPackage  | false | 'file("test/file")' | "2018.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Linux"
//        UnityPackage  | false | 'file("test/file")' | "2018.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Windows 7"
//        UnityPackage  | false | 'file("test/file")' | "2019.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Mac OS X"
//        UnityPackage  | false | 'file("test/file")' | "2019.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Linux"
//        UnityPackage  | false | 'file("test/file")' | "2019.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Windows 7"
//
//        Test          | true           | null                | "2018.1.0f1" | "-logFile"                           | "Mac OS X"
//        Test          | true           | null                | "2018.1.0f1" | "-logFile"                           | "Linux"
//        Test          | true           | null                | "2018.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Windows 7"
//        Test          | true           | null                | "2019.1.0f1" | "-logFile -"                         | "Mac OS X"
//        Test          | true           | null                | "2019.1.0f1" | "-logFile -"                         | "Linux"
//        Test          | true           | null                | "2019.1.0f1" | "-logFile -"                         | "Windows 7"
//        Test          | true           | 'file("test/file")' | "2018.1.0f1" | "-logFile"                           | "Mac OS X"
//        Test          | true           | 'file("test/file")' | "2018.1.0f1" | "-logFile"                           | "Linux"
//        Test          | true           | 'file("test/file")' | "2018.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Windows 7"
//        Test          | true           | 'file("test/file")' | "2019.1.0f1" | "-logFile -"                         | "Mac OS X"
//        Test          | true           | 'file("test/file")' | "2019.1.0f1" | "-logFile -"                         | "Linux"
//        Test          | true           | 'file("test/file")' | "2019.1.0f1" | "-logFile -"                         | "Windows 7"
//        Test          | false          | null                | "2018.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Mac OS X"
//        Test          | false          | null                | "2018.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Linux"
//        Test          | false          | null                | "2018.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Windows 7"
//        Test          | false          | null                | "2019.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Mac OS X"
//        Test          | false          | null                | "2019.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Linux"
//        Test          | false          | null                | "2019.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Windows 7"
//        Test          | false          | 'file("test/file")' | "2018.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Mac OS X"
//        Test          | false          | 'file("test/file")' | "2018.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Linux"
//        Test          | false          | 'file("test/file")' | "2018.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Windows 7"
//        Test          | false          | 'file("test/file")' | "2019.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Mac OS X"
//        Test          | false          | 'file("test/file")' | "2019.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Linux"
//        Test          | false          | 'file("test/file")' | "2019.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Windows 7"
//    }
//


//    @Unroll
//    @Ignore("test occasionally fails on jenkins")
//    //test occasionally fails on jenkins
//    //@IgnoreIf({ System.getProperty("os.name").toLowerCase().contains("windows") })
//    def "redirects unity log to stdout when redirectStdOut is set to true for #taskType"() {
//        given: "a custom build task"
//        buildFile << """
//            task (mUnity, type: ${taskType.name}) {
//                dependsOn unitySetup
//                redirectStdOut = false
//            }
//        """.stripIndent()
//
//        when:
//        def result = runTasks("mUnity")
//
//        then:
//        !result.standardOutput.contains("Next license update check is after")
//
//        when:
//        buildFile << """
//            mUnity.redirectStdOut = true
//        """.stripIndent()
//        result = runTasks("mUnity")
//
//        then:
//        result.standardOutput.contains("Next license update check is after")
//
//        where:
//        taskType | _
//        Unity | _
//    }
//
//    @Ignore("test occasionally fails on jenkins")
//    @IgnoreIf({ System.getProperty("os.name").toLowerCase().contains("windows") })
//    def "redirects unity log to stdout and custom logfile if provided"() {
//        given: "a custom log file location"
//        def logFile = File.createTempFile("log", "out")
//        and: "a custom build task"
//        buildFile << """
//            task (mUnity, type: ${Unity.name}) {
//                dependsOn unitySetup
//                redirectStdOut = true
//                logFile = file('${escapedPath(logFile.path)}')
//            }
//        """.stripIndent()
//
//        when:
//        def result = runTasks("mUnity")
//
//        then:
//        result.standardOutput.contains("Next license update check is after")
//        logFile.text.contains("Next license update check is after")
//    }
}
