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

import com.wooga.spock.extensions.unity.UnityPathResolution
import com.wooga.spock.extensions.unity.UnityPluginTestOptions
import com.wooga.spock.extensions.uvm.UnityInstallation
import org.gradle.api.logging.LogLevel
import spock.lang.IgnoreIf
import spock.lang.Unroll
import spock.util.environment.RestoreSystemProperties
import wooga.gradle.unity.models.UnityCommandLineOption
import wooga.gradle.unity.tasks.Unity

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
                        UnityCommandLineOption.flags.collect(
                                { it -> ["${it}", it.flag] }),
                        [true, false],
                        [true, false]
                ].combinations()

        property = testCase[0]
        expectedCommandlineSwitch = testCase[1]
        method = (useSetter) ? "set${property.capitalize()}" : property
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
                        UnityCommandLineOption.argumentFlags.collect(
                                { it -> ["${it}", it.flag] }),
                        [true, false],
                        ["foobar", ""]
                ].combinations()

        property = testCase[0]
        expectedCommandlineSwitch = testCase[1]
        method = (useSetter) ? "set${property.capitalize()}" : property
    }

    @Unroll
    def "can configure arguments with #method #message"() {
        given: "a custom archive task"
        buildFile << """
            ${subjectUnderTestName} {
                arguments(["--test", "value"])
            }
        """.stripIndent()

        and: "a task to read back the value"
        buildFile << """
            task("readValue") {
                doLast {
                    println("property: " + ${subjectUnderTestName}.${property}.get())
                }
            }
        """.stripIndent()

        and: "a set property"
        buildFile << """
            ${subjectUnderTestName}.${method}($value)
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("readValue")

        then:
        outputContains(result, "property: " + expectedValue.toString())

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
        addProviderQueryTask("custom", "${subjectUnderTestName}.unityLogFile", ".get().asFile.path")

        when:
        def result = runTasksSuccessfully(subjectUnderTestName, "custom")

        then:
        result.wasExecuted(subjectUnderTestName)
        def resultPath = new File(projectDir, "/build/logs/${path}").getPath()
        //result.standardOutput.contains("-logFile ${resultPath}")
        result.standardOutput.contains("${subjectUnderTestName}.unityLogFile: ${resultPath}")

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
        def envNames = System.getenv().keySet().toArray()
        environmentVariables.clear(*envNames)

        and: "a test value in system env"
        initialValue.each { key, value ->
            environmentVariables.set(key, value)
        }

        and: "an overridden environment"
        appendToSubjectTask("$method($value)")
        addProviderQueryTask("custom", "${subjectUnderTestName}.environment", ".get()")

        and: "some values in the user environment"
        environmentVariables.set("USER_A", "foo")

        when:
        def result = runTasksSuccessfully(subjectUnderTestName, "custom")

        then:
        result.standardOutput.contains("${subjectUnderTestName}.environment: ${rawValue.toString()}")

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

        initialValue = ["B": "5", "C" : "7"]
        method = (useSetter) ? "set${property.capitalize()}" : "${property}.set"
        value = wrapValueBasedOnType(rawValue, Map)
    }

    def "adds environment for task exec"() {
        given: "some clean environment variables"
        def envNames = System.getenv().keySet().toArray()
        environmentVariables.clear(*envNames)

        and: "a test value in system env"
        initialValue.each { key, value ->
            environmentVariables.set(key, value)
        }

        appendToSubjectTask("$method(${wrapValueBasedOnType(rawValue, Map)})")
        addProviderQueryTask("custom", "${subjectUnderTestName}.environment", ".get().sort()")

        when:
        def result = runTasksSuccessfully(subjectUnderTestName, "custom")

        then:
        def expectedValue = new HashMap<String, ?>()
        expectedValue.putAll(initialValue)
        expectedValue.putAll(rawValue)
        result.standardOutput.contains("${subjectUnderTestName}.environment: ${expectedValue}")

        where:
        method               | rawValue   | initialValue
        "environment.putAll" | ["A": "7"] | ["B": "5"]

        value = wrapValueBasedOnType(rawValue, Map)
    }

    def "task executes without output when set to quiet"() {
        given:
        logLevel = LogLevel.QUIET

        when:
        def result = runTasksSuccessfully(subjectUnderTestName)

        then:
        !result.standardOutput.contains(mockUnityStartupMessage)
    }


    //I run this test only in macOS due to the path encoding issues.
    @IgnoreIf({ System.getProperty("os.name").toLowerCase().contains("windows") })
    @Unroll
    @RestoreSystemProperties
    def "calls unity with correct logFile parameter when redirectStdOut: #redirectStdOut logFilePath: #logFilePath unityVersion: #unityVersion os: #os"() {
        given:
        if (logFilePath) {
            appendToSubjectTask """
            unityLogFile = ${logFilePath}
            """.stripIndent()
        }

        appendToSubjectTask("""
            logToStdout.set(${redirectStdOut})
        """.stripIndent())

        and: "mocked unity version"
        createFile("gradle.properties") << "defaultUnityTestVersion=${unityVersion}"

        and: "mocked operating system"
        System.setProperty('os.name', os)

        and: "make sure the test file exists"
        def testFile = createFile("test/file")

        when:
        def result = runTasks(subjectUnderTestName)

        then:
        result.wasExecuted(subjectUnderTestName)
        result.standardOutput.contains("Starting process 'command '")
        def cmdSwitch = expectedCommandlineSwitch.replace("#{DEFAULT_LOG_FILE_PATH}", new File(projectDir, "build/logs").path).replace("#{PROVIDED_LOG_FILE_PATH}", new File(projectDir, "test/file").path)
        result.standardOutput.contains("${cmdSwitch}".trim())

        where:
        redirectStdOut | logFilePath         | unityVersion | expectedCommandlineSwitch            | os
        true           | null                | "2018.1.0f1" | "-logFile"                           | "Mac OS X"
        true           | null                | "2018.1.0f1" | "-logFile"                           | "Linux"
        true           | null                | "2018.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Windows 7"
        true           | null                | "2019.1.0f1" | "-logFile -"                         | "Mac OS X"
        true           | null                | "2019.1.0f1" | "-logFile -"                         | "Linux"
        true           | null                | "2019.1.0f1" | "-logFile -"                         | "Windows 7"
        true           | 'file("test/file")' | "2018.1.0f1" | "-logFile"                           | "Mac OS X"
        true           | 'file("test/file")' | "2018.1.0f1" | "-logFile"                           | "Linux"
        true           | 'file("test/file")' | "2018.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Windows 7"
        true           | 'file("test/file")' | "2019.1.0f1" | "-logFile -"                         | "Mac OS X"
        true           | 'file("test/file")' | "2019.1.0f1" | "-logFile -"                         | "Linux"
        true           | 'file("test/file")' | "2019.1.0f1" | "-logFile -"                         | "Windows 7"
        false          | null                | "2018.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Mac OS X"
        false          | null                | "2018.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Linux"
        false          | null                | "2018.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Windows 7"
        false          | null                | "2019.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Mac OS X"
        false          | null                | "2019.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Linux"
        false          | null                | "2019.1.0f1" | "-logFile #{DEFAULT_LOG_FILE_PATH}"  | "Windows 7"
        false          | 'file("test/file")' | "2018.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Mac OS X"
        false          | 'file("test/file")' | "2018.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Linux"
        false          | 'file("test/file")' | "2018.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Windows 7"
        false          | 'file("test/file")' | "2019.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Mac OS X"
        false          | 'file("test/file")' | "2019.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Linux"
        false          | 'file("test/file")' | "2019.1.0f1" | "-logFile #{PROVIDED_LOG_FILE_PATH}" | "Windows 7"
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
        def result = runTasks(subjectUnderTestName)

        then:
        result.standardOutput.contains(mockUnityStartupMessage)
        logFile.text.contains(mockUnityStartupMessage)
    }

}
