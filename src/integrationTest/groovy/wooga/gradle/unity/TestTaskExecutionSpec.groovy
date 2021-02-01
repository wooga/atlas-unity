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

import spock.lang.Unroll
import wooga.gradle.unity.batchMode.BatchModeFlags
import wooga.gradle.unity.utils.internal.ProjectSettingsSpec

import java.nio.file.Files
import java.nio.file.StandardCopyOption

class TestTaskExecutionSpec extends UnityIntegrationSpec {

    @Unroll
    def "Test tasks dependencies for :#task"() {

        given: "a build file with testBuildTargets"
        buildFile << """    
        unity.testBuildTargets = $testBuildTargets
        """.stripIndent()

        when:
        def result = runTasksSuccessfully(task)

        then:
        includedTasks.every { result.wasExecuted(it) }
        excludedTasks.every { !result.wasExecuted(it) }

        where:
        task              | testBuildTargets     | includedTasks                                                      | excludedTasks
        "test"            | '["ios", "android"]' | ["test", "testPlayMode", "testPlayModeAndroid", "testPlayModeIos"] | ["check"]
        "testPlayMode"    | '["android"]'        | ["testPlayMode", "testPlayModeAndroid"]                            | ["test", "testPlayModeIos"]
        "testPlayModeIos" | '["ios"]'            | ["testPlayModeIos"]                                                | ["test", "testPlayMode"]
    }

