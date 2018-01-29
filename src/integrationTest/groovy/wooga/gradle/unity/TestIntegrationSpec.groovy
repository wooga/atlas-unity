/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package wooga.gradle.unity

import spock.lang.IgnoreIf
import wooga.gradle.unity.batchMode.BatchModeFlags

class TestIntegrationSpec extends UnityIntegrationSpec {

    def "calls unity test mode"() {
        given: "a build script with fake test unity location"
        buildFile << """
            task (mUnity, type: wooga.gradle.unity.tasks.Test)
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("mUnity")

        then:
        result.standardOutput.contains(BatchModeFlags.RUN_TESTS)
    }

    def "can set reports location via reports extension in task"() {
        given: "destination path"
        def destination = "out/reports/test.xml"

        and: "a build script with fake test unity location"
        buildFile << """
            task (mUnity, type: wooga.gradle.unity.tasks.Test) {
                reports.xml.destination = file("$destination")
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("mUnity")

        then:
        result.standardOutput.contains(new File(destination).path)
    }

    def "can set reports destination via reports extension in plugin"() {
        given: "destination path"
        def destination = "out/reports"
        and: "a build script with fake test unity location"
        buildFile << """
            unity.reportsDir = "$destination"
            task (mUnity, type: wooga.gradle.unity.tasks.Test)
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("mUnity")

        then:
        result.standardOutput.contains(BatchModeFlags.RUN_TESTS)
        result.standardOutput.contains(new File(destination + "/mUnity/mUnity.xml").path)
    }

    def "can disable reports via reports extension in task"() {
        given: "a build script with fake test unity location"
        buildFile << """
            task (mUnity, type: wooga.gradle.unity.tasks.Test) {
                reports.xml.enabled = false
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("mUnity")

        then:
        result.standardOutput.contains(BatchModeFlags.RUN_TESTS)
        !result.standardOutput.contains(BatchModeFlags.TEST_RESULTS)
    }

    def "has default reports location"() {
        given: "a build script with fake test unity location"
        buildFile << """
            task (mUnity, type: wooga.gradle.unity.tasks.Test)
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("mUnity")

        then:
        result.standardOutput.contains(BatchModeFlags.RUN_TESTS)
        result.standardOutput.contains(new File("reports/unity/mUnity/mUnity.xml").path)
    }

    @IgnoreIf({ os.windows })
    def "can set testPlatform to playMode"() {
        given: "a build script with fake test unity location"
        buildFile << """
            task (mUnity, type: wooga.gradle.unity.tasks.Test) {
                testPlatform = "playmode"
            }
        """.stripIndent()

        and: "a mocked project setting"
        def settings = createFile("ProjectSettings/ProjectSettings.asset")
        settings << """      
        PlayerSettings:
            wiiUDrcBufferDisabled: 0
            wiiUProfilerLibPath: 
            playModeTestRunnerEnabled: 0
            actionOnDotNetUnhandledException: 1
            
        """.stripIndent()

        and: "unity version > 5.5"

        when:
        def result = runTasksSuccessfully("mUnity", "-PdefaultUnityTestVersion=2017.1.1f3")

        then:
        result.standardOutput.contains("PlayMode tests not activated")
    }
}
