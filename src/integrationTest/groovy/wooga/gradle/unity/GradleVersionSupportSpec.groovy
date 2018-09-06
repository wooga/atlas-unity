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

import spock.lang.Ignore
import spock.lang.Unroll

@Ignore
class GradleVersionSupportSpec extends UnityIntegrationSpec {

    def gradleVersions() {
        ["2.14", "3.0", "3.1", "3.2", "3.4", "3.4.1", "3.5", "3.5.1", "4.0"]
    }

    def setup() {

        jvmArguments = ['-Xms1g', '-Xmx2g']
        fork = true
        memorySafeMode = true
    }

    @Unroll("verify plugin activation with gradle #gradleVersionToTest")
    def "activates with multiple gradle versions"() {
        given: "a buildfile with unity plugin applied"
        buildFile << """
            group = 'test'
            ${applyPlugin(wooga.gradle.unity.UnityPlugin)}
        """.stripIndent()

        gradleVersion = gradleVersionToTest

        expect:
        runTasksSuccessfully("tasks")

        where:
        gradleVersionToTest << gradleVersions()
    }

    @Unroll("verify build task works with gradle #gradleVersionToTest")
    def "runs basic tasks with multiple gradle versions"() {
        given: "a buildfile with unity plugin applied"
        buildFile << """
            group = 'test'
            ${applyPlugin(wooga.gradle.unity.UnityPlugin)}
        """.stripIndent()

        gradleVersion = gradleVersionToTest

        expect:
        runTasksSuccessfully("build")

        where:
        gradleVersionToTest << gradleVersions()
    }
}
