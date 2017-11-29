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

import spock.lang.Shared
import spock.lang.Unroll
import spock.util.environment.RestoreSystemProperties

/**
 * Spec test for basic configuration of the plugin
 */
class PluginConfigurationSpec extends UnityIntegrationSpec {

    @Unroll("sets buildTarget with #taskConfig #useOverride")
    def "sets defaultBuildTarget for all tasks"() {
        given: "a build script"
        buildFile << """
            unity {
                defaultBuildTarget = "android"
            }

            task (customTest, type: wooga.gradle.unity.tasks.Test) {
                $taskConfig
            }

        """.stripIndent()

        when:
        def result = runTasks("customTest")

        then:
        result.standardOutput.contains(expected)

        where:
        taskConfig            | expected
        'buildTarget = "ios"' | "-buildTarget ios"
        ''                    | "-buildTarget android"

        useOverride = taskConfig != '' ? "use override" : "fallback to default"
    }

    @Unroll
    def "plugin sets default #property"() {
        given: "a build script"

        buildFile << """
            task(customTest) {
                doLast {
                    print "$property: "
                    println unity.$property
                }
            }
        """

        and: "a path to the project"
        def path = new File(projectDir, expectedPath)

        when:
        def result = runTasks("customTest")

        then:
        result.standardOutput.contains("$property: ${path.path}")

        where:
        property    | expectedPath
        'assetsDir' | "Assets"
        'pluginsDir' | "Assets/Plugins"
    }
}
