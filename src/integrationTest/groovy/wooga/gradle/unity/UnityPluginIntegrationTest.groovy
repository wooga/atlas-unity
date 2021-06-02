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
import wooga.gradle.unity.models.BuildTarget
import wooga.gradle.unity.models.UnityCommandLineOption
import wooga.gradle.unity.tasks.Test
import wooga.gradle.unity.utils.ProjectSettingsFile

/**
 * Tests the {@link UnityPluginExtension} when applied on the {@link UnityPlugin}
 */
class UnityPluginIntegrationTest extends UnityIntegrationTest {

    @Unroll
    @UnityPluginTestOptions(addPluginTestDefaults = false)
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

    @UnityPluginTestOptions(addMockTask = false)
    @Unroll("can set batchMode for #testPlatform to #useBatchMode in #locationMessage")
    def "batchMode override for #testPlatform"() {
        given: "a build file with custom test task"
        def propertiesFile = createFile("gradle.properties") << """
        defaultUnityTestVersion=2018.4.0
        """.stripIndent()

        switch (location) {
            case PropertyLocation.property:
                propertiesFile << "${propertiesKey}=${useBatchMode}"
                break

            case PropertyLocation.environment:
                def envKey = convertPropertyToEnvName(extensionKey)
                environmentVariables.set(envKey, useBatchMode.toString())
                break

            case PropertyLocation.script:
                buildFile << "\n${extensionKey} = ${useBatchMode}"
                break
        }
        addTask("Custom", Test.class.name, false, "testPlatform = \"${testPlatform}\"")

        and: "a mocked unity project with enabled playmode tests"
        setProjectSettingsFile(ProjectSettingsFile.TEMPLATE_CONTENT_ENABLED)

        when:
        def result = runTasksSuccessfully("Custom")

        then:
        result.standardOutput.contains(UnityCommandLineOption.batchMode.flag) == expectBatchModeFlag

        where:
        testPlatform | useBatchMode | location                     | expectBatchModeFlag
        "playmode"   | true         | PropertyLocation.environment | true
        "playmode"   | false        | PropertyLocation.environment | false
        "playmode"   | true         | PropertyLocation.property    | true
        "playmode"   | false        | PropertyLocation.property    | false
        "playmode"   | true         | PropertyLocation.script      | true
        "playmode"   | false        | PropertyLocation.script      | false
        "playmode"   | _            | _                            | true
        "editmode"   | true         | PropertyLocation.environment | true
        "editmode"   | false        | PropertyLocation.environment | false
        "editmode"   | true         | PropertyLocation.property    | true
        "editmode"   | false        | PropertyLocation.property    | false
        "editmode"   | true         | PropertyLocation.script      | true
        "editmode"   | false        | PropertyLocation.script      | false
        "editmode"   | _            | _                            | true

        property = testPlatform == "playmode" ? "batchModeForPlayModeTest" : "batchModeForEditModeTest"
        extensionKey = "unity.$property"
        propertiesKey = "unity.$property"
        locationMessage = "${location.toString()}"
    }

    @UnityPluginTestOptions(unityPath = UnityPathResolution.None, addPluginTestDefaults = false,
            disableAutoActivateAndLicense = false)
    @Unroll()
    def "extension property :#property returns '#testValue' if #reason"() {
        given:
        setupUnityPlugin()
        addProviderQueryTask("custom", "${extensionName}.${property}")

        and: "a set value"
        switch (location) {
            case PropertyLocation.script:
                buildFile << "${extensionName}.${invocation}"
                break
            case PropertyLocation.property:
                def propertiesFile = createFile("gradle.properties")
                propertiesFile << "${extensionName}.${property} = ${escapedValue}"
                break
            case PropertyLocation.environment:
                def envPropertyKey = envNameFromProperty(extensionName, property)
                environmentVariables.set(envPropertyKey, value)
                break
            default:
                break
        }

        and: "the test value with replace placeholders"
        if (testValue instanceof String) {
            testValue = testValue.replaceAll("#projectDir#", escapedPath(projectDir.path))
        }

        and: "an applied plugin"
        setupUnityPlugin()

        when: ""
        def result = runTasksSuccessfully("custom")

        then:
        result.standardOutput.contains("${extensionName}.${property}: ${testValue}")

        where:
        property                   | method          | rawValue                  | expectedValue                                              | type                    | location
        "unityPath"                | _               | _                         | UnityPluginConventions.getPlatformUnityPath().absolutePath | "Provider<RegularFile>" | PropertyLocation.none
        "unityPath"                | _               | osPath("/foo/bar/unity1") | _                                                          | _                       | PropertyLocation.environment
        "unityPath"                | _               | osPath("/foo/bar/unity2") | _                                                          | _                       | PropertyLocation.property
        "unityPath"                | "setUnityPath"  | osPath("/foo/bar/unity3") | _                                                          | "Provider<RegularFile>" | PropertyLocation.script
        "unityPath"                | "unityPath.set" | osPath("/foo/bar/unity4") | _                                                          | "Provider<RegularFile>" | PropertyLocation.script

        "defaultBuildTarget"       | _               | _                         | BuildTarget.undefined                                      | BuildTarget             | PropertyLocation.none
        "autoActivateUnity"        | _               | _                         | true                                                       | Boolean                 | PropertyLocation.none
        "autoReturnLicense"        | _               | _                         | true                                                       | Boolean                 | PropertyLocation.none
        "logCategory"              | _               | _                         | "unity"                                                    | "Property<String>"      | PropertyLocation.none
        "batchModeForEditModeTest" | _               | _                         | true                                                       | Boolean                 | PropertyLocation.none
        "batchModeForPlayModeTest" | _               | _                         | true                                                       | Boolean                 | PropertyLocation.none

        "assetsDir"                | _               | _                         | osPath("#projectDir#/Assets")                              | "Provider<Directory>"   | PropertyLocation.none
        "logsDir"                  | _               | _                         | osPath("#projectDir#/build/logs")                          | "Provider<Directory>"   | PropertyLocation.none
        "reportsDir"               | _               | _                         | osPath("#projectDir#/build/reports")                       | "Provider<Directory>"   | PropertyLocation.none

        value = (type != _) ? wrapValueBasedOnType(rawValue, type) : rawValue
        providedValue = (location == PropertyLocation.script) ? type : value
        testValue = (expectedValue == _) ? rawValue : expectedValue
        reason = location.reason() + ((location == PropertyLocation.none) ? "" : "  with '$providedValue' ")
        escapedValue = (value instanceof String) ? escapedPath(value) : value
        invocation = (method != _) ? "${method}(${escapedValue})" : "${property} = ${escapedValue}"
    }

