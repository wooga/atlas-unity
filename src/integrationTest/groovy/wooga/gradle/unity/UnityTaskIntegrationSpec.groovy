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


import org.gradle.api.logging.LogLevel
import spock.lang.IgnoreIf
import spock.lang.Unroll
import spock.util.environment.RestoreSystemProperties
import wooga.gradle.utils.MapPropertyQueryTaskWriter
import wooga.gradle.unity.models.UnityCommandLineOption
import wooga.gradle.unity.tasks.Unity
import wooga.gradle.utils.MethodQueryTaskWriter
import wooga.gradle.utils.PropertyQueryTaskWriter

import java.lang.reflect.ParameterizedType
import java.nio.file.Files
import java.nio.file.StandardCopyOption

abstract class UnityTaskIntegrationSpec<T extends UnityTask> extends UnityIntegrationSpec {

    Class<T> getSubjectUnderTestClass() {
        if (!_sutClass) {
            try {
                this._sutClass = (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass())
                        .getActualTypeArguments()[0];
            }
            catch (Exception e) {
                this._sutClass = (Class<T>) Unity
            }
        }
        _sutClass
    }
    private Class<T> _sutClass

    @Override
    String getSubjectUnderTestName() {
        "${subjectUnderTestClass.simpleName.uncapitalize()}Test"
    }

