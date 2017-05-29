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

class BaseUnityActionIntegrationSpec extends UnityIntegrationSpec {

    def "runs batchmode action"() {
        given: "a build script"
        buildFile << """
            task mUnity {
                doLast {
                    unity.batchMode {
                        args "-createProject", "Test"
                    }
                }
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("mUnity")

        then:
        result.standardOutput.contains(BatchModeFlags.CREATE_PROJECT + " Test")
    }

    def "runs batchmode task"() {
        given: "a build script"
        buildFile << """
            group = 'test'
            ${applyPlugin(UnityPlugin)}
         
            task (mUnity, type: wooga.gradle.unity.tasks.Unity) {
                args "-createProject", "Test"
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("mUnity")

        then:
        result.standardOutput.contains(BatchModeFlags.CREATE_PROJECT + " Test")
    }

//    @IgnoreRest()
//    def "runs batchmode action in Unity"() {
//        given: "path to future project"
//        def project_path = new File( projectDir,"build/test")
//
//        and: "a build script"
//        buildFile << """
//            group = 'test'
//            ${applyPlugin(UnityPlugin)}
//
//            task mUnity {
//                doLast {
//                    unity.batchMode {
//                        args "-createProject", "${escapedPath(project_path.path)}"
//                    }
//                }
//            }
//        """.stripIndent()
//
//        when:
//        def result = runTasksSuccessfully("mUnity")
//
//        then:
//        result.standardOutput.contains(DefaultUnityPluginExtension.defaultUnityLocation().path)
//        fileExists(project_path.path)
//    }
//
//    def "runs batchmode task in Unity"() {
//        given: "path to future project"
//        def project_path = new File( projectDir,"build/test")
//
//        and: "a build script"
//        buildFile << """
//            group = 'test'
//            ${applyPlugin(UnityPlugin)}
//
//            task (mUnity, type: wooga.gradle.unity.tasks.Unity) {
//                args "-createProject", "${escapedPath(project_path.path)}"
//            }
//        """.stripIndent()
//
//        when:
//        def result = runTasksSuccessfully("mUnity")
//
//        then:
//        result.standardOutput.contains(DefaultUnityPluginExtension.defaultUnityLocation().path)
//        fileExists(project_path.path)
//    }
}
