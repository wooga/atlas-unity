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

package wooga.gradle.unity.utils.internal

import org.junit.Rule
import org.junit.contrib.java.lang.system.EnvironmentVariables
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll


class UnityHubSpec extends Specification {

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables()

    @Shared
    File hubBasePath

    @Shared
    File secondaryInstallPath

    @Shared
    File manualInstallPath

    @Shared
    File defaultEditorConfig

    @Shared
    File editorsConfig

    @Shared
    File secondaryInstallPathConfig

    def setup() {
        environmentVariables.set("HOME", File.createTempDir().path)
        environmentVariables.set("HOMEPATH", File.createTempDir().path)
        secondaryInstallPath = File.createTempDir()
        manualInstallPath = File.createTempDir()

        hubBasePath = UnityHub.getConfigLocation()
    }

    def setupUnityHubConfiguration() {
        hubBasePath.mkdirs()
        editorsConfig = new File(hubBasePath, UnityHub.HUB_MANUAL_EDITORS)
        editorsConfig.createNewFile()

        defaultEditorConfig = new File(hubBasePath, UnityHub.HUB_DEFAULT_EDITOR)
        defaultEditorConfig.createNewFile()

        secondaryInstallPathConfig = new File(hubBasePath, UnityHub.HUB_INSTALL_LOCATION)
        secondaryInstallPathConfig.createNewFile()

        editorsConfig.text = "{}"
        defaultEditorConfig.text = ""
        secondaryInstallPathConfig.text = "\"${secondaryInstallPath.path}\""
    }

    File mockUnityInstallation(File basePath, String version) {
        def versionPath = new File(basePath, "${version}/${UnityHub.unitySytemAppName}")

        if (!versionPath.exists()) {
            versionPath.mkdirs()
            versionPath.createNewFile()
        }

        versionPath
    }

    @Unroll("isAvailable returns #expectedResult if unity hub path #message")
    def "getIsAvailable"() {
        given: "unity hub configuration"
        if (unityHubExists) {
            setupUnityHubConfiguration()
        }

        expect:
        UnityHub.isAvailable == expectedResult

        where:
        unityHubExists | expectedResult
        true           | true
        false          | false
        message = (unityHubExists) ? "exists" : "does not exist"
    }

    @Unroll("hubInstallPath returns #locationMessage when unity hub path #message")
    def "getHubInstallPath"() {
        given: "unity hub configuration"
        if (unityHubExists) {
            setupUnityHubConfiguration()
            secondaryInstallPathConfig.text = "\"${configuredPath}\""
        }

        expect:
        UnityHub.hubInstallPath == expectedResult

        where:
        unityHubExists | configuredPath | expectedResult                  | locationMessage
        true           | "/custom/path" | new File("/custom/path")        | "path to configured install location"
        true           | ''             | UnityHub.defaultInstallLocation | "path to default install location"
        false          | ''             | null                            | "null"
        message = (unityHubExists) ? "exists" : "does not exist"

    }

    @Unroll("manualInstalledEditors returns #mapMessage when #whenMessage")
    def "getManualInstalledEditors"() {
        given: "unity hub configuration"
        if (unityHubExists) {
            setupUnityHubConfiguration()
        }

        and: "some configured unity editor versions"
        def version = "2017.1.0f3"
        if (unityHubExists && hasInstallation) {

            def location = mockUnityInstallation(manualInstallPath, version)

            editorsConfig.text = """
            {
                "${version}": {
                    "version":"${version}",
                    "location": ["${location.path}"],
                    "manual":true
                }
            }
            """.stripIndent().trim()
        }

        expect:
        def editors = UnityHub.manualInstalledEditors
        editors.size() == expectedSize
        if (hasInstallation) {
            editors[version] == UnityHub.unitySytemBinaryPath(mockUnityInstallation(manualInstallPath, version))
        } else {
            editors[version] == null
        }

        where:
        unityHubExists | expectedSize | hasInstallation | mapMessage                                               | whenMessage
        true           | 1            | true            | "Map with manual configured unity installation location" | "unity hub exists and unity editor is installed"
        true           | 0            | false           | "empty Map"                                              | "unity hub exists and no unity editor is installed"
        false          | 0            | false           | "empty Map"                                              | "unity hub doesn't exist"
    }

    @Unroll("hubInstalledEditors returns #mapMessage when #whenMessage")
    def "getHubInstalledEditors"() {
        given: "unity hub configuration"
        if (unityHubExists) {
            setupUnityHubConfiguration()
        }

        and: "some configured unity editor versions"
        def version = "2017.1.0f3"
        if (unityHubExists && hasInstallation) {
            mockUnityInstallation(secondaryInstallPath, version)
        }

        expect:
        def editors = UnityHub.hubInstalledEditors
        editors.size() == expectedSize
        if (hasInstallation) {
            editors[version] == UnityHub.unitySytemBinaryPath(mockUnityInstallation(secondaryInstallPath, version))
        } else {
            editors[version] == null
        }

        where:
        unityHubExists | expectedSize | hasInstallation | mapMessage                                           | whenMessage
        true           | 1            | true            | "Map with hub installed unity installation location" | "unity hub exists and unity editor is installed"
        true           | 0            | false           | "empty Map"                                          | "unity hub exists and no unity editor is installed"
        false          | 0            | false           | "empty Map"                                          | "unity hub doesn't exist"
    }