    @Override
    String getSubjectUnderTestTypeName() {
        subjectUnderTestClass.getTypeName()
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
        result.standardOutput.contains("Starting process 'command '")
        value == result.standardOutput.contains(" $expectedCommandlineSwitch")

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
        result.standardOutput.contains("Starting process 'command '")
        value == result.standardOutput.contains(" $expectedCommandlineSwitch")
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
        result.standardOutput.contains("Starting process 'command '")
        def shouldContain = value != ""
        shouldContain == result.standardOutput.contains(" $expectedCommandlineSwitch")

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
        result.standardOutput.contains("Starting process 'command '")
        def shouldContain = value != ""
        shouldContain == result.standardOutput.contains(" $expectedCommandlineSwitch")
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
    def "can configure arguments with #method #message"() {
        given: "a custom archive task"
        buildFile << """
            ${subjectUnderTestName} {
                arguments(["--test", "value"])
            }
        """.stripIndent()

        and: "a set property"
        buildFile << """
            ${subjectUnderTestName}.${method}($value)
        """.stripIndent()

        when:
        def query = new PropertyQueryTaskWriter("${subjectUnderTestName}.${property}")
        query.write(buildFile)
        def result = runTasksSuccessfully(query.taskName)

        then:
        query.matches(result, expectedValue)

        where:
        method                    | rawValue         | type                      | append | expectedValue
        "argument"                | "--foo"          | "String"                  | true   | ["--test", "value", "--foo"]
        "arguments"               | ["--foo", "bar"] | "List<String>"            | true   | ["--test", "value", "--foo", "bar"]
        "arguments"               | ["--foo", "bar"] | "String[]"                | true   | ["--test", "value", "--foo", "bar"]
        "setAdditionalArguments"  | ["--foo", "bar"] | "List<String>"            | false  | ["--foo", "bar"]
        "setAdditionalArguments"  | ["--foo", "bar"] | "Provider<List<String>>"  | false  | ["--foo", "bar"]
        "additionalArguments.set" | ["--foo", "bar"] | "List<String>"            | false  | ["--foo", "bar"]
        "additionalArguments.set" | ["--foo", "bar"] | "Provider<List<String>>>" | false  | ["--foo", "bar"]

        property = "additionalArguments"
        value = wrapValueBasedOnType(rawValue, type)
        message = (append) ? "which appends arguments" : "which replaces arguments"
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
        if (windows) {
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

    @Unroll
    def "set environment variable #rawValue for task exec"() {
        given: "some clean environment variables"
        if (!windows) {
            def envNames = System.getenv().keySet().toArray()
            environmentVariables.clear(*envNames)
        }

        and: "a test value in system env"
        initialValue.each { key, value ->
            environmentVariables.set(key, value)
        }

        and: "an overridden environment"
        appendToSubjectTask("$method($value)")

        and: "some values in the user environment"
        environmentVariables.set("USER_A", "foo")

        when:
        def query = new MapPropertyQueryTaskWriter(propertyPath)
        query.write(buildFile)
        def result = runTasksSuccessfully(subjectUnderTestName, query.taskName)

        then:
        query.contains(result, rawValue)

        where:
        property      | useSetter | rawValue
        "environment" | true      | ["A": "foo"]
        "environment" | false     | ["A": "bar"]
        "environment" | true      | ["A": 7]
        "environment" | false     | ["A": 7]
        "environment" | true      | ["A": file("foo.bar")]
        "environment" | false     | ["A": file("foo.bar")]
        "environment" | true      | ["A": true]
        "environment" | false     | ["A": false]

        initialValue = ["B": "5", "C": "7"]
        method = (useSetter) ? "set${property.capitalize()}" : "${property}.set"
        value = wrapValueBasedOnType(rawValue, Map)
        propertyPath = "${subjectUnderTestName}.environment"

    }

    def "adds environment for task exec"() {
        given: "some clean environment variables"
        if (!windows) {
            def envNames = System.getenv().keySet().toArray()
            environmentVariables.clear(*envNames)
        }

        and: "a test value in system env"
        initialValue.each { key, value ->
            environmentVariables.set(key, value)
        }

        appendToSubjectTask("$method(${wrapValueBasedOnType(rawValue, Map)})")

        when:
        def query = new MapPropertyQueryTaskWriter(propertyPath)
        query.write(buildFile)
        def result = runTasksSuccessfully(subjectUnderTestName, query.taskName)

        then:
        query.contains(result, initialValue, rawValue)

        where:
        method               | rawValue   | initialValue
        "environment.putAll" | ["A": "7"] | ["B": "5"]

        value = wrapValueBasedOnType(rawValue, Map)
        propertyPath = "${subjectUnderTestName}.environment"
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
    def "redirects unity log to stdout when redirectStdOut is set to true for #taskType"() {
        given: "a custom build task"
        appendToSubjectTask("""
                logToStdout = false 
        """.stripIndent())

        when:
        def result = runTasks(subjectUnderTestName)

        then:
        !result.standardOutput.contains(mockUnityStartupMessage)

        when:
        appendToSubjectTask("""
                logToStdout = true 
        """.stripIndent())
        result = runTasks(subjectUnderTestName)

        then:
        result.standardOutput.contains(mockUnityStartupMessage)
    }

    def "redirects unity log to stdout and custom logfile if provided"() {
        given: "a custom log file location"
        def logFile = File.createTempFile("log", "out")
        and: "a custom build task"
        appendToSubjectTask("""
                logToStdout = true
                unityLogFile = file('${escapedPath(logFile.path)}')
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
            expectedValue = expectedValue.replace("#path", logFile.path)
            // The escaped path has two \\ dir separators
            appendToSubjectTask("unityLogFile = file('${escapedPath(logFile.path)}')")
        }

        and: "mocked unity version"
        setUnityTestVersion(unityVersion)

        when:
        def query = new MethodQueryTaskWriter("${subjectUnderTestName}.resolveLogFilePath")
        query.write(buildFile)
        def result = runTasks(subjectUnderTestName, query.taskName)

        then:
        result.wasExecuted(subjectUnderTestName)
        result.standardOutput.contains("Starting process 'command '")
        flagPresent == result.standardOutput.contains(flag)
        if (flagPresent){
            query.matches(result, expectedValue)
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
        "2018"       | true           | true        | true               | ""            | true
        "2018"       | false          | true        | false              | ""            | true
        // TODO: Log file path is enabled by default on the plugin
        // "2018"       | false          | false       | false              | ""            | false

        flag = "-logFile"

    }

}
