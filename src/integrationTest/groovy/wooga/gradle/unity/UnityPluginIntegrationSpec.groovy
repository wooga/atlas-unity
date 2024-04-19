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


import com.wooga.gradle.PropertyUtils
import com.wooga.gradle.test.PropertyLocation
import com.wooga.gradle.test.PropertyQueryTaskWriter
import com.wooga.gradle.test.serializers.PropertyTypeSerializer
import com.wooga.gradle.test.writers.PropertyGetterTaskWriter
import com.wooga.gradle.test.writers.PropertySetInvocation
import com.wooga.gradle.test.writers.PropertySetterWriter
import com.wooga.spock.extensions.unity.UnityPathResolution
import com.wooga.spock.extensions.unity.UnityPluginTestOptions
import spock.lang.Unroll
import wooga.gradle.unity.models.ResolutionStrategy
import wooga.gradle.unity.models.UnityCommandLineOption
import wooga.gradle.unity.models.UnityProjectManifest
import wooga.gradle.unity.tasks.Test
import wooga.gradle.unity.utils.ProjectSettingsFile

import java.time.Duration
import java.util.regex.Pattern

import static com.wooga.gradle.test.writers.PropertySetInvocation.getAssignment
import static com.wooga.gradle.test.writers.PropertySetInvocation.getNone
import static com.wooga.gradle.test.writers.PropertySetInvocation.getProviderSet
import static com.wooga.gradle.test.writers.PropertySetInvocation.getSetter
import static wooga.gradle.unity.UnityPluginConventions.getUnityFileTree
import static wooga.gradle.unity.UnityPluginConventions.getPlatformUnityPath

/**
 * Tests the {@link UnityPluginExtension} when applied on the {@link UnityPlugin}
 */
