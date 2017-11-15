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

/**
 * Spec test for basic configuration of the plugin
 */
class PluginConfigurationSpec extends UnityIntegrationSpec {

    @Unroll("verify setup task is #status when running #taskName")
    def "binds all unity tasks to setup task"() {
        given: "a build script"
        buildFile << """
            task (createProject, type: wooga.gradle.unity.tasks.Unity) {
                args "-createProject", "Test"
            }
            
            task (customTest, type: wooga.gradle.unity.tasks.Test) {
            }
            
            task (customExport, type: wooga.gradle.unity.tasks.UnityPackage) {
            }
            
            task (emptyTask)
            
        """.stripIndent()

        when:
        def result = runTasks(taskName)

        then:
        result.wasExecuted(UnityPlugin.SETUP_TASK_NAME) == shouldRun
        result.wasExecuted(UnityPlugin.ASSEMBLE_RESOURCES_TASK_NAME) == shouldRun

        where:
        taskName                             | shouldRun
        UnityPlugin.TEST_TASK_NAME           | true
        UnityPlugin.EXPORT_PACKAGE_TASK_NAME | true
        "createProject"                      | true
        "customTest"                         | true
        "customExport"                       | true
        "emptyTask"                          | false

        status = shouldRun ? "executed" : "not executed"
    }

    def "sets defaultBuildTarget for all tasks"() {
        given: "a build script"
        buildFile << """
            
            task (createProject, type: wooga.gradle.unity.tasks.Unity) {
                args "-createProject", "Test"
            }
            
            unity {
                defaultBuildTarget = "android"
            }
            
            task (customTest, type: wooga.gradle.unity.tasks.Test) {
                doLast{
                    print customTest.buildTarget
                }
            }
            
        """.stripIndent()

        when:
        def result = runTasks("customTest")

        then:
        result.standardOutput.contains("android")

    }
}
