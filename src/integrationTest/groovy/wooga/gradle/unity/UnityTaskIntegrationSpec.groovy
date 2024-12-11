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

package wooga.gradle.unity

import com.wooga.gradle.PlatformUtils
import com.wooga.gradle.test.PropertyQueryTaskWriter
import com.wooga.gradle.test.TaskIntegrationSpec
import com.wooga.gradle.test.writers.PropertyGetterTaskWriter
import org.gradle.api.logging.LogLevel
import spock.lang.Unroll
import wooga.gradle.unity.testutils.GradleRunResult
import wooga.gradle.unity.models.UnityCommandLineOption

import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.Duration
import java.time.Instant

abstract class UnityTaskIntegrationSpec<T extends UnityTask> extends UnityIntegrationSpec
        implements TaskIntegrationSpec<T> {
    @Override
    String getSubjectUnderTestName() {
        "${subjectUnderTestClass.simpleName.uncapitalize()}Test"
    }

    @Unroll
    def "can set option '#property' (#value) with #method"() {
        given: "a custom build task"
        appendToSubjectTask("$method($value)")

        and: "make sure the test file exists"
        def testFile = createFile("test/file")

        when:
        def result = runTasks(subjectUnderTestName)

        then:
        result.wasExecuted(subjectUnderTestName)
        def stdOut = GradleRunResult.taskLog(subjectUnderTestName, result.standardOutput)
        stdOut.contains("Starting process 'command '")
        value == stdOut.contains(" $expectedCommandlineSwitch")

        where:
        [testCase, useSetter, value] <<
                [
                        // For each option that is a flag (with a boolean value)
                        UnityCommandLineOption.flags.collect(
                                { it -> ["${it}", it.flag] }),
                        // Test with and without the setter
                        [true, false],
                        // Test both true and false values
                        [true, false]
                ].combinations()

        property = testCase[0]
        expectedCommandlineSwitch = testCase[1]
        method = (useSetter) ? "set${property.capitalize()}" : property
    }

    @Unroll
    def "can get option '#property' (#value) with #getMethod"() {
        given: "a custom build task"
        appendToSubjectTask("$setMethod($value)")

        when:
        def query = new PropertyQueryTaskWriter("${subjectUnderTestName}.${property}")
        query.write(buildFile)
        def result = runTasks(subjectUnderTestName, query.taskName)

        then:
        result.wasExecuted(subjectUnderTestName)
        def stdOut = GradleRunResult.taskLog(subjectUnderTestName, result.standardOutput)
        stdOut.contains("Starting process 'command '")
        query.matches(result, value)

        where:
        [testCase, useGetter, value] <<
                [
                        // For each option that is a flag (with a boolean value)
                        UnityCommandLineOption.flags.collect(
                                { it -> ["${it}", it.flag] }),
                        // Test with and without the setter
                        [true, false],
                        // Test both true and false values
                        [true, false]
                ].combinations()

        property = testCase[0]
        expectedCommandlineSwitch = testCase[1]
        setMethod = "set${property.capitalize()}"
        getMethod = property
    }

    @Unroll
    def "can set arguments option '#property' = '#value' by #method"() {
        given: "a custom build task"
        buildFile << """
            ${subjectUnderTestName} {
                $method("$value")
            }
        """.stripIndent()

        and: "make sure the test file exists"
        def testFile = createFile("test/file")

        when:
        def result = runTasks(subjectUnderTestName)

        then:
        result.wasExecuted(subjectUnderTestName)
        def stdOut = GradleRunResult.taskLog(subjectUnderTestName, result.standardOutput)
        stdOut.contains("Starting process 'command '")
        def shouldContain = value != ""
        shouldContain == stdOut.contains(" $expectedCommandlineSwitch")

        where:
        [testCase, useSetter, value] <<
                [
                        // For each option that requires an argument (with a string value)
                        UnityCommandLineOption.argumentFlags.collect(
                                { it -> ["${it}", it.flag] }),
                        // Test with and without the setter
                        [true, false],
                        // Test a valid string and an empty one (which will lead to the option being ignored)
                        ["foobar", ""]
                ].combinations()

        property = testCase[0]
        expectedCommandlineSwitch = testCase[1]
        method = (useSetter) ? "set${property.capitalize()}" : property
    }

    @Unroll
    def "can get arguments option '#property' = '#value' by #getMethod"() {
        given: "a custom build task"
        buildFile << """
            ${subjectUnderTestName} {
                $method("$value")
            }
        """.stripIndent()

        when:
        def query = new PropertyQueryTaskWriter("${subjectUnderTestName}.${property}")
        query.write(buildFile)
        def result = runTasks(subjectUnderTestName, query.taskName)

        then:
        result.wasExecuted(subjectUnderTestName)
        def subjectStdOut = GradleRunResult.taskLog(subjectUnderTestName, result.standardOutput)
        subjectStdOut.contains("Starting process 'command '")
        def shouldContain = value != ""
        shouldContain == subjectStdOut.contains(" $expectedCommandlineSwitch")
        // GradleRunResult.taskLog(query.taskName, result.standardOutput)
        query.filterTaskOutput()
        query.matches(result, value)

        where:
        [testCase, useSetter, value] <<
                [
                        // For each option that requires an argument (with a string value)
                        UnityCommandLineOption.argumentFlags.collect(
                                { it -> ["${it}", it.flag] }),
                        // Test with and without the setter
                        [true, false],
                        // Test a valid string and an empty one (which will lead to the option being ignored)
                        ["foobar", ""]
                ].combinations()

        property = testCase[0]
        expectedCommandlineSwitch = testCase[1]
        method = (useSetter) ? "set${property.capitalize()}" : property
        getMethod = property
    }

    @Unroll
    def "can set unityPath with #method"() {
        given: "a build file with custom test task"
        appendToSubjectTask("$method(file('$value'))")

        and:
        def testUnity = createFile(value)
        Files.copy(mockUnityFile.toPath(), testUnity.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES)

        when:
        def result = runTasksSuccessfully(subjectUnderTestName)

        then:
        if (PlatformUtils.windows) {
            value = value.replace('/', '\\')
        }
        result.standardOutput.contains(value)

        where:
        property    | useSetter | value
        "unityPath" | true      | "custom/unity.bat"
        "unityPath" | false     | "custom/unity.bat"

        method = (useSetter) ? "set${property.capitalize()}" : "${property}.set"
    }

    @Unroll
    def "set log category with #method to '#value'"() {
        given:
        buildFile << """
            ${subjectUnderTestName}.$method(${value})
            """.stripIndent()

        when:
        def query = new PropertyQueryTaskWriter("${subjectUnderTestName}.unityLogFile")
        query.write(buildFile)
        def result = runTasksSuccessfully(subjectUnderTestName, query.taskName)

        then:
        result.wasExecuted(subjectUnderTestName)
        def resultPath = new File(projectDir, "/build/logs/${path}").getPath()
        query.matches(result, resultPath)

        where:
        rawValue     | useSetter | path
        "helloworld" | true      | "helloworld/${subjectUnderTestName}.log"
        "helloworld" | false     | "helloworld/${subjectUnderTestName}.log"
        "foobar"     | true      | "foobar/${subjectUnderTestName}.log"
        ""           | true      | "${subjectUnderTestName}.log"
        ""           | false     | "${subjectUnderTestName}.log"
        null         | true      | "unity/${subjectUnderTestName}.log"
        null         | false     | "unity/${subjectUnderTestName}.log"

        value = rawValue != null ? wrapValueBasedOnType(rawValue, String) : null
        method = (useSetter) ? "setLogCategory" : "logCategory.set"
    }

    def "task executes without output when set to quiet"() {
        given:
        logLevel = LogLevel.QUIET

        when:
        def result = runTasksSuccessfully(subjectUnderTestName)

        then:
        !result.standardOutput.contains(mockUnityStartupMessage)
    }

    @Unroll
    def "redirects ? (#value) unity log to stdout when #propertyName is set to #value"() {

        given: "the property is set"
        appendToSubjectTask("""
                ${propertyName} = ${wrapValueBasedOnType(value, Boolean)} 
        """.stripIndent())

        when:
        def result = runTasks(subjectUnderTestName)

        then:
        def stdOut = GradleRunResult.taskLog(subjectUnderTestName, result.standardOutput)
        value == stdOut.contains(mockUnityStartupMessage)

        where:
        propertyName = "logToStdout"
        value << [true, false]
    }

    def "redirects unity log to stdout and custom logfile if provided"() {
        given: "a custom log file location"
        def logFile = File.createTempFile("log", "out")
        and: "a custom build task"
        appendToSubjectTask("""
                logToStdout = true
                unityLogFile = file('${PlatformUtils.escapedPath(logFile.path)}')
        """.stripIndent())

        when:
        def query = new PropertyQueryTaskWriter("${subjectUnderTestName}.logFile")
        query.write(buildFile)
        def result = runTasks(subjectUnderTestName, query.taskName)

        then:
        result.standardOutput.contains(mockUnityStartupMessage)
        logFile.text.contains(mockUnityStartupMessage)
        !query.matches(result, logFile.path)
    }

    @Unroll
    def "calls unity with correct logFile parameter when logToStdout is '#logToStdout' and logFilePath is '#logFilePathSet' on unityVersion '#unityVersion'"() {
        given:
        appendToSubjectTask("logToStdout = ${logToStdout}")

        if (logFilePathSet) {
            def logFile = File.createTempFile("log", "out")
            // The escaped path has two \\ dir separators
            appendToSubjectTask("unityLogFile = file('${PlatformUtils.escapedPath(logFile.path)}')")

            if (expectedValue != null) {
                expectedValue = expectedValue.replace("#path", logFile.path)
            }
        }

        and: "mocked unity version"
        setUnityTestVersion(unityVersion)

        when:
        def get = new PropertyGetterTaskWriter(subjectUnderTestName, ".resolveLogFilePath()")
        get.write(this)
        def result = runTasks(subjectUnderTestName, get.taskName)

        then:
        result.wasExecuted(subjectUnderTestName)
        result.standardOutput.contains("Starting process 'command '")
        flagPresent == result.standardOutput.contains(flag)
        if (flagPresent) {
            def query = get.generateQuery(this, result)
            query.matches(expectedValue, String)
        }

        where:
        unityVersion | logFilePathSet | logToStdout | isLogWrittenToFile | expectedValue | flagPresent
        "2019"       | true           | false       | true               | "#path"       | true
        "2019"       | true           | true        | true               | "-"           | true
        "2019"       | false          | true        | false              | "-"           | true
        // TODO: Perhaps think about disabling logging on an extension-scope for all tasks
        // TODO: Log file path is enabled by default on the plugin
        // "2019"       | false          | false       | false              | ""            | false

        "2018"       | true           | false       | true               | "#path"       | true
        "2018"       | true           | true        | true               | null          | true
        "2018"       | false          | true        | false              | null          | true
        // TODO: Log file path is enabled by default on the plugin
        // "2018"       | false          | false       | false              | ""            | false

        flag = "-logFile"

    }

    @Unroll
    def "unity #message #maxRetries times with #retryWait wait times when line in log matches #retryRegexes"() {
        given:
//        def fakeUnity = createMockUnity(unityLog, 1)
//        def fakeUnityFile = fakeUnity.toDirectory(unityMainDirectory)
//        addUnityPathToExtension(fakeUnityFile.absolutePath)

        writeMockExecutable ({
            it.withText(unityLog)
            it.exitValue = 1
        })

        buildFile << """
        $subjectUnderTestName {
            maxRetries = ${wrapValue(maxRetries, Integer)}
            retryWait = Duration.ofMillis(${wrapValue(retryWait.toMillis(), Integer)})
            retryRegexes = ${"[" + retryRegexes.collect { "/$it/" }.join(", ") + "]"}
        }
        """

        when:
        def startTime = Instant.now()
        def result = runTasks(subjectUnderTestName, "-xensureProjectManifest")
        def endTime = Instant.now()
        then:
        def stdout = result.standardOutput
        def duration = Duration.ofMillis(endTime.minusMillis(startTime.toEpochMilli()).toEpochMilli())
        def expectedRunCount = shouldRetry ? maxRetries : 1

        stdout.readLines().count { it.contains(mockUnityStartupMessage) } == expectedRunCount
        shouldRetry ?
                duration > retryWait.multipliedBy(maxRetries) :
                duration < retryWait

        where:
        maxRetries | retryWait             | retryRegexes                                             | shouldRetry
        3          | Duration.ofSeconds(2) | [/^\s*TestPro License:\s*NO$/]                           | true
        3          | Duration.ofSeconds(2) | [/^\s*Not Matching:\s*NO$/, /^\s*TestPro License:\s*NO/] | true
        99         | Duration.ofSeconds(2) | [/^\s*Not matching:\s*$/]                                | false
        unityLog = """
        Some very verbose stuff
        TestPro License: NO
        more verbosity
        """.readLines().collect { it.trim().stripIndent() }.findAll { !it.empty } join("\n")
        message = shouldRetry ? "should retry" : "shouldn't retry"
    }
}