class UnityPluginIntegrationSpec extends UnityIntegrationSpec {

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
                def envKey = PropertyUtils.envNameFromProperty(extensionKey)
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
        def taskStdOut = result.standardOutput.substring(result.standardOutput.indexOf("Task :Custom"))
        taskStdOut.contains(UnityCommandLineOption.batchMode.flag) == expectBatchModeFlag

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
    @Unroll
    def "property unity.#property is set on #target with #inputType as #rawValue"() {
        given: "an applied plugin"
        setupUnityPlugin()

        when:
        set.location = target == none ? PropertyLocation.none : set.location

        def propertyQuery = runPropertyQuery(get, set)
                .withSerializer(Long, longGetSerializer)
                .withSerializer(Duration, durationGetSerializer)
                .withSerializer("Provider<Duration>", durationGetSerializer)
                .withSerializer("List<Pattern>", patternsGetSerializer)
                .withSerializer("Provider<List<Pattern>>", patternsGetSerializer)
                .withSerializer("List<String>", stringsGetSerializer)
                .withSerializer("Provider<List<String>>", stringsGetSerializer)

        then:
        propertyQuery.matches(rawValue)

        where:
        property                   | target        | rawValue                                                        | inputType                 | outputType
        "unityPath"                | none          | getPlatformUnityPath().absolutePath                             | "Provider<RegularFile>"   | _
        "unityPath"                | setter        | osPath("/foo/bar/unity1")                                       | "Provider<RegularFile>"   | _
        "unityPath"                | setter        | osPath("/foo/bar/unity1")                                       | "RegularFile"             | _
        "unityPath"                | assignment    | osPath("/foo/bar/unity2")                                       | "Provider<RegularFile>"   | _
        "unityPath"                | assignment    | osPath("/foo/bar/unity2")                                       | "RegularFile"             | _
        "unityPath"                | providerSet   | osPath("/foo/bar/unity3")                                       | "Provider<RegularFile>"   | _
        "unityPath"                | providerSet   | osPath("/foo/bar/unity3")                                       | "RegularFile"             | _
        "unityPath"                | "environment" | osPath("/foo/bar/unity4")                                       | "RegularFile"             | _

        "unityRootDir"             | none          | getUnityFileTree(getPlatformUnityPath()).unityRoot.absolutePath | "Provider<Directory>"     | _

        "defaultBuildTarget"       | none          | null                                                            | _                         | _
        "defaultBuildTarget"       | setter        | "buildTarget"                                                   | String                    | _
        "defaultBuildTarget"       | setter        | "buildTarget"                                                   | "Provider<String>"        | _
        "defaultBuildTarget"       | assignment    | "buildTarget"                                                   | String                    | _
        "defaultBuildTarget"       | assignment    | "buildTarget"                                                   | "Provider<String>"        | _
        "defaultBuildTarget"       | providerSet   | "buildTarget"                                                   | String                    | _
        "defaultBuildTarget"       | providerSet   | "buildTarget"                                                   | "Provider<String>"        | _

        "autoActivateUnity"        | none          | true                                                            | Boolean                   | _
        "autoActivateUnity"        | setter        | false                                                           | Boolean                   | _
        "autoActivateUnity"        | setter        | false                                                           | "Provider<Boolean>"       | _
        "autoActivateUnity"        | assignment    | false                                                           | Boolean                   | _
        "autoActivateUnity"        | assignment    | false                                                           | "Provider<Boolean>"       | _
        "autoActivateUnity"        | providerSet   | false                                                           | Boolean                   | _
        "autoActivateUnity"        | providerSet   | false                                                           | "Provider<Boolean>"       | _

        "autoActivate"             | none          | true                                                            | Boolean                   | _
        "autoActivate"             | setter        | false                                                           | Boolean                   | _
        "autoActivate"             | setter        | false                                                           | "Provider<Boolean>"       | _
        "autoActivate"             | assignment    | false                                                           | Boolean                   | _
        "autoActivate"             | assignment    | false                                                           | "Provider<Boolean>"       | _
        "autoActivate"             | providerSet   | false                                                           | Boolean                   | _
        "autoActivate"             | providerSet   | false                                                           | "Provider<Boolean>"       | _
        "autoActivate"             | "environment" | false                                                           | Boolean                   | _
        "autoActivate"             | "property"    | false                                                           | Boolean                   | _

        "autoReturnLicense"        | none          | true                                                            | Boolean                   | _
        "autoReturnLicense"        | setter        | false                                                           | Boolean                   | _
        "autoReturnLicense"        | setter        | false                                                           | "Provider<Boolean>"       | _
        "autoReturnLicense"        | assignment    | false                                                           | Boolean                   | _
        "autoReturnLicense"        | assignment    | false                                                           | "Provider<Boolean>"       | _
        "autoReturnLicense"        | providerSet   | false                                                           | Boolean                   | _
        "autoReturnLicense"        | providerSet   | false                                                           | "Provider<Boolean>"       | _

        "logCategory"              | none          | "unity"                                                         | String                    | _
        "logCategory"              | setter        | "log"                                                           | String                    | _
        "logCategory"              | setter        | "log"                                                           | "Provider<String>"        | _
        "logCategory"              | assignment    | "log"                                                           | String                    | _
        "logCategory"              | assignment    | "log"                                                           | "Provider<String>"        | _
        "logCategory"              | providerSet   | "log"                                                           | String                    | _
        "logCategory"              | providerSet   | "log"                                                           | "Provider<String>"        | _

        "batchModeForEditModeTest" | none          | true                                                            | Boolean                   | _
        "batchModeForEditModeTest" | setter        | false                                                           | Boolean                   | _
        "batchModeForEditModeTest" | setter        | false                                                           | "Provider<Boolean>"       | _
        "batchModeForEditModeTest" | assignment    | false                                                           | Boolean                   | _
        "batchModeForEditModeTest" | assignment    | false                                                           | "Provider<Boolean>"       | _
        "batchModeForEditModeTest" | providerSet   | false                                                           | Boolean                   | _
        "batchModeForEditModeTest" | providerSet   | false                                                           | "Provider<Boolean>"       | _

        "batchModeForPlayModeTest" | none          | true                                                            | Boolean                   | _
        "batchModeForPlayModeTest" | setter        | false                                                           | Boolean                   | _
        "batchModeForPlayModeTest" | setter        | false                                                           | "Provider<Boolean>"       | _
        "batchModeForPlayModeTest" | assignment    | false                                                           | Boolean                   | _
        "batchModeForPlayModeTest" | assignment    | false                                                           | "Provider<Boolean>"       | _
        "batchModeForPlayModeTest" | providerSet   | false                                                           | Boolean                   | _
        "batchModeForPlayModeTest" | providerSet   | false                                                           | "Provider<Boolean>"       | _

        "enableTestCodeCoverage"   | none          | false                                                           | Boolean                   | _
        "enableTestCodeCoverage"   | setter        | true                                                            | Boolean                   | _
        "enableTestCodeCoverage"   | setter        | true                                                            | "Provider<Boolean>"       | _
        "enableTestCodeCoverage"   | assignment    | true                                                            | Boolean                   | _
        "enableTestCodeCoverage"   | assignment    | true                                                            | "Provider<Boolean>"       | _
        "enableTestCodeCoverage"   | providerSet   | true                                                            | Boolean                   | _
        "enableTestCodeCoverage"   | providerSet   | true                                                            | "Provider<Boolean>"       | _

        "upmPackages"              | none          | [:]                                                             | Map                       | _
        "upmPackages"              | setter        | ["unity.package": "ver"]                                        | Map                       | _
        "upmPackages"              | setter        | ["unity.package": "ver"]                                        | "Provider<Map>"           | _
        "upmPackages"              | assignment    | ["unity.package": "ver"]                                        | Map                       | _
        "upmPackages"              | assignment    | ["unity.package": "ver"]                                        | "Provider<Map>"           | _
        "upmPackages"              | providerSet   | ["unity.package": "ver"]                                        | Map                       | _
        "upmPackages"              | providerSet   | ["unity.package": "ver"]                                        | "Provider<Map>"           | _

        "maxRetries"               | none          | 3                                                               | Integer                   | _
        "maxRetries"               | setter        | 5                                                               | Integer                   | _
        "maxRetries"               | setter        | 5                                                               | "Provider<Integer>"       | _
        "maxRetries"               | assignment    | 6                                                               | Integer                   | _
        "maxRetries"               | assignment    | 6                                                               | "Provider<Integer>"       | _
        "maxRetries"               | providerSet   | 6                                                               | Integer                   | _
        "maxRetries"               | providerSet   | 6                                                               | "Provider<Integer>"       | _
        "maxRetries"               | "environment" | 7                                                               | Integer                   | _
        "maxRetries"               | "property"    | 7                                                               | Integer                   | _

        "retryWait"                | none          | 30000                                                           | Duration                  | _
        "retryWait"                | setter        | 500                                                             | "Provider<Duration>"      | _
        "retryWait"                | setter        | 500                                                             | Duration                  | _
        "retryWait"                | setter        | 500                                                             | Long                      | "Duration"
        "retryWait"                | assignment    | 500                                                             | Duration                  | _
        "retryWait"                | assignment    | 500                                                             | "Provider<Duration>"      | _
        "retryWait"                | assignment    | 500                                                             | Long                      | "Duration"
        "retryWait"                | providerSet   | 500                                                             | "Provider<Duration>"      | _
        "retryWait"                | providerSet   | 500                                                             | Duration                  | _
        "retryWait"                | "environment" | 500                                                             | Long                      | "Duration"
        "retryWait"                | "property"    | 500                                                             | Long                      | "Duration"

        "retryRegexes"             | none          | [/^\s*Pro License:\s*NO$/]                                      | "List<Pattern>"           | _
        "retryRegexes"             | setter        | [/^\s*Pro License:\s*TEST$/]                                    | "Provider<List<Pattern>>" | _
        "retryRegexes"             | setter        | [/^\s*Pro License:\s*TEST$/]                                    | "List<Pattern>"           | _
        "retryRegexes"             | setter        | ["^\\s*Pro License:\\s*TEST\$"]                                 | "List<String_>"           | "List<Pattern>"
        "retryRegexes"             | setter        | [/^\s*Pro License:\s*TEST$/]                                    | "Provider<List<String_>>" | "List<Pattern>"
        "retryRegexes"             | assignment    | [/^\s*Pro License:\s*TEST$/]                                    | "List<Pattern>"           | _
        "retryRegexes"             | assignment    | [/^\s*Pro License:\s*TEST$/]                                    | "Provider<List<Pattern>>" | _
        "retryRegexes"             | assignment    | [/^\s*Pro License:\s*TEST$/]                                    | "List<String_>"           | "List<Pattern>"
        "retryRegexes"             | assignment    | [/^\s*Pro License:\s*TEST$/]                                    | "Provider<List<String_>>" | "List<Pattern>"
        "retryRegexes"             | providerSet   | [/^\s*Pro License:\s*TEST$/]                                    | "Provider<List<Pattern>>" | _
        "retryRegexes"             | providerSet   | [/^\s*Pro License:\s*TEST$/]                                    | "List<Pattern>"           | _


        durationWriteSerializer = { value -> "java.time.Duration.ofMillis(${Long.valueOf(value)})" }
        patternWriteSerializer = { value -> "java.util.regex.Pattern.compile(/$value/)" }
        stringWriteSerializer = { value ->
            property == "retryRegexes" ? "/$value/" : wrapValue(value, String)
        }
        durationGetSerializer = { value -> Duration.ofMillis(Long.valueOf(value)).toString() }
        patternsGetSerializer = { List value ->
            "[" + value.collect { it -> Pattern.compile(it.toString()).pattern() }.join(", ") + "]"
        }
        longGetSerializer = { value -> outputType == "Duration" ? durationGetSerializer(value) : value }
        stringsGetSerializer = { value -> outputType == "List<Pattern>" ? patternsGetSerializer(value) : value }

        location = target instanceof String ? PropertyLocation.valueOf(target) as PropertyLocation : PropertyLocation.script
        scriptInvocation = target instanceof PropertySetInvocation ? target : null

        set = new PropertySetterWriter(extensionName, property).with {
            it.set(rawValue, inputType)
            it.serialize([
                    new PropertyTypeSerializer("Duration", durationWriteSerializer),
                    new PropertyTypeSerializer("Pattern", patternWriteSerializer),
                    //needed to differentiate from the normal string type, because the
                    // current string serializer (wrapValue) breaks regexes for some reason
                    new PropertyTypeSerializer("String_", stringWriteSerializer)
            ], location)
            return scriptInvocation ? it.toScript(scriptInvocation) : it.to(location)
        }
        get = new PropertyGetterTaskWriter(set)
    }

