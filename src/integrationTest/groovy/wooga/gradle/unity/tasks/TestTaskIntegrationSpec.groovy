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

import com.wooga.gradle.PropertyUtils
import com.wooga.gradle.test.PropertyLocation
import com.wooga.gradle.test.PropertyQueryTaskWriter
import com.wooga.gradle.test.writers.PropertyGetterTaskWriter
import com.wooga.gradle.test.writers.PropertySetterWriter
import com.wooga.spock.extensions.unity.UnityPluginTestOptions
import nebula.test.functional.ExecutionResult
import spock.lang.Shared
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
        ExecutionResult result = unityVersion ?
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

    @Shared
    def mockProjectFiles = [
        [new File("Assets/Plugins.meta"), false],
        [new File("Library/SomeCache.asset"), true],
        [new File("ProjectSettings/SomeSettings.asset"), false],
        [new File("UnityPackageManager/manifest.json"), false],
        [new File("Assets/Plugins/iOS.meta"), true],
        [new File("Assets/Plugins/iOS/somefile.m"), true],
        [new File("Assets/Plugins/iOS/somefile.m.meta"), true],
        [new File("Assets/Nested.meta"), false],
        [new File("Assets/Nested/Plugins.meta"), false],
        [new File("Assets/Nested/Plugins/iOS.meta"), true],
        [new File("Assets/Nested/Plugins/iOS/somefile.m"), true],
        [new File("Assets/Nested/Plugins/iOS/somefile.m.meta"), true],
        [new File("Assets/Plugins/WebGL.meta"), true],
        [new File("Assets/Plugins/WebGL/somefile.ts"), true],
        [new File("Assets/Plugins/WebGL/somefile.ts.meta"), true],
        [new File("Assets/Nested/Plugins/WebGL.meta"), true],
        [new File("Assets/Nested/Plugins/WebGL/somefile.ts"), true],
        [new File("Assets/Nested/Plugins/WebGL/somefile.ts.meta"), true],
        [new File("Assets/Editor.meta"), false],
        [new File("Assets/Editor/somefile.cs"), false],
        [new File("Assets/Editor/somefile.cs.meta"), false],
        [new File("Assets/Nested/Editor/somefile.cs"), false],
        [new File("Assets/Source.cs"), false],
        [new File("Assets/Source.cs.meta"), false],
        [new File("Assets/Nested/LevelEditor.meta"), false],
        [new File("Assets/Nested/LevelEditor/somefile.cs"), false],
        [new File("Assets/Nested/LevelEditor/somefile.cs.meta"), false],
        [new File("Assets/Plugins/Android.meta"), false],
        [new File("Assets/Plugins/Android/somefile.java"), false],
        [new File("Assets/Plugins/Android/somefile.java.meta"), false],
        [new File("Assets/Nested/Plugins/Android.meta"), false],
        [new File("Assets/Nested/Plugins/Android/s.java"), false],
        [new File("Assets/Nested/Plugins/Android/s.java.meta"), false],
    ]

    @Unroll
    def "task #statusMessage up-to-date when #file changed with default inputFiles"() {
        given: "a mocked unity project"
        //need to convert the relative files to absolute files
        def (_, File testFile) = prepareMockedProject(projectDir, files as Iterable<File>, file as File)
        and: "a unity test task"
        buildFile << """
            tasks.register("unityTest", wooga.gradle.unity.tasks.Test) {
                it.buildTarget = "${buildTarget}"
            }
        """.stripIndent()

        and: "a up-to-date project state"
        def result = runTasksSuccessfully("unityTest")
        assert !result.wasUpToDate('unityTest')

        result = runTasksSuccessfully("unityTest")
        assert result.wasUpToDate('unityTest')

        when: "change content of one source file"
        testFile.text = "new content"

        result = runTasksSuccessfully("unityTest")

        then:
        result.wasUpToDate('unityTest') == upToDate

        where:
        files = mockProjectFiles.collect { it[0] }
        [file, upToDate] << mockProjectFiles
        buildTarget = "android"
        statusMessage = upToDate ? "is" : "is not"
    }

    @Unroll
    def "can set custom inputFiles for up-to-date check #type"() {
        given: "a mocked unity project"
        //need to convert the relative files to absolute files
        def (_, File testFile) = prepareMockedProject(projectDir, files as Iterable<File>, file as File)

        and: "a custom inputCollection"
        buildFile << """
            tasks.register("unityTest", wooga.gradle.unity.tasks.Test) {
                it.inputFiles.setFrom(${value})
            }
        """.stripIndent()

        and: "a up-to-date project state"
        def result = runTasksSuccessfully("unityTest")
        assert !result.wasUpToDate('unityTest')

        result = runTasksSuccessfully("unityTest")
        assert result.wasUpToDate('unityTest')

        when: "change content of one source file"
        testFile.text = "new content"

        result = runTasksSuccessfully("unityTest")

        then:
        result.wasUpToDate('unityTest') == upToDate

        where:
        file                                          | upToDate | type             | value
        new File("Assets/Plugins/iOS/somefile.m")     | true     | 'FileTree'       | 'project.fileTree(project.projectDir){include("Assets/**"); exclude("**/Plugins/iOS/**")}'
        new File("Assets/Plugins/Android/somefile.m") | false    | 'FileTree'       | 'project.fileTree(project.projectDir){include("Assets/**"); exclude("**/Plugins/iOS/**")}'
        new File("Assets/Source.cs")                  | false    | 'FileTree'       | 'project.fileTree(project.projectDir){include("Assets/**"); exclude("**/Plugins/iOS/**")}'
        new File("Assets/Plugins/iOS/somefile.m")     | false    | 'FileTree'       | 'project.fileTree(project.projectDir){include("Assets/**"); exclude("**/Plugins/Android/**")}'
        new File("Assets/Plugins/Android/somefile.m") | true     | 'FileTree'       | 'project.fileTree(project.projectDir){include("Assets/**"); exclude("**/Plugins/Android/**")}'
        new File("Assets/Source.cs")                  | false    | 'FileTree'       | 'project.fileTree(project.projectDir){include("Assets/**"); exclude("**/Plugins/Android/**")}'
        new File("Assets/Editor/somefile.cs")         | true     | 'FileCollection' | 'project.files("Assets/Editor/anyfile.cs","Assets/Source.cs")'
        new File("Assets/Source.cs")                  | false    | 'FileCollection' | 'project.files("Assets/Editor/anyfile.cs","Assets/Source.cs")'

        files = mockProjectFiles.collect { it[0] }
        statusMessage = (upToDate) ? "is" : "is not"
    }

    Tuple prepareMockedProject(File projectDir, Iterable<File> files, File testFile) {
        files = files.collect { new File(projectDir, it.path) }
        testFile = new File(projectDir, testFile.path)

        //create directory structure
        files.each { f ->
            f.parentFile.mkdirs()
            f.text = "some content"
        }
        new Tuple(files, testFile)
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

    @Unroll
    def "can set command line option #option from #location"() {

        given:
        switch (location) {
            case PropertyLocation.property:
                createFile("gradle.properties") << "${propertyKey}=${value}"
                break

            case PropertyLocation.environment:
                environmentVariables.set(envKey, value)
                break
        }

        when:
        def queryWriter = new PropertyQueryTaskWriter("${subjectUnderTestName}.${option}")
        queryWriter.write(buildFile)
        def result = runTasksSuccessfully(subjectUnderTestName, queryWriter.taskName)


        then:
        result.success
        queryWriter.matches(result, value)


        where:
        option       | propertyKey        | envKey              | value            | type   | location
        "testFilter" | "unity.testFilter" | "UNITY_TEST_FILTER" | "Wooga.Pancakes" | String | PropertyLocation.property
        "testFilter" | "unity.testFilter" | "UNITY_TEST_FILTER" | "Wooga.Pancakes" | String | PropertyLocation.environment
    }
}
