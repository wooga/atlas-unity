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

import spock.lang.Unroll
import com.wooga.spock.extensions.unity.UnityPluginTestOptions
import wooga.gradle.unity.UnityTaskIntegrationSpec
import wooga.gradle.unity.models.UnityCommandLineOption

/**
 * Integration spec for activation / return unity license
 */

class ActivateTaskIntegrationSpec extends UnityTaskIntegrationSpec<Activate> {

    @UnityPluginTestOptions(forceMockTaskRun = false)
    def "skips activation when authentication is empty"() {
        when:
        def result = runTasksSuccessfully(mockTaskName)

        then:
        result.wasSkipped(mockTaskName)
    }

    @UnityPluginTestOptions(forceMockTaskRun = false)
    @Unroll("fails activation task when credentials part is missing (#name, #password, #serial)")
    def "fails activation task when credentials part is missing"() {
        given: "a build script with fake test unity location"
        buildFile << """
            unity {
                authentication {
                    username = $name
                    password = $password
                    serial = $serial
                }
            }
        """.stripIndent()

        when:
        def result = runTasks(mockTaskName)

        then:
        result.failure

        where:
        name  | password | serial
        "'a'" | "'b'"    | null
        "'a'" | null     | "'c'"
        "'a'" | null     | null
        null  | "'b'"    | "'c'"
        null  | "'b'"    | null
        null  | null     | "'c'"
    }

    @UnityPluginTestOptions(forceMockTaskRun = false)
    def "runs activation task with serial when set via action"() {
        given: "a build script with fake test unity location"
        buildFile << """
            unity {
                authentication.username = "test@test.test"
                authentication.password = "testtesttest"
                authentication.serial = "abcdefg"                
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully(mockTaskName)

        then:
        !result.wasSkipped(mockTaskName)
        result.standardOutput.contains("${UnityCommandLineOption.userName.flag} test@test.test")
        result.standardOutput.contains("${UnityCommandLineOption.password.flag} testtesttest")
        result.standardOutput.contains("${UnityCommandLineOption.serial.flag} abcdefg")
    }

    @UnityPluginTestOptions(forceMockTaskRun = false)
    def "runs activation task with serial when set"() {
        given: "a build script with fake test unity location"
        buildFile << """
            unity.authentication.username = "test@test.test"
            unity.authentication.password = "testtesttest"
            unity.authentication.serial = "abcdefg"                
            
        """.stripIndent()

        when:
        def result = runTasksSuccessfully(mockTaskName)

        then:
        !result.wasSkipped(mockTaskName)
        result.standardOutput.contains("${UnityCommandLineOption.userName.flag} test@test.test")
        result.standardOutput.contains("${UnityCommandLineOption.password.flag} testtesttest")
        result.standardOutput.contains("${UnityCommandLineOption.serial.flag} abcdefg")
    }

    @UnityPluginTestOptions(forceMockTaskRun = false)
    def "authentication can be overridden in task configuration"() {
        given: "a build script with fake test unity location"
        buildFile << """
            unity {
                    authentication.username = "test@test.test"
                    authentication.password = "testtesttest"
                    authentication.serial = "abcdefg"                
            }

            ${mockTaskName} {
                     authentication.username = "beta@test.test"
                     authentication.password = "betatesttest"
                     authentication.serial = "zyxw"                
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully(mockTaskName)

        then:
        !result.wasSkipped(mockTaskName)
        result.standardOutput.contains("${UnityCommandLineOption.userName.flag} beta@test.test")
        result.standardOutput.contains("${UnityCommandLineOption.password.flag} betatesttest")
        result.standardOutput.contains("${UnityCommandLineOption.serial.flag} zyxw")
    }

    @UnityPluginTestOptions(forceMockTaskRun = false)
    def "authentication can be set via properties"() {
        given: "a build script with fake test unity location"
        def propertiesFile = createFile("gradle.properties")
        propertiesFile << """
            unity.authentication.username=delta@test.test
            unity.authentication.password=deltatesttest
            unity.authentication.serial=123456789
        """.stripIndent()

        when:
        def result = runTasksSuccessfully(mockTaskName)

        then:
        !result.wasSkipped(mockTaskName)
        result.standardOutput.contains("${UnityCommandLineOption.userName.flag} delta@test.test")
        result.standardOutput.contains("${UnityCommandLineOption.password.flag} deltatesttest")
        result.standardOutput.contains("${UnityCommandLineOption.serial.flag} 123456789")
    }

    @UnityPluginTestOptions(forceMockTaskRun = false)
    def "activates with unity project path"() {
        given: "a build script with fake test unity authentication"
        buildFile << """
            unity {
                authentication.username = "test@test.test"
                authentication.password = "testtesttest"
                authentication.serial = "abcdefg"
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully(mockTaskName)

        then:
        !result.wasSkipped(mockTaskName)
        result.standardOutput.contains("${UnityCommandLineOption.projectPath.flag} ${projectDir}")
    }
}
