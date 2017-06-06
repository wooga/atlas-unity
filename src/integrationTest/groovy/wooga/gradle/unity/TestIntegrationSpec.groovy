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
        result.standardOutput.contains(BatchModeFlags.RUN_EDITOR_TESTS)
        result.standardOutput.contains(BatchModeFlags.EDITOR_TEST_VERBOSE_LOG)
        result.standardOutput.contains(BatchModeFlags.EDITOR_TEST_RESULTS_FILE)
        and: "not"
        !result.standardOutput.contains(BatchModeFlags.EDITOR_TEST_FILTER)
        !result.standardOutput.contains(BatchModeFlags.EDITOR_TEST_CATEGORIES)
    }

    def "can set reports location via reports extension in task"() {
        given: "destination path"
        def destination = "out/reports/test.xml"
        and: "a build script with fake test unity location"
        buildFile << """
            task (mUnity, type: wooga.gradle.unity.tasks.Test) {
                reports.xml.destination = "$destination"
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("mUnity")

        then:
        result.standardOutput.contains(BatchModeFlags.RUN_EDITOR_TESTS)
        result.standardOutput.contains(BatchModeFlags.EDITOR_TEST_RESULTS_FILE)
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
        result.standardOutput.contains(BatchModeFlags.RUN_EDITOR_TESTS)
        result.standardOutput.contains(BatchModeFlags.EDITOR_TEST_RESULTS_FILE)
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
        result.standardOutput.contains(BatchModeFlags.RUN_EDITOR_TESTS)
        !result.standardOutput.contains(BatchModeFlags.EDITOR_TEST_RESULTS_FILE)
    }

    def "has default reports location"() {
        given: "a build script with fake test unity location"
        buildFile << """
            task (mUnity, type: wooga.gradle.unity.tasks.Test)
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("mUnity")

        then:
        result.standardOutput.contains(BatchModeFlags.RUN_EDITOR_TESTS)
        result.standardOutput.contains(BatchModeFlags.EDITOR_TEST_RESULTS_FILE)
        result.standardOutput.contains(new File("reports/unity/mUnity/mUnity.xml").path)
    }

    def "can set test categories as assignment"() {
        given: "a build script with fake test unity location"
        buildFile << """
            task (mUnity, type: wooga.gradle.unity.tasks.Test) {
                categories = ["One", "Two", "Three"]
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("mUnity")

        then:
        result.standardOutput.contains(BatchModeFlags.RUN_EDITOR_TESTS)
        result.standardOutput.contains(BatchModeFlags.EDITOR_TEST_CATEGORIES + " One,Two,Three")
    }

    def "can set test categories as method"() {
        given: "a build script with fake test unity location"
        buildFile << """
            task (mUnity, type: wooga.gradle.unity.tasks.Test) {
                categories(["One", "Two", "Three"])
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("mUnity")

        then:
        result.standardOutput.contains(BatchModeFlags.RUN_EDITOR_TESTS)
        result.standardOutput.contains(BatchModeFlags.EDITOR_TEST_CATEGORIES + " One,Two,Three")
    }

    def "can set test filter as assignment"() {
        given: "a build script with fake test unity location"
        buildFile << """
            task (mUnity, type: wooga.gradle.unity.tasks.Test) {
                filter = ["One", "Two", "Three"]
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("mUnity")

        then:
        result.standardOutput.contains(BatchModeFlags.RUN_EDITOR_TESTS)
        result.standardOutput.contains(BatchModeFlags.EDITOR_TEST_FILTER + " One,Two,Three")
    }

    def "can set test filter as method"() {
        given: "a build script with fake test unity location"
        buildFile << """
            task (mUnity, type: wooga.gradle.unity.tasks.Test) {
                filter(["One", "Two", "Three"])
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("mUnity")

        then:
        result.standardOutput.contains(BatchModeFlags.RUN_EDITOR_TESTS)
        result.standardOutput.contains(BatchModeFlags.EDITOR_TEST_FILTER + " One,Two,Three")
    }

    def "can set test verbose output"() {
        given: "a build script with fake test unity location"
        buildFile << """
            task (mUnity, type: wooga.gradle.unity.tasks.Test) {
                verbose = false
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("mUnity")

        then:
        result.standardOutput.contains(BatchModeFlags.RUN_EDITOR_TESTS)
        !result.standardOutput.contains(BatchModeFlags.EDITOR_TEST_VERBOSE_LOG)
    }

    def "can set test verbose teamcity output"() {
        given: "a build script with fake test unity location"
        buildFile << """
            task (mUnity, type: wooga.gradle.unity.tasks.Test) {
                verbose = true
                teamcity = true
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("mUnity")

        then:
        result.standardOutput.contains(BatchModeFlags.RUN_EDITOR_TESTS)
        result.standardOutput.contains(BatchModeFlags.EDITOR_TEST_VERBOSE_LOG + ' teamcity')
    }

    def "can set teamcity log only when verbose is set"() {
        given: "a build script with fake test unity location"
        buildFile << """
            task (mUnity, type: wooga.gradle.unity.tasks.Test) {
                verbose = false
                teamcity = true
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("mUnity")

        then:
        result.standardOutput.contains(BatchModeFlags.RUN_EDITOR_TESTS)
        !result.standardOutput.contains(BatchModeFlags.EDITOR_TEST_VERBOSE_LOG + " teamcity")
    }

}