    @Unroll("installedEditors returns #mapMessage when #whenMessage")
    def "getInstalledEditors"() {
        given: "unity hub configuration"
        if (unityHubExists) {
            setupUnityHubConfiguration()
        }

        and: "manual unity editor versions"
        def manualVersion = "2017.1.0f3"
        if (unityHubExists && hasManualInstallation) {
            def location = mockUnityInstallation(manualInstallPath, manualVersion)

            editorsConfig.text = """
            {
                "${manualVersion}": {
                    "version":"${manualVersion}",
                    "location": ["${location.path}"],
                    "manual":true
                }
            }
            """.stripIndent().trim()
        }

        and: "hub unity editor versions"
        def hubVersion = "2018.1.0f3"
        if (unityHubExists && hasHubInstallation) {
            mockUnityInstallation(secondaryInstallPath, hubVersion)
        }

        expect:
        def editors = UnityHub.installedEditors
        editors.size() == expectedSize
        if (hasHubInstallation) {
            editors[hubVersion] == UnityHub.unitySytemBinaryPath(mockUnityInstallation(secondaryInstallPath, hubVersion))
        } else {
            editors[hubVersion] == null
        }

        if (hasManualInstallation) {
            editors[manualVersion] == UnityHub.unitySytemBinaryPath(mockUnityInstallation(manualInstallPath, manualVersion))
        } else {
            editors[manualVersion] == null
        }

        where:
        unityHubExists | expectedSize | hasManualInstallation | hasHubInstallation | mapMessage                                                      | whenMessage
        true           | 2            | true                  | true               | "Map with hub and manual installed unity installation location" | "unity hub exists and unity editor is installed"
        true           | 1            | false                 | true               | "Map with hub installed unity installation location"            | "unity hub exists and unity editor is installed"
        true           | 1            | true                  | false              | "Map with manual configured unity installation location"        | "unity hub exists and unity editor is installed"
        true           | 0            | false                 | false              | "empty Map"                                                     | "unity hub exists and no unity editor is installed"
        false          | 0            | false                 | false              | "empty Map"                                                     | "unity hub doesn't exist"
    }

    @Unroll("defaultEditorVersion returns #resultMessage when #whenMessage")
    def "getDefaultEditorVersion"() {
        given: "unity hub configuration"
        if (unityHubExists) {
            setupUnityHubConfiguration()
        }

        and: "a active unity version"
        if (hasDefaultVersion) {
            defaultEditorConfig.text = "\"2018.1.0f3\""
        } else {
            defaultEditorConfig.delete()
        }

        expect:
        UnityHub.defaultEditorVersion == expectedResult

        where:
        unityHubExists | expectedResult | hasDefaultVersion | resultMessage    | whenMessage
        true           | "2018.1.0f3"   | true              | "active version" | "unity hub exists and default version is set"
        true           | null           | false             | "null"           | "unity hub exists and no default version is set"
        false          | null           | false             | "null"           | "unity hub doesn't exist"

    }

    @Unroll("defaultEditor returns #resultMessage when #whenMessage")
    def "getDefaultEditor"() {
        given: "unity hub configuration"
        if (unityHubExists) {
            setupUnityHubConfiguration()
        }

        and: "manual unity editor versions"
        if (unityHubExists && hasManualInstallation) {
            def location = mockUnityInstallation(manualInstallPath, hasManualInstallation)

            editorsConfig.text = """
            {
                "${hasManualInstallation}": {
                    "version":"${hasManualInstallation}",
                    "location": ["${location.path}"],
                    "manual":true
                }
            }
            """.stripIndent().trim()
        }

        and: "hub unity editor versions"
        if (unityHubExists && hasHubInstallation) {
            mockUnityInstallation(secondaryInstallPath, hasHubInstallation)
        }

        and: "a active unity version"
        if (hasDefaultVersion) {
            defaultEditorConfig.text = "\"${hasDefaultVersion}\""
        } else {
            defaultEditorConfig.delete()
        }

        expect:
        def result = UnityHub.defaultEditor
        if (expectedVersion) {
            result.path.contains(expectedVersion)
        } else {
            result == null
        }

        where:
        unityHubExists | expectedVersion | hasDefaultVersion | hasManualInstallation | hasHubInstallation | resultMessage                             | whenMessage
        true           | "2018.1.0f3"    | "2018.1.0f3"      | "2018.1.0f3"          | "2017.1.0f3"       | "path to active manual installed version" | "unity hub exists and default version is set to manual installation"
        true           | "2017.2.0f3"    | "2017.2.0f3"      | "2018.2.0f3"          | "2017.2.0f3"       | "path to active hub installed version"    | "unity hub exists and default version is set to hub installation"
        true           | null            | null              | "2018.2.0f3"          | "2017.2.0f3"       | "null"                                    | "unity hub exists and no default version is set"
        true           | null            | "2017.4.0f3"      | "2018.2.0f3"          | "2017.3.0f3"       | "null"                                    | "unity hub exists and default version is set to unknown version"
        false          | null            | null              | null                  | null               | "null"                                    | "unity hub doesn't exist"
    }
}
