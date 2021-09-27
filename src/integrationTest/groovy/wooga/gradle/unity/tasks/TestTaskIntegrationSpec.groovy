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

package wooga.gradle.unity.tasks

import com.wooga.spock.extensions.unity.UnityPluginTestOptions
import kotlin.Unit
import nebula.test.functional.ExecutionResult
import spock.lang.Unroll
import wooga.gradle.unity.UnityPlugin
import wooga.gradle.unity.UnityTaskIntegrationSpec
import wooga.gradle.unity.models.UnityCommandLineOption
import wooga.gradle.unity.testutils.GradleRunResult
import wooga.gradle.unity.utils.ProjectSettingsFile

class TestTaskIntegrationSpec extends UnityTaskIntegrationSpec<Test> {

    def "calls unity test mode"() {
        given: "a build script with fake test unity location"

        when:
        def result = runTasksSuccessfully("test")

        then:
        !result.wasSkipped("test")
    }

    def "can set reports location via reports extension in task"() {
        given: "destination path"
        def destination = "out/reports/test.xml"

        and: "a build script with fake test unity location"
        buildFile << """
            ${subjectUnderTestName} {
                reports.xml.destination = file("$destination")
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully(subjectUnderTestName)

        then:
        result.standardOutput.contains(new File(destination).path)
    }

    @Unroll
    def "can set testPlatform with #method and #value"() {
        given: "a build file with custom test task"
        appendToSubjectTask("$method($value)")

        and: "properties file with custom unity version"
        createFile("gradle.properties") << """
        defaultUnityTestVersion=5.6.0
        """.stripIndent()

        and: "a mocked unity project with enabled playmode tests"
        setProjectSettingsFile(ProjectSettingsFile.TEMPLATE_CONTENT_ENABLED)

        when:
        def result = runTestTaskSuccessfully()

        then:
        result.standardOutput.contains("$expectedCommandlineSwitch")

        where:
        property       | useSetter | value                                             | expectedCommandlineSwitch
        "testPlatform" | false     | '"playmode"'                                      | '-testPlatform playmode'
        "testPlatform" | false     | '"editmode"'                                      | '-testPlatform editmode'
        "testPlatform" | true      | '"playmode"'                                      | '-testPlatform playmode'
        "testPlatform" | true      | '"editmode"'                                      | '-testPlatform editmode'
        "testPlatform" | false     | 'wooga.gradle.unity.models.TestPlatform.playmode' | '-testPlatform playmode'
        "testPlatform" | false     | 'wooga.gradle.unity.models.TestPlatform.editmode' | '-testPlatform editmode'
        "testPlatform" | true      | 'wooga.gradle.unity.models.TestPlatform.playmode' | '-testPlatform playmode'
        "testPlatform" | true      | 'wooga.gradle.unity.models.TestPlatform.editmode' | '-testPlatform editmode'
        method = (useSetter) ? "set${property.capitalize()}" : property
    }

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

    // How to handle this use case?
    def "can set reports destination via reports extension in plugin"() {
        given: "destination path"
        def destination = "out/reports"
        and: "a build script with fake test unity location"
        buildFile << """
            unity.reportsDir = "$destination"
        """.stripIndent()

        when:
        def result = runTasksSuccessfully(subjectUnderTestName)

        then:
        result.standardOutput.contains(UnityCommandLineOption.runTests.toString())
        def expectedTestResultsPath = new File(destination + "/${subjectUnderTestName}/${subjectUnderTestName}.xml").path
        result.standardOutput.contains(expectedTestResultsPath)
    }

    def "can disable reports via reports extension in task"() {
        given: "a build script with fake test unity location"
        buildFile << """
             ${subjectUnderTestName} {
                reports.xml.enabled = false
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully(subjectUnderTestName)

        then:
        result.standardOutput.contains(UnityCommandLineOption.runTests.toString())
        !result.standardOutput.contains(UnityCommandLineOption.testResults.toString())
    }

    def "has default reports location"() {
        given: "a build script with fake test unity location"

        when:
        def result = runTasksSuccessfully(subjectUnderTestName)

        then:
        result.standardOutput.contains(UnityCommandLineOption.runTests.toString())
        result.standardOutput.contains(new File("reports/unity/${subjectUnderTestName}/${subjectUnderTestName}.xml").path)
    }
//
//    @IgnoreIf({ os.windows })
//    def "can set testPlatform to playMode"() {
//        given: "a build script with fake test unity location"
//        buildFile << """
//            task (mUnity, type: wooga.gradle.unity.tasks.Test) {
//                testPlatform = "playmode"
//            }
//        """.stripIndent()
//
//        and: "a mocked project setting"
//        def settings = createFile("ProjectSettings/ProjectSettings.asset")
//        settings << """
//        PlayerSettings:
//            wiiUDrcBufferDisabled: 0
//            wiiUProfilerLibPath:
//            playModeTestRunnerEnabled: 0
//            actionOnDotNetUnhandledException: 1
//
//        """.stripIndent()
//
//        and: "unity version > 5.5"
//
//        when:
//        def result = runTasksSuccessfully("mUnity", "-PdefaultUnityTestVersion=2017.1.1f3")
//
//        then:
//        result.standardOutput.contains("PlayMode tests not activated")
//    }
//
    def "Auto-detect for playmode tests with binary settings file"() {

        given: "a build script with fake test unity location"
        buildFile << """
            ${subjectUnderTestName} {
                testPlatform = "playmode"
            }
        """.stripIndent()

        and: "a mocked project setting with binary content"
        def settings = createFile("ProjectSettings/ProjectSettings.asset")
        settings.bytes = [0, 1, 0, 1, 1, 0, 1, 0] as byte[]
        and: "unity version > 5.5"

        when:
        def result = runTasksSuccessfully(subjectUnderTestName, "-PdefaultUnityTestVersion=2017.1.1f3")

        then:
        result.standardOutput.contains("PlayMode tests not activated")

    }

    @Unroll("#setupMsg coverage arguments for unity version #unityVersion if enableTestCodeCoverage is #enableTestCodeCoverage")
    @UnityPluginTestOptions(addMockTask = false, disableAutoActivateAndLicense = false)
    def "sets up coverage arguments if code coverage is enabled"() {
        given: "a build script with fake test unity location"
        and: "enabled test code coverage on extension"
        buildFile << """
        ${UnityPlugin.EXTENSION_NAME} {
            enableTestCodeCoverage = ${enableTestCodeCoverage}
        }
        """

        and: "a mocked unity project with enabled playmode tests"
        setProjectSettingsFile(ProjectSettingsFile.TEMPLATE_CONTENT_ENABLED)

        when:
        ExecutionResult result = unityVersion?
                runTasksSuccessfully("test", "-PdefaultUnityTestVersion=${unityVersion}") :
                runTasksSuccessfully("test")

        then:
        def playModeResult = new GradleRunResult(":testPlayModeAndroid", result.standardOutput)
        setsUpCoverage ? matchesExpectedCoverageArgs(playModeResult) : !matchesExpectedCoverageArgs(playModeResult)
        def editModeResult = new GradleRunResult(":testEditModeAndroid", result.standardOutput)
        setsUpCoverage ? matchesExpectedCoverageArgs(editModeResult) : !matchesExpectedCoverageArgs(editModeResult)

        where:
        enableTestCodeCoverage | unityVersion | setsUpCoverage | setupMsg
        true                   | "2017.1.1f3" | false          | "doesn't sets up"
        true                   | "2018.2.1f3" | false          | "doesn't sets up"
        false                  | "2018.2.1f3" | false          | "doesn't sets up"
        true                   | "2019.1.1f3" | true           | "sets up"
        false                  | "2019.1.1f3" | false          | "doesn't sets up"
        true                   | "2020.5.1f3" | true           | "sets up"
        false                  | "2020.5.1f3" | false          | "doesn't sets up"
        false                  | null         | false          | "doesn't sets up"
        true                   | null         | true           | "sets up"

    }


    @UnityPluginTestOptions(addMockTask = false, disableAutoActivateAndLicense = false)
    def "doesnt sets up coverage arguments if code coverage is disabled"() {
        given: "a build script with fake test unity location"
        and: "enabled test code coverage on extension"
        buildFile << """
        ${UnityPlugin.EXTENSION_NAME} {
            enableTestCodeCoverage.set(false)
        }
        """
        
        and: "a mocked unity project with enabled playmode tests"
        setProjectSettingsFile(ProjectSettingsFile.TEMPLATE_CONTENT_ENABLED)

        when:
        def result = runTasksSuccessfully("test")

        then:
        def playModeResult = new GradleRunResult(":testPlayModeAndroid", result.standardOutput)
        !playModeResult.args.contains("-enableCodeCoverage")
        def editModeResult = new GradleRunResult(":testEditModeAndroid", result.standardOutput)
        !editModeResult.args.contains("-enableCodeCoverage")
    }

    boolean matchesExpectedCoverageArgs(GradleRunResult taskResult) {
        return taskResult.args.contains("-enableCodeCoverage") &&
                taskResult.args.contains("-coverageResultsPath") &&
                taskResult.argValueMatches("-coverageResultsPath") { String value ->
                    new File(value) == new File(projectDir, "build/reports/unity")
                } &&
                taskResult.args.contains("-coverageOptions") &&
                taskResult.argValueMatches("-coverageOptions") { it == "generateAdditionalMetrics" } &&
                taskResult.args.contains("-debugCodeOptimization")

    }

}
