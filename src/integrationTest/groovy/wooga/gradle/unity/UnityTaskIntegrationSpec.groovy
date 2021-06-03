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
import spock.lang.Unroll
import wooga.gradle.unity.models.UnityCommandLineOption
import wooga.gradle.unity.tasks.Unity

import java.lang.reflect.ParameterizedType
import java.nio.file.Files
import java.nio.file.StandardCopyOption

abstract class UnityTaskIntegrationSpec<T extends UnityTask> extends UnityIntegrationSpec {

    Class<T> getTaskClass() {
        if (!_taskClass) {
            try {
                this._taskClass = (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass())
                        .getActualTypeArguments()[0];
            }
            catch (Exception e) {
                this._taskClass = (Class<T>) Unity
            }
        }
        _taskClass
    }
    private Class<T> _taskClass

    @Override
    String getMockTaskName() {
        "${taskClass.simpleName.uncapitalize()}Mock"
    }

    @Override
    String getMockTaskTypeName() {
        taskClass.getTypeName()
    }

    @Unroll
    def "can set option '#property' (#value) with #method"() {
        given: "a custom build task"
        appendToMockTask("$method($value)")

        and: "make sure the test file exists"
        def testFile = createFile("test/file")

        when:
        def result = runTasks(mockTaskName)

        then:
        result.wasExecuted(mockTaskName)
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
            ${mockTaskName} {
                $method("$value")
            }
        """.stripIndent()

        and: "make sure the test file exists"
        def testFile = createFile("test/file")

        when:
        def result = runTasks(mockTaskName)

        then:
        result.wasExecuted(mockTaskName)
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
            ${mockTaskName} {
                arguments(["--test", "value"])
            }
        """.stripIndent()

        and: "a task to read back the value"
        buildFile << """
            task("readValue") {
                doLast {
                    println("property: " + ${mockTaskName}.${property}.get())
                }
            }
        """.stripIndent()

        and: "a set property"
        buildFile << """
            ${mockTaskName}.${method}($value)
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
        appendToMockTask("$method(file('$value'))")

        and:
        def testUnity = createFile(value)
        Files.copy(mockUnityFile.toPath(), testUnity.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES)

        when:
        def result = runTasksSuccessfully(mockTaskName)

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
            ${mockTaskName}.$method(${value})
            """.stripIndent()
        addProviderQueryTask("custom", "${mockTaskName}.unityLogFile", ".get().asFile.path")

        when:
        def result = runTasksSuccessfully(mockTaskName, "custom")

        then:
        result.wasExecuted(mockTaskName)
        def resultPath = new File(projectDir, "/build/logs/${path}").getPath()
        //result.standardOutput.contains("-logFile ${resultPath}")
        result.standardOutput.contains("${mockTaskName}.unityLogFile: ${resultPath}")

        where:
        rawValue     | useSetter | path
        "helloworld" | true      | "helloworld/${mockTaskName}.log"
        "helloworld" | false     | "helloworld/${mockTaskName}.log"
        "foobar"     | true      | "foobar/${mockTaskName}.log"
        ""           | true      | "${mockTaskName}.log"
        ""           | false     | "${mockTaskName}.log"
        null         | true      | "unity/${mockTaskName}.log"
        null         | false     | "unity/${mockTaskName}.log"

        value = rawValue != null ? wrapValueBasedOnType(rawValue, String) : null
        method = (useSetter) ? "setLogCategory" : "logCategory.set"
    }

    def "task executes without output when set to quiet"() {
        given:
        logLevel = LogLevel.QUIET

        when:
        def result = runTasksSuccessfully(mockTaskName)

        then:
        !result.standardOutput.contains(mockUnityMessage)
    }

//
//    @Ignore("test occasionally fails on jenkins")
//    @IgnoreIf({ FileUtils.isWindows() })
//    def "redirects unity log to stdout and custom logfile if provided"() {
//        given: "a custom log file location"
//        def logFile = File.createTempFile("log", "out")
//        and: "a custom build task"
//        buildFile << """
//            task (${testTaskName}, type: ${testTaskTypeName}) {
//                dependsOn unitySetup
//                redirectStdOut = true
//                logFile = file('${escapedPath(logFile.path)}')
//            }
//        """.stripIndent()
//
//        when:
//        def result = runTasks(testTaskName)
//
//        then:
//        result.standardOutput.contains("Next license update check is after")
//        logFile.text.contains("Next license update check is after")
//    }

}
