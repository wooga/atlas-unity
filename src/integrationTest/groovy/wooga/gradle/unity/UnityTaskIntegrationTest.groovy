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

abstract class UnityTaskIntegrationTest<T extends UnityTask> extends UnityIntegrationTest {

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
    String getTestTaskName() {
        "${taskClass.simpleName}Mock"
    }

    @Override
    String getTestTaskTypeName() {
        taskClass.getTypeName()
    }

    @Unroll
    def "can set option '#property' (#value) with #method"() {
        given: "a custom build task"
        appendToTestTask("$method($value)")

        and: "make sure the test file exists"
        def testFile = createFile("test/file")

        when:
        def result = runTasks(testTaskName)

        then:
        result.wasExecuted(testTaskName)
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
            ${testTaskName} {
                $method("$value")
            }
        """.stripIndent()

        and: "make sure the test file exists"
        def testFile = createFile("test/file")

        when:
        def result = runTasks(testTaskName)

        then:
        result.wasExecuted(testTaskName)
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
            ${testTaskName} {
                arguments(["--test", "value"])
            }
        """.stripIndent()

        and: "a task to read back the value"
        buildFile << """
            task("readValue") {
                doLast {
                    println("property: " + ${testTaskName}.${property}.get())
                }
            }
        """.stripIndent()

        and: "a set property"
        buildFile << """
            ${testTaskName}.${method}($value)
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
        appendToTestTask("$method(file('$value'))")

        and:
        def testUnity = createFile(value)
        Files.copy(mockUnityFile.toPath(), testUnity.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES)

        when:
        def result = runTasksSuccessfully(testTaskName)

        then:
        if (isWindows) {
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
            ${testTaskName}.$method(${value})
            """.stripIndent()
        addProviderQueryTask("custom", "${testTaskName}.unityLogFile", ".get().asFile.path")

        when:
        def result = runTasksSuccessfully(testTaskName, "custom")

        then:
        result.wasExecuted(testTaskName)
        def resultPath = new File(projectDir, "/build/logs/${path}").getPath()
        //result.standardOutput.contains("-logFile ${resultPath}")
        result.standardOutput.contains("${testTaskName}.unityLogFile: ${resultPath}")

        where:
        rawValue     | useSetter | path
        "helloworld" | true      | "helloworld/${testTaskName}.log"
        "helloworld" | false     | "helloworld/${testTaskName}.log"
        "foobar"     | true      | "foobar/${testTaskName}.log"
        ""           | true      | "${testTaskName}.log"
        ""           | false     | "${testTaskName}.log"
        null         | true      | "unity/${testTaskName}.log"
        null         | false     | "unity/${testTaskName}.log"

        value = rawValue != null ? wrapValueBasedOnType(rawValue, String) : null
        method = (useSetter) ? "setLogCategory" : "logCategory.set"
    }

    def "task executes without output when set to quiet"() {
        given:
        logLevel = LogLevel.QUIET

        when:
        def result = runTasksSuccessfully(testTaskName)

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