    @Unroll("sets buildTarget with #taskConfig #useOverride")
    def "sets defaultBuildTarget for all tasks"() {
        given: "a build script"
        appendToPluginExtension("defaultBuildTarget = \"android\"")
        appendToTestTask(taskConfig)

        when:
        def result = runTasks(testTaskName)

        then:
        result.standardOutput.contains(expected)

        where:
        taskConfig            | expected
        'buildTarget = "ios"' | "-buildTarget ios"
        ''                    | "-buildTarget android"

        useOverride = taskConfig != '' ? "use override" : "fallback to default"
    }

    @Unroll
    def "plugin sets default #property"() {
        given: "a build script"

        buildFile << """
            task(customTest) {
                doLast {
                    print "$property: "
                    println unity.${property}.getOrNull()
                }
            }
        """

        and: "a path to the project"
        def path = new File(projectDir, expectedPath)

        when:
        def result = runTasks("customTest")

        then:
        result.standardOutput.contains("$property: ${path.path}")

        where:
        property     | expectedPath
        'assetsDir'  | "Assets"
        'pluginsDir' | "Assets/Plugins"
        'logsDir'    | "build/logs"
    }

    void appendPropertyPrint(String property, String root) {
        buildFile << """
            task(customTest) {
                doLast {
                    print "$property: "
                    println ${root}${property}.getOrNull()
                }
            }
        """
    }

    @UnityPluginTestOptions(forceMockTaskRun = false, disableAutoActivateAndLicense = false)
    def "skips activateUnity and returnUnityLicense when autoActivate is false"() {
        given: "a build script with fake test unity location"
        buildFile << """
            unity {
                authentication.username = "test@test.test"
                authentication.password = "testtesttest"
                authentication.serial = "abcdefg"                
                autoActivateUnity = false
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully(testTaskName)

        then:
        result.wasSkipped("activateUnity")
        result.wasExecuted(testTaskName)
        result.wasSkipped("returnUnityLicense")
    }

    @UnityPluginTestOptions(forceMockTaskRun = false, disableAutoActivateAndLicense = false)
    def "skips returnUnityLicense when autoReturnLicense is false"() {
        given: "a build script with fake test unity location"
        buildFile << """
            unity {
                authentication.username = "test@test.test"
                authentication.password = "testtesttest"
                authentication.serial = "abcdefg"                
                autoReturnLicense = false
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully(testTaskName)

        then:
        result.wasExecuted("activateUnity")
        result.wasExecuted(testTaskName)
        result.wasSkipped("returnUnityLicense")
    }

    @UnityPluginTestOptions(forceMockTaskRun = false, disableAutoActivateAndLicense = false)
    def "runs activation before a unity task when authentication is set once"() {
        given: "a build script with fake test unity location"
        buildFile << """
            unity {
                authentication.username = "test@test.test"
                authentication.password = "testtesttest"
                authentication.serial = "abcdefg"                
            }
            
            task (mUnity, type: wooga.gradle.unity.tasks.Test)
            task (mUnity2, type: wooga.gradle.unity.tasks.Test)            
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("test", "mUnity", "mUnity2")

        then:
        !result.wasSkipped("test")
        !result.wasSkipped("mUnity")
        !result.wasSkipped("mUnity2")
        result.wasExecuted("activateUnity")
    }

    @UnityPluginTestOptions(forceMockTaskRun = false, disableAutoActivateAndLicense = false)
    def "skips activateUnity and returnUnityLicense when authentication is not set"() {
        when:
        def result = runTasksSuccessfully(testTaskName)

        then:
        result.wasSkipped("activateUnity")
        result.wasSkipped("returnUnityLicense")
    }

    @UnityPluginTestOptions(forceMockTaskRun = false, disableAutoActivateAndLicense = false)
    def "run activateUnity from cli"() {
        when:
        def result = runTasksSuccessfully("activateUnity")

        then:
        result.wasSkipped("returnUnityLicense")
        result.wasExecuted("activateUnity")
    }
}
