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

import nebula.test.IntegrationSpec
import spock.lang.Unroll

import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class AssembleResourcesIntegrationSpec extends IntegrationSpec {

    File iOSResourcebase
    File androidResourcebase


    File androidPlugins
    File iOSPlugins

    def setup() {
        androidPlugins = new File(projectDir, "Assets/Plugins/Android")
        iOSPlugins = new File(projectDir, "Assets/Plugins/iOS")

        def resourcesBase = new File(projectDir, "test/resources")
        iOSResourcebase = new File(resourcesBase, "iOS")
        androidResourcebase = new File(resourcesBase, "android")

        iOSResourcebase.mkdirs()
        androidResourcebase.mkdirs()
    }

    def createAARPackage(File path) {
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(path))
        out.putNextEntry(new ZipEntry("classes.jar"))
        out.putNextEntry(new ZipEntry("AndroidManifest.xml"))
        out.close()
    }


    def "skips copy tasks when no dependencies are set"() {
        given: "a build file without external dependencies"

        buildFile << """
            ${applyPlugin(wooga.gradle.unity.UnityPlugin)}
        """.stripIndent()

        when: "running the setup task"
        def result = runTasksSuccessfully(UnityPlugin.SETUP_TASK_NAME)

        then:
        result.wasExecuted(UnityPlugin.ASSEMBLE_RESOURCES_TASK_NAME)
        result.wasExecuted("assembleIOSResources")
        result.wasExecuted("assembleAndroidResources")
        !androidPlugins.list()
        !iOSPlugins.list()
    }

    def "syncs iOS resources when configured"() {
        given: "a test class to copy"
        createFile("WGTestClass.mm", iOSResourcebase)

        and: "a .framework mock"
        createFile("Test.framework", iOSResourcebase)

        and: "an empty output directory"
        assert !iOSPlugins.list()

        and: "a build file with artifact dependency to that file"
        buildFile << """
            ${applyPlugin(wooga.gradle.unity.UnityPlugin)}

            dependencies {
                ios fileTree(dir: "${iOSResourcebase.path.replace('\\', '/')}")
            }

        """.stripIndent()

        when: "running the setup task"
        runTasksSuccessfully(UnityPlugin.SETUP_TASK_NAME)

        then:
        !androidPlugins.list()
        iOSPlugins.list()
        iOSPlugins.list().contains('WGTestClass.mm')
        iOSPlugins.list().contains('Test.framework')
    }

    def "syncs android resources when configured"() {
        given: "a jar file mock to copy"
        createFile("WGDeviceInfo.jar", androidResourcebase)

        and: "an aar file mock"
        createAARPackage(createFile("WGDeviceInfo.aar", androidResourcebase))

        and: "an empty output directory"
        assert !androidPlugins.list()

        and: "a build file with artifact dependency to that file"
        buildFile << """
            ${applyPlugin(wooga.gradle.unity.UnityPlugin)}

            dependencies {
                android fileTree(dir: "${androidResourcebase.path.replace('\\', '/')}")
            }

        """.stripIndent()

        when: "running the setup task"
        runTasksSuccessfully(UnityPlugin.SETUP_TASK_NAME)

        then:
        !iOSPlugins.list()
        androidPlugins.list()
        androidPlugins.list().contains('WGDeviceInfo.jar')
        androidPlugins.list().contains('WGDeviceInfo.aar')
    }

    def "syncs android resources and unpacks aars"() {
        given: "a jar file mock to copy"
        createFile("WGDeviceInfo.jar", androidResourcebase)

        and: "a test aar package"
        createAARPackage(createFile("WGDeviceInfo.aar", androidResourcebase))

        and: "an empty output directory"
        assert !androidPlugins.list()

        and: "a build file with artifact dependency to that file"
        buildFile << """
            ${applyPlugin(wooga.gradle.unity.UnityPlugin)}

            unity.androidResourceCopyMethod = "arrUnpack"
            
            dependencies {
                android fileTree(dir: "${androidResourcebase.path.replace('\\', '/')}")
            }

        """.stripIndent()

        when: "running the setup task"
        runTasksSuccessfully(UnityPlugin.SETUP_TASK_NAME)

        then:
        def unpackedAARDir = new File(androidPlugins, "WGDeviceInfo")
        def libsDir = new File(androidPlugins, "libs")

        !iOSPlugins.list()
        androidPlugins.list()
        unpackedAARDir.exists()
        unpackedAARDir.list().contains("WGDeviceInfo.jar")
        unpackedAARDir.list().contains("AndroidManifest.xml")
        libsDir.exists()
        libsDir.list().contains("WGDeviceInfo.jar")
    }

    def "can set plugins folder in extension"() {
        given: "a jar file mock to copy"
        createFile("WGDeviceInfo.jar", androidResourcebase)

        and: "a custom plugins directory"
        androidPlugins = new File(projectDir, "Assets/Plugins/Custom/Android")

        and: "a build file with artifact dependency to that file"
        buildFile << """
            ${applyPlugin(wooga.gradle.unity.UnityPlugin)}

            unity.pluginsDir = "${androidPlugins.path.replace('\\', '/')}"
            
            dependencies {
                android fileTree(dir: "${androidResourcebase.parentFile.path.replace('\\', '/')}")
            }

        """.stripIndent()

        when: "running the setup task"
        runTasksSuccessfully(UnityPlugin.SETUP_TASK_NAME)

        then:
        androidPlugins.list()
    }

    @Unroll()
    def "clean deletes iOS and Android plugins dir with pluginsDir set to #pluginsDir"() {
        given: "a jar file mock to copy"
        createFile("WGDeviceInfo.jar", androidResourcebase)

        and: "a .framework mock"
        createFile("Test.framework", iOSResourcebase)

        and: "custom set pluginsDir"
        androidPlugins = new File(projectDir, pluginsDir + '/Android')
        iOSPlugins = new File(projectDir, pluginsDir + '/iOS')

        and: "a build file with artifact dependency to that file"
        buildFile << """
            ${applyPlugin(wooga.gradle.unity.UnityPlugin)}

            unity.pluginsDir = "${pluginsDir}"
            dependencies {
                ios fileTree(dir: "${iOSResourcebase.path.replace('\\', '/')}")
                android fileTree(dir: "${androidResourcebase.path.replace('\\', '/')}")
            }

        """.stripIndent()

        and: "and setup run"
        runTasksSuccessfully(UnityPlugin.SETUP_TASK_NAME)
        assert androidPlugins.list()
        assert iOSPlugins.list()

        when:
        runTasksSuccessfully('clean')

        then:
        !androidPlugins.exists()
        !iOSPlugins.exists()

        where:
        pluginsDir << ["Assets/Plugins", "Assets/Plugins/Custom"]
    }
}
