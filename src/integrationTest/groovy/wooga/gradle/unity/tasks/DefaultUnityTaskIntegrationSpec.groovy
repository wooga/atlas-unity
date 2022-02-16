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

package wooga.gradle.unity.tasks

import com.wooga.spock.extensions.unity.UnityPathResolution
import com.wooga.spock.extensions.unity.UnityPluginTestOptions
import com.wooga.spock.extensions.uvm.UnityInstallation
import net.wooga.uvm.Installation
import spock.lang.Requires
import wooga.gradle.unity.UnityTaskIntegrationSpec

class DefaultUnityTaskIntegrationSpec extends UnityTaskIntegrationSpec<Unity> {

    @Requires({  os.macOs })
    @UnityPluginTestOptions(unityPath = UnityPathResolution.Default)
    @UnityInstallation(version = "2019.4.27f1", cleanup = false)
    def "creates unity project"(Installation unity) {
        given: "path to future project"
        def project_path = "build/test_project"

        and: "a pre installed unity editor"
        environmentVariables.set("UNITY_PATH", unity.getExecutable().getPath())

        and: "a build script"
        appendToSubjectTask("createProject = \"${project_path}\"",
                // We need to select a valid build target before loading
                "buildTarget = \"Android\"")

        when:
        def result = runTasksSuccessfully(subjectUnderTestName)

        then:
        result.standardOutput.contains("Starting process 'command '${unity.getExecutable().getPath()}'")
        fileExists(project_path)
        fileExists(project_path, "Assets")
        fileExists(project_path, "Library")
        fileExists(project_path, "ProjectSettings")
    }
}
