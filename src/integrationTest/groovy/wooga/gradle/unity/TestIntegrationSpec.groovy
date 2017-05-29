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

import spock.lang.Unroll
import wooga.gradle.unity.batchMode.BatchModeFlags

class TestIntegrationSpec extends UnityIntegrationSpec {

    def setup() {

    }

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

    @Unroll
    def "can set reports results location as method with value #testValue"(String testValue) {
        given: "a build script with fake test unity location"
        buildFile << """
            task (mUnity, type: wooga.gradle.unity.tasks.Test) {
                reportsPath $testValue
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("mUnity")

        then:
        result.standardOutput.contains(BatchModeFlags.RUN_EDITOR_TESTS)
        result.standardOutput.contains(BatchModeFlags.EDITOR_TEST_RESULTS_FILE)
        result.standardOutput.contains(new File("/reports/Test.xml").path)

        where:
        testValue << ['project.file("/reports/Test.xml")', '{"/reports/Test.xml"}', '"/reports/Test.xml"']
    }

    @Unroll
    def "can set reports results location as assignment with value #testValue"(String testValue) {
        given: "a build script with fake test unity location"
        buildFile << """
            task (mUnity, type: wooga.gradle.unity.tasks.Test) {
                reportsPath = $testValue
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("mUnity")

        then:
        result.standardOutput.contains(BatchModeFlags.RUN_EDITOR_TESTS)
        result.standardOutput.contains(BatchModeFlags.EDITOR_TEST_RESULTS_FILE)
        result.standardOutput.contains(new File("/reports/Test.xml").path)

        where:
        testValue << ['project.file("/reports/Test.xml")', '{"/reports/Test.xml"}', '"/reports/Test.xml"']
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
