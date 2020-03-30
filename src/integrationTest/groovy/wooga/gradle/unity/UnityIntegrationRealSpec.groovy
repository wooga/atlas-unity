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

import com.wooga.spock.extensions.uvm.UnityInstallation
import net.wooga.uvm.Installation
import org.apache.commons.lang.StringEscapeUtils
import org.junit.contrib.java.lang.system.EnvironmentVariables
import spock.lang.Ignore
import spock.lang.IgnoreIf
import spock.lang.Shared

@IgnoreIf({ os.windows })
class UnityIntegrationRealSpec extends IntegrationSpec {

    def escapedPath(String path) {
        String osName = System.getProperty("os.name").toLowerCase()
        if (osName.contains("windows")) {
            return StringEscapeUtils.escapeJava(path)
        }
        path
    }

    EnvironmentVariables environmentVariables = new EnvironmentVariables()

    @Shared
    @UnityInstallation(version="2018.4.18f1", basePath = "build/unity", cleanup = true)
    Installation preInstalledUnity2018_4_18f1

    def "runs batchmode action"() {
        given: "path to future project"
        def project_path = "build/test_project"

        and: "a pre installed unity editor"
        environmentVariables.set("UNITY_PATH", unityPath)

        and: "a build script"
        buildFile << """
            group = 'test'
            ${applyPlugin(UnityPlugin)}
         
            task mUnity {
                doLast {
                    unity.batchMode {
                        args "-createProject", "${project_path}"
                    }
                }
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("mUnity")

        then:
        result.standardOutput.contains("Starting process 'command '${unityPath}'")
        fileExists(project_path)

        where:
        unityPath = preInstalledUnity2018_4_18f1.executable.path
    }

    def "runs batchmode task"() {
        given: "path to future project"
        def project_path = "build/test_project"

        and: "a pre installed unity editor"
        environmentVariables.set("UNITY_PATH", unityPath)

        and: "a build script"
        buildFile << """
            group = 'test'
            ${applyPlugin(UnityPlugin)}
         
            task (mUnity, type: wooga.gradle.unity.tasks.Unity) {
                args "-createProject", "${project_path}"
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("mUnity")

        then:
        result.standardOutput.contains("Starting process 'command '${unityPath}'")
        fileExists(project_path)

        where:
        unityPath = preInstalledUnity2018_4_18f1.executable.path
    }
}