    @Unroll("sets buildTarget with #taskConfig #useOverride")
    def "sets defaultBuildTarget for all tasks"() {
        given: "a build script"
        appendToPluginExtension("defaultBuildTarget = \"android\"")
        appendToSubjectTask(taskConfig)

        when:
        def result = runTasks(subjectUnderTestName)

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
        given: "a path to the project"
        def path = new File(projectDir, expectedPath)

        when:
        def query = new PropertyQueryTaskWriter("unity.${property}")
        query.write(buildFile)
        def result = runTasks(query.taskName)

        then:
        query.matches(result, "${path.path}")

        where:
        property     | expectedPath
        'assetsDir'  | "Assets"
        'pluginsDir' | "Assets/Plugins"
        'logsDir'    | "build/logs"
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
        def result = runTasksSuccessfully(subjectUnderTestName)

        then:
        result.wasSkipped("activateUnity")
        result.wasExecuted(subjectUnderTestName)
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
        def result = runTasksSuccessfully(subjectUnderTestName)

        then:
        result.wasExecuted("activateUnity")
        result.wasExecuted(subjectUnderTestName)
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
        def result = runTasksSuccessfully(subjectUnderTestName)

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


    @UnityPluginTestOptions(forceMockTaskRun = false, disableAutoActivateAndLicense = false)
    def "runs generateSolution task"() {
        when:
        def result = runTasksSuccessfully("generateSolution")

        then:
        result.wasExecuted("generateSolution")
    }

    @Unroll
    def "runs addUPMPackages task"() {
        given:
        new File(projectDir, manifestFile).with {
            delete()
            if (hasManifest) {
                parentFile.mkdirs()
                text = new UnityProjectManifest([:]).serialize()
            }
        }
        buildFile << """
            unity {
                enableTestCodeCoverage = ${testCoverageEnabled}
                upmPackages = ${wrapValueBasedOnType(packagesToInstall, Map)}
            }
        """
        when:
        def result = runTasks("test")

        then:
        shouldRun ?
                result.wasExecuted("addUPMPackages") :
                result.standardOutput.contains("Task :addUPMPackages SKIPPED")

        where:
        hasManifest | testCoverageEnabled | packagesToInstall  | shouldRun
        false       | false               | [:]                | false
        true        | false               | [:]                | true
        true        | false               | ["package": "ver"] | true
        false       | false               | ["package": "ver"] | true
        true        | true                | [:]                | true
        true        | true                | ["package": "ver"] | true
        false       | true                | ["package": "ver"] | true
        manifestFile = "Packages/manifest.json"
    }

    @Unroll
    def "sets extension resolution strategy #value #location"() {

        when:
        def query = runPropertyQuery(getter, set)

        then:
        query.matches(expected != _ ? expected : value)

        where:
        value                           | expected | location
        ResolutionStrategy.lowest       | _        | PropertyLocation.script
        ResolutionStrategy.highest      | _        | PropertyLocation.script
        ResolutionStrategy.lowest       | _        | PropertyLocation.property
        ResolutionStrategy.highestMinor | _        | PropertyLocation.environment
        ""                              | null     | PropertyLocation.environment

        set = new PropertySetterWriter("unity", "resolutionStrategy")
                .set(value, String)
                .withKeyComposedFrom("unity")
                .to(location)
        getter = new PropertyGetterTaskWriter(set)
    }

    @UnityPluginTestOptions(forceMockTaskRun = false)
    @Unroll
    def "task to ensure the project manifest is invoked before running #taskName"() {

        when:
        def result = runTasks(taskName)

        then:
        result.wasExecuted(ensureTaskName)

        where:
        taskName << ["setResolutionStrategy", "addUPMPackages"]
        ensureTaskName = "ensureProjectManifest"
    }
}
