/*
 * Copyright 2021 Wooga GmbH
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

import nebula.test.ProjectSpec
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import spock.lang.Unroll
import wooga.gradle.unity.internal.DefaultUnityPluginExtension
import wooga.gradle.unity.tasks.AddUPMPackages
import wooga.gradle.unity.tasks.GenerateSolution

class UnityPluginTest extends ProjectSpec {

    public static final String PLUGIN_NAME = UnityPlugin.PLUGIN_NAME

    def 'creates the [unity] extension'() {
        given:
        assert !project.plugins.hasPlugin(PLUGIN_NAME)
        assert !project.extensions.findByName(UnityPlugin.EXTENSION_NAME)

        when:
        project.plugins.apply(PLUGIN_NAME)

        then:
        def extension = project.extensions.findByName(UnityPlugin.EXTENSION_NAME)
        extension instanceof DefaultUnityPluginExtension
    }

    @Unroll("creates the task #taskName")
    def 'Creates needed tasks'(String taskName, Class taskType) {
        given:
        assert !project.plugins.hasPlugin(PLUGIN_NAME)
        assert !project.tasks.findByName(taskName)

        when:
        project.plugins.apply(PLUGIN_NAME)

        then:
        def task = project.tasks.findByName(taskName)
        taskType.isInstance(task)

        where:
        taskName                           | taskType
        UnityPlugin.Tasks.test             | DefaultTask
        UnityPlugin.Tasks.testEditMode     | DefaultTask
        UnityPlugin.Tasks.testPlayMode     | DefaultTask
        UnityPlugin.Tasks.generateSolution | GenerateSolution
        UnityPlugin.Tasks.addUPMPackages   | AddUPMPackages
    }

    @Unroll
    def "configures addUPMPackages task"() {
        given: "project without applied atlas-unity plugin"
        assert !project.plugins.hasPlugin(PLUGIN_NAME)

        when: "applying atlas unity plugin"
        project.plugins.apply(PLUGIN_NAME)
        and: "configured unity extension"
        project.extensions.unity.with {
            upmPackages = extUPMPackages
            enableTestCodeCoverage = coverageEnabled
        }

        then:
        def task = project.tasks.findByName(UnityPlugin.Tasks.addUPMPackages.toString()) as AddUPMPackages
        def packages = task.upmPackages.get()
        and: "task has a manifest file"
        task.manifestPath.present
        task.manifestPath.get().asFile == new File(projectDir, "Packages/manifest.json")
        and: "task contains expected packages"
        packages.entrySet().containsAll(extUPMPackages.entrySet())
        if (coverageEnabled) {
            packages["com.unity.testtools.codecoverage"] == "1.1.0"
            packages.size() == extUPMPackages.size() + 1
        } else {
            packages.size() == extUPMPackages.size()
        }

        where:
        coverageEnabled | extUPMPackages
        true            | [:]
        false           | [:]
        false           | ["unity.pkg": "ver", "otherpkg": "0.0.1"]
        true            | ["unity.pkg": "ver", "otherpkg": "0.0.1"]
    }

}
