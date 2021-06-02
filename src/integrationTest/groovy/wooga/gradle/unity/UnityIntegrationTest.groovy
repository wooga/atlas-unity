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
import spock.lang.Shared
import wooga.gradle.IntegrationSpec
import wooga.gradle.unity.tasks.Unity
import wooga.gradle.unity.utils.ProjectSettingsFile

abstract class UnityIntegrationTest extends IntegrationSpec {

    File mockUnityFile
    final String mockUnityMessage = "Mock Unity Started"

    File unityMainDirectory
    File projectSettingsFile

    final String extensionName = UnityPlugin.getEXTENSION_NAME()
    final String groupName = "integrationTest"
    final String unityPathOverrideEnvVariable = "UNITY_PATH_TEST"

    UnityPluginTestOptions options
    Boolean initialized = false

    String getMockTaskName() {
        "unityIntegrationTest"
    }

    String getMockTaskTypeName() {
        Unity.class.name
    }

    // @TODO: This forces this version to be installed without prompting for any test run. Could we instead only try to install when its needed?
    @Shared
    @UnityInstallation(version = "2019.4.27f1", cleanup = false)
    Installation preInstalledUnity

    def setup() {
        setupUnityPluginImpl(true)
    }

    protected def setupUnityPlugin() {
        setupUnityPluginImpl(false)
    }

    private void setupUnityPluginImpl(Boolean fromSetup) {

        if (initialized) {
            return
        }

        if (!options) {
            options = new DefaultUnityPluginTestOptions()
        } else {
            println "Using option overrides ${options}"
        }

        if (fromSetup && !options.applyPlugin()) {
            println "Skipping Unity plugin setup..."
            return
        } else {
            applyUnityPlugin()
        }

        unityMainDirectory = projectDir

//        if (!isWindows) {
//            unityMainDirectory = new File(projectDir, "Unity/SomeLevel/SecondLevel")
//            unityMainDirectory.mkdirs()
//        }

        setProjectSettingsFile()
        setLicenseDirectory()

        if (options.addPluginTestDefaults()) {
            buildFile << """
            unity {
                 testBuildTargets = ["android"]
            }
            """.stripIndent()
        }

        if (options.disableAutoActivateAndLicense()) {
            buildFile << """
            unity {
                 autoReturnLicense = false
                 autoActivateUnity = false
            }
            """.stripIndent()
        }

        switch (options.unityPath()) {
            case UnityPathResolution.Mock:
                addMockUnityPath()
                break

            case UnityPathResolution.Default:
                addDefaultUnityPath()
                break
        }

        if (options.addMockTask()) {
            addMockTask(options.forceMockTaskRun(), options.clearMockTaskActions())
        }

        initialized = true
    }

    private void applyUnityPlugin() {
        buildFile << """
            group = '${groupName}'
            ${applyPlugin(UnityPlugin)}         
        """.stripIndent()
    }

    protected File setProjectSettingsFile(String content = ProjectSettingsFile.DEFAULT_TEMPLATE_CONTENT) {
        if (!projectSettingsFile){
            projectSettingsFile = createFile("ProjectSettings/ProjectSettings.asset")
        }
        else{
            projectSettingsFile.text = ""
        }
        projectSettingsFile << content
        projectSettingsFile
    }

    protected void addMockUnityPath() {

        mockUnityFile = createFile("fakeUnity.bat", unityMainDirectory)
        mockUnityFile.executable = true
        if (osName.contains("windows")) {
            mockUnityFile << """
                @echo off
                echo ${mockUnityMessage}              
                echo %*
            """.stripIndent()
        } else {
            mockUnityFile << """
                #!/usr/bin/env bash
                echo ${mockUnityMessage}
                echo \$@
            """.stripIndent()
        }
        addUnityPathToExtension(mockUnityFile.path)
    }

    void setLicenseDirectory() {
        // Setup fake license dir so we don't delete actual licenses
        def licenseDir = File.createTempDir("unity","testLicenseDir")
        createFile("testLicense", licenseDir)
        buildFile << """
        unity {
            licenseDirectory.set(new File("${escapedPath(licenseDir.path)}"))
        }
        """.stripIndent()
    }

    protected void addDefaultUnityPath() {
        addUnityPathToExtension(unityPath)
    }

    protected String getUnityPath() {
        def result = System.getenv().get(unityPathOverrideEnvVariable)
        if (result) {
            return result
        }
        return preInstalledUnity.executable.path
    }

    protected void addUnityPathToExtension(String path) {
        buildFile << "unity.unityPath = file(\"${escapedPath(path)}\")"
    }

    void addMockTask(Boolean force, Boolean clearActions, String... lines) {
        addTask(mockTaskName, mockTaskTypeName, force, lines)
        if (clearActions) {
            clearMockTaskActions()
        }
    }

    void clearMockTaskActions() {
        appendToMockTask("""
        doFirst {
            println "woo"
        }
        actions = [actions[0]]
        """.stripIndent())
    }

    void appendToMockTask(String... lines) {
        buildFile << """
        $mockTaskName {
            ${lines.join('\n')}
        }
        """.stripIndent()
    }

    def runTestTaskSuccessfully() {
        runTasksSuccessfully(mockTaskName)
    }

    void addMockTask(String name, String typeName, Boolean force, String... lines) {
        def originalTypeName = typeName
        typeName = "MockTestTask"
        buildFile << """
            class ${typeName} extends ${originalTypeName} {                   
                @Override
                void exec() {
                }
            }
            """.stripIndent()
        addTask(name, typeName, force, lines)
    }

    void addTask(String name, String typeName, Boolean force, String... lines) {
        lines = lines ?: []
        buildFile << """
        task (${name}, type: ${typeName}) {                       
            ${force ? "onlyIf = {true}\n" : ""}${lines.join('\n')}
        }
        """.stripIndent()
    }

    void appendToPluginExtension(String... lines) {
        StringBuilder builder = new StringBuilder()
        builder.append("unity {")
        lines.each { l -> builder.append("${l}\n") }
        builder.append("}")
        buildFile << """
            unity {
                defaultBuildTarget = "android"
            }
        """.stripIndent()
        buildFile << builder.toString()
    }

    void addProviderQueryTask(String name, String path, String invocation = ".getOrNull()") {
        buildFile << """
            task(${name}) {
                doLast {
                    def value = ${path}${invocation}
                    println("${path}: " + value)
                }
            }
        """
    }
}
