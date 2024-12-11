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

import com.wooga.gradle.PlatformUtils
import com.wooga.gradle.test.IntegrationSpec
import com.wooga.gradle.test.executable.FakeExecutables
import com.wooga.gradle.test.mock.MockExecutable
import com.wooga.spock.extensions.unity.DefaultUnityPluginTestOptions
import com.wooga.spock.extensions.unity.UnityPathResolution
import com.wooga.spock.extensions.unity.UnityPluginTestOptions
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.ClosureSignatureHint
import groovy.transform.stc.FirstParam
import groovy.transform.stc.FromString
import wooga.gradle.unity.tasks.Unity
import wooga.gradle.unity.utils.ProjectSettingsFile

import java.util.function.Function

abstract class UnityIntegrationSpec extends IntegrationSpec {

    MockExecutable mockUnityFile = createMockUnity()
    final String mockUnityStartupMessage = "Mock Unity Started"

    File unityMainDirectory
    File projectSettingsFile

    static final String extensionName = UnityPlugin.EXTENSION_NAME
    final String groupName = "integrationTest"
    final String unityPathOverrideEnvVariable = "UNITY_PATH_TEST"

    UnityPluginTestOptions options
    Boolean initialized = false

    String getSubjectUnderTestName() {
        "unityIntegrationTest"
    }

    String getSubjectUnderTestTypeName() {
        Unity.class.name
    }

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
                if (options.writeMockExecutable()) {
                    writeMockExecutable()
                }
                break

            case UnityPathResolution.Default:
                break
        }

        if (options.addMockTask()) {
            addMockTask(options.forceMockTaskRun(), options.clearMockTaskActions())
        }

        initialized = true
    }

    /**
     * Writes the mock executable with a predetermined location
     */
    protected void writeMockExecutable(@ClosureParams(value= FromString, options = "com.wooga.gradle.test.mock.MockExecutable")
                                           Closure<MockExecutable> configure = null) {
        mockUnityFile = createMockUnity()
        if (configure != null) {
            configure(mockUnityFile)
        }
        def file = mockUnityFile.toDirectory(unityMainDirectory)
        addUnityPathToExtension(file.path)
    }

    private void applyUnityPlugin() {
        buildFile << """
            group = '${groupName}'
            ${applyPlugin(UnityPlugin)}         
        """.stripIndent()
    }

    protected File setProjectSettingsFile(String content = ProjectSettingsFile.DEFAULT_TEMPLATE_CONTENT) {
        if (!projectSettingsFile) {
            projectSettingsFile = createFile("ProjectSettings/ProjectSettings.asset")
        } else {
            projectSettingsFile.text = ""
        }
        projectSettingsFile << content
        projectSettingsFile
    }

    protected MockExecutable createMockUnity(String extraLog = null, int exitValue=0) {

        def mockFile = new MockExecutable("fakeUnity.bat")
        mockFile.withText(mockUnityStartupMessage)
        mockFile.withExitValue(exitValue)
        if (extraLog != null) {
            mockFile.text += "\n${extraLog? extraLog.readLines().collect{"echo $it"}.join("\n") : ""}"
        }
        return mockFile
    }

    void setLicenseDirectory() {
        // Setup fake license dir so we don't delete actual licenses
        def licenseDir = File.createTempDir("unity", "testLicenseDir")
        createFile("testLicense", licenseDir)
        buildFile << """
        unity {
            licenseDirectory.set(new File("${PlatformUtils.escapedPath(licenseDir.path)}"))
        }
        """.stripIndent()
    }

    protected String getUnityPath() {
        def result = System.getenv().get(unityPathOverrideEnvVariable)
        if (result) {
            return result
        }
    }

    /**
     * @return Generates a file in the test project directory
     */
    def projectFile(String... path) {
        file(path.join(File.separator), projectDir)
    }

    protected void addUnityPathToExtension(String path) {
        buildFile << "unity.unityPath = file(\"${PlatformUtils.escapedPath(path)}\")"
    }

    void addMockTask(Boolean force, Boolean clearActions, String... lines) {
        addTask(subjectUnderTestName, subjectUnderTestTypeName, force, lines)
        if (clearActions) {
            clearSubjectTaskActions()
        }
    }

    void clearSubjectTaskActions() {
        appendToSubjectTask("""
        doFirst {
            println "woo"
        }
        actions = [actions[0]]
        """.stripIndent())
    }

    void appendToSubjectTask(String... lines) {
        buildFile << """
        $subjectUnderTestName {
            ${lines.join('\n')}
        }
        """.stripIndent()
    }

    def runTestTaskSuccessfully() {
        runTasksSuccessfully(subjectUnderTestName)
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

    // TODO: Consider adding to gradle-commons-test
    /**
     * Set a task dependency where A depends on B
     */
    void setTaskDependency(String a, String b) {
        buildFile << """
${a} dependsOn ${b}
"""
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

    /**
     * Sets the unity version parsed by the plugin
     * @param version
     */
    void setUnityTestVersion(String version) {
        createFile("gradle.properties") << "defaultUnityTestVersion=${version}"
    }
}