    @Unroll
    def "verify testBuildTargets fallback order with #message"() {
        given: "a build file"
        buildFile << """
        $buildFileTestBuildTargets
        $defaultBuildTarget
        """.stripIndent()

        and: "a properties file"
        createFile("gradle.properties") << """
        $propertyFileTestBuildTargets
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("check", commandlineTestBuildTargets)

        then:
        taskShouldRun.each { String task ->
            assert result.wasExecuted(task)
        }

        where:
        message                                    | buildFileTestBuildTargets                    | propertyFileTestBuildTargets         | commandlineTestBuildTargets         | defaultBuildTarget               | expectedTasksToRun
        "nothing"                                  | ""                                           | ""                                   | "-Pnothing=true"                    | ""                               | []
        "build.gradle"                             | "unity.testBuildTargets = ['ios']"           | ""                                   | "-Pnothing=true"                    | ""                               | ["testPlayModeIos", "testEditModeIos"]
        "multiple build.gradle"                    | "unity.testBuildTargets = ['ios','android']" | ""                                   | "-Pnothing=true"                    | ""                               | ["testPlayModeIos", "testEditModeIos", "testPlayModeAndroid", "testEditModeAndroid"]
        "gradle.properties"                        | ""                                           | "unity.testBuildTargets=webgl"       | "-Pnothing=true"                    | ""                               | ["testPlayModeWebgl", "testEditModeWebgl"]
        "multiple gradle.properties"               | ""                                           | "unity.testBuildTargets=webgl,linux" | "-Pnothing=true"                    | ""                               | ["testPlayModeWebgl", "testEditModeWebgl", "testPlayModeLinux", "testEditModeLinux"]
        "commandline"                              | ""                                           | ""                                   | "-Punity.testBuildTargets=ps4"      | ""                               | ["testPlayModePs4", "testEditModePs4"]
        "multiple commandline"                     | ""                                           | ""                                   | "-Punity.testBuildTargets=ps4,psp2" | ""                               | ["testPlayModePs4", "testEditModePs4", "testPlayModePsp2", "testEditModePsp2"]
        "defaultBuildTarget"                       | ""                                           | ""                                   | "-Pnothing=true"                    | "unity.defaultBuildTarget='web'" | ["testPlayModeWeb", "testEditModeWeb"]
        "build.gradle and gradle.properties"       | "unity.testBuildTargets = ['ios']"           | "unity.testBuildTargets=webgl"       | "-Pnothing=true"                    | ""                               | ["testPlayModeIos", "testEditModeIos"]
        "build.gradle and commandline"             | "unity.testBuildTargets = ['ios']"           | ""                                   | "-Punity.testBuildTargets=ps4,psp2" | ""                               | ["testPlayModeIos", "testEditModeIos"]
        "build.gradle and defaultBuildTarget"      | "unity.testBuildTargets = ['ios']"           | ""                                   | "-Pnothing=true"                    | "unity.defaultBuildTarget='web'" | ["testPlayModeIos", "testEditModeIos"]
        "commandline and defaultBuildTarget"       | ""                                           | ""                                   | "-Punity.testBuildTargets=ps4,psp2" | "unity.defaultBuildTarget='web'" | ["testPlayModePs4", "testEditModePs4", "testPlayModePsp2", "testEditModePsp2"]
        "commandline and gradle.properties "       | ""                                           | "unity.testBuildTargets=webgl,linux" | "-Punity.testBuildTargets=ps4,psp2" | ""                               | ["testPlayModePs4", "testEditModePs4", "testPlayModePsp2", "testEditModePsp2"]
        "gradle.properties and defaultBuildTarget" | ""                                           | "unity.testBuildTargets=webgl,linux" | "-Pnothing=true"                    | "unity.defaultBuildTarget='web'" | ["testPlayModeWebgl", "testEditModeWebgl", "testPlayModeLinux", "testEditModeLinux"]

        taskShouldRun = expectedTasksToRun << "test" << "testPlayMode" << "testEditMode"
    }

    @Unroll
    def "can set unityPath with #method"() {
        given: "a build file with custom test task"
        buildFile << """

        task('customTest', type:wooga.gradle.unity.tasks.Test) {
            $method(file('$value'))
        }
        """.stripIndent()

        and:

        def testUnity = createFile(value)
        Files.copy(unityTestLocation.toPath(), testUnity.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES)

        when:
        def result = runTasksSuccessfully("customTest")

        then:
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            value = value.replace('/', '\\')
        }
        result.standardOutput.contains(value)

        where:
        property    | useSetter | value
        "unityPath" | true      | "custom/unity.bat"
        "unityPath" | false     | "custom/unity.bat"

        method = (useSetter) ? "set${property.capitalize()}" : property
    }

    @Unroll
    def "can set testPlatform with #method and #value"() {
        given: "a build file with custom test task"
        buildFile << """

        task('customTest', type:wooga.gradle.unity.tasks.Test) {
            $method($value)
        }
        """.stripIndent()

        and: "properties file with custom unity version"
        createFile("gradle.properties") << """
        defaultUnityTestVersion=5.6.0
        """.stripIndent()

        and: "a mocked unity project with enabled playmode tests"
        settings.text = ""
        settings << ProjectSettingsSpec.TEMPLATE_CONTENT_ENABLED

        when:
        def result = runTasksSuccessfully("customTest")

        then:
        result.standardOutput.contains("$expectedCommandlineSwitch")

        where:
        property       | useSetter | value                                                | expectedCommandlineSwitch
        "testPlatform" | false     | '"playmode"'                                         | '-testPlatform playmode'
        "testPlatform" | false     | '"editmode"'                                         | '-testPlatform editmode'
        "testPlatform" | true      | '"playmode"'                                         | '-testPlatform playmode'
        "testPlatform" | true      | '"editmode"'                                         | '-testPlatform editmode'
        "testPlatform" | false     | 'wooga.gradle.unity.batchMode.TestPlatform.playmode' | '-testPlatform playmode'
        "testPlatform" | false     | 'wooga.gradle.unity.batchMode.TestPlatform.editmode' | '-testPlatform editmode'
        "testPlatform" | true      | 'wooga.gradle.unity.batchMode.TestPlatform.playmode' | '-testPlatform playmode'
        "testPlatform" | true      | 'wooga.gradle.unity.batchMode.TestPlatform.editmode' | '-testPlatform editmode'
        method = (useSetter) ? "set${property.capitalize()}" : property
    }

    @Unroll
    def "batchMode override for #testPlatform"() {
        given: "a build file with custom test task"
        buildFile << """

        task('customTest', type:wooga.gradle.unity.tasks.Test) {
            setTestPlatform($testPlatform)
        }
        """.stripIndent()

        and: "properties file with custom unity version"
        def properties = createFile("gradle.properties") << """
        defaultUnityTestVersion=2018.4.0
        """.stripIndent()

        if (batchModeForPlayModeTest != _) {
            properties << "unity.batchModeForPlayModeTest=${batchModeForPlayModeTest}\n"
        }
        if (batchModeForEditModeTest != _) {
            properties << "unity.batchModeForEditModeTest=${batchModeForEditModeTest}\n"
        }
        and: "a mocked unity project with enabled playmode tests"
        settings.text = ""
        settings << ProjectSettingsSpec.TEMPLATE_CONTENT_ENABLED

        when:
        def result = runTasksSuccessfully("customTest")

        then:
        result.standardOutput.contains(BatchModeFlags.BATCH_MODE) == containsBatchModeFlag

        where:
        batchModeForPlayModeTest | batchModeForEditModeTest | testPlatform | containsBatchModeFlag
        true                     | true                     | '"playmode"' | true
        _                        | true                     | '"playmode"' | true
        false                    | true                     | '"playmode"' | false
        false                    | false                    | '"playmode"' | false
        true                     | true                     | '"editmode"' | true
        true                     | _                        | '"editmode"' | true
        true                     | false                    | '"editmode"' | false
        false                    | false                    | '"editmode"' | false
    }
}
