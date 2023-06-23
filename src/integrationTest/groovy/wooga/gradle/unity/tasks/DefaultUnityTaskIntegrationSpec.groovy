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

import com.wooga.gradle.PropertyUtils
import com.wooga.gradle.test.PropertyLocation
import com.wooga.gradle.test.writers.PropertyGetterTaskWriter
import com.wooga.gradle.test.writers.PropertySetterWriter
import com.wooga.spock.extensions.unity.UnityPathResolution
import com.wooga.spock.extensions.unity.UnityPluginTestOptions
import com.wooga.spock.extensions.uvm.UnityInstallation
import net.wooga.uvm.Installation
import spock.lang.Requires
import spock.lang.Unroll
import wooga.gradle.unity.UnityTaskIntegrationSpec
import wooga.gradle.unity.models.UnityCommandLineOption

class DefaultUnityTaskIntegrationSpec extends UnityTaskIntegrationSpec<Unity> {

    @Requires({ os.macOs })
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

    // Options that accept arguments
    @Unroll
    def "can set command line argument option #option when #location with key #keyString"() {

        when:
        def query = runPropertyQuery(getter, setter)

        then:
        query.success
        query.matches(value, type)

        where:
        [testCase, value, location] <<
            [
                [
                    UnityCommandLineOption.projectPath,
                    UnityCommandLineOption.createProject,
                    UnityCommandLineOption.userName,
                    UnityCommandLineOption.password,
                    UnityCommandLineOption.testCategory,
                    UnityCommandLineOption.testFilter,
                    UnityCommandLineOption.testPlatform,
                    UnityCommandLineOption.assemblyNames,
                    UnityCommandLineOption.testResults,
                    UnityCommandLineOption.playerHeartbeatTimeout,
                    UnityCommandLineOption.executeMethod,
                    UnityCommandLineOption.exportPackage,
                    UnityCommandLineOption.coverageHistoryPath,
                    UnityCommandLineOption.cacheServerEndPoint,
                    UnityCommandLineOption.cacheServerNamespacePrefix,
                    UnityCommandLineOption.cacheServerIPAddress,
                ]
                    .collect({ it -> [it.toString()] }),
                ["foobar"],
                [PropertyLocation.property, PropertyLocation.environment]
            ].combinations()

        option = testCase[0]
        type = String.class
        propertyKey = "${extensionName}." + option
        envKey = PropertyUtils.envNameFromProperty(propertyKey)
        keyString = location == PropertyLocation.property
            ? propertyKey : envKey

        setter = new PropertySetterWriter(subjectUnderTestName, option)
            .set(value, type)
            .withPropertyKey(propertyKey)
            .withEnvironmentKey(envKey)
            .to(location)
        getter = new PropertyGetterTaskWriter(setter)
    }

    // Options that are flags
    @Unroll
    def "can set command line flag option #option to #value when #location with key #keyString"() {

        when:
        def query = runPropertyQuery(getter, setter)

        then:
        query.success
        query.matches(value, type)

        where:
        [testCase, value, location] <<
            [
                [
                    UnityCommandLineOption.noGraphics,
                    UnityCommandLineOption.serial,
                    UnityCommandLineOption.returnLicense,
                    UnityCommandLineOption.forgetProjectPath,
                    UnityCommandLineOption.runTests,
                    UnityCommandLineOption.serial,
                    UnityCommandLineOption.runSynchronously,
                    UnityCommandLineOption.serial,
                    UnityCommandLineOption.disableAssemblyUpdater,
                    UnityCommandLineOption.deepProfiling,
                    UnityCommandLineOption.enableCacheServer,
                ]
                    .collect({ it -> [it.toString()] }),
                [true, false],
                [PropertyLocation.property, PropertyLocation.environment]
            ].combinations()

        option = testCase[0]
        type = Boolean.class
        propertyKey = "${extensionName}." + option
        envKey = PropertyUtils.envNameFromProperty(propertyKey)
        keyString = location == PropertyLocation.property
            ? propertyKey : envKey

        setter = new PropertySetterWriter(subjectUnderTestName, option)
            .set(value, type)
            .withPropertyKey(propertyKey)
            .withEnvironmentKey(envKey)
            .to(location)
        getter = new PropertyGetterTaskWriter(setter)
    }

    // Options that are (boolean) flags but need to be passed as arguments
    @Unroll
    def "can set command line argument boolean option #option when #location with key #keyString to #value"() {

        when:
        def query = runPropertyQuery(getter, setter)

        then:
        query.success
        query.matches(value, String)

        where:
        [testCase, value, location] <<
            [
                [
                    UnityCommandLineOption.cacheServerEnableDownload,
                    UnityCommandLineOption.cacheServerEnableUpload,
                ]
                    .collect({ it -> [it.toString()] }),
                [true, false],
                [PropertyLocation.property, PropertyLocation.environment, PropertyLocation.script]
            ].combinations()

        option = testCase[0]
        propertyKey = "${extensionName}." + option
        envKey = PropertyUtils.envNameFromProperty(propertyKey)
        keyString = location == PropertyLocation.property
            ? propertyKey : envKey

        setter = new PropertySetterWriter(subjectUnderTestName, option)
            .set(value, Boolean)
            .withPropertyKey(propertyKey)
            .withEnvironmentKey(envKey)
            .to(location)

        getter = new PropertyGetterTaskWriter(setter)
    }
}
