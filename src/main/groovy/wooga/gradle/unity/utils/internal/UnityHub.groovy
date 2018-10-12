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

import groovy.json.JsonSlurper

class UnityHub {

    static HUB_INSTALL_LOCATION = "secondaryInstallPath.json"
    static HUB_MANUAL_EDITORS = "editors.json"
    static HUB_DEFAULT_EDITOR = "defaultEditor.json"

    static File DEFAULT_UNITY_HUB_INSTALL_LOCATION_MAC_OS() {
        new File("/Applications/Unity/Hub/Editor")
    }

    static File DEFAULT_UNITY_HUB_INSTALL_LOCATION_WINDOWS() {
        new File("${System.env.get('ProgramFiles')}\\Unity\\Hub\\Editor")
    }

    private static File UNITY_HUB_CONFIG_PATH_MAC_OS() {
        new File("${System.env.get('HOME')}/Library/Application Support/UnityHub")
    }

    private static File UNITY_HUB_CONFIG_PATH_WINDOWS() {
        new File("${System.env.get('HOMEPATH')}\\AppData\\Roaming\\UnityHub")
    }

    static File getConfigLocation() {
        File unityHubConfigPath = null
        String osName = System.getProperty("os.name").toLowerCase()
        if (osName.contains("windows")) {
            unityHubConfigPath = UNITY_HUB_CONFIG_PATH_WINDOWS()
        } else if (osName.contains("mac os x")) {
            unityHubConfigPath = UNITY_HUB_CONFIG_PATH_MAC_OS()
        }
        unityHubConfigPath
    }

    static File getDefaultInstallLocation() {
        File unityHubDefaultInstallLocation = null
        String osName = System.getProperty("os.name").toLowerCase()
        if (osName.contains("windows")) {
            unityHubDefaultInstallLocation = DEFAULT_UNITY_HUB_INSTALL_LOCATION_WINDOWS()
        } else if (osName.contains("mac os x")) {
            unityHubDefaultInstallLocation = DEFAULT_UNITY_HUB_INSTALL_LOCATION_MAC_OS()
        }
        unityHubDefaultInstallLocation
    }

    static File unitySytemBinaryPath(File basePath) {
        File unityBinary = null
        String osName = System.getProperty("os.name").toLowerCase()
        if (osName.contains("windows")) {
            unityBinary = basePath
        } else if (osName.contains("mac os x")) {
            unityBinary = new File(basePath, "Contents/MacOS/Unity")
        }
        unityBinary
    }

    static String getUnitySytemAppName() {
        String appName = "Unity"
        String osName = System.getProperty("os.name").toLowerCase()
        if (osName.contains("windows")) {
            appName = "${appName}.exe"
        } else if (osName.contains("mac os x")) {
            appName = "${appName}.app"
        }
        appName
    }


    static boolean getIsAvailable() {
        return configLocation && configLocation.exists()
    }

    static File getHubInstallPath() {
        File installPath = null
        if (isAvailable) {
            def installHubPath = new File(configLocation, HUB_INSTALL_LOCATION)
            def path = new JsonSlurper().parseText(installHubPath.text) as String
            installPath = (path == "") ? defaultInstallLocation : new File(path)

        }
        installPath
    }

    static Map<String, File> getInstalledEditors() {
        getManualInstalledEditors() + getHubInstalledEditors()
    }

    static Map<String, File> getManualInstalledEditors() {
        def editors = new HashMap<String, File>()
        if (isAvailable) {
            def editorsConfigPath = new File(configLocation, HUB_MANUAL_EDITORS)
            if(editorsConfigPath.exists()) {
                def rawEditors = new JsonSlurper().parseText(editorsConfigPath.text) as Map<String, Object>
                editors = rawEditors.collectEntries(editors) { k, v ->
                    [k, unitySytemBinaryPath(new File(v['location'][0] as String))]
                }
            }
        }
        editors
    }

    static Map<String, File> getHubInstalledEditors() {
        def editors = new HashMap<String, File>()
        if (isAvailable) {
            if (hubInstallPath && hubInstallPath.exists()) {
                hubInstallPath.eachDir { File dir ->
                    def unityPath = new File(dir, unitySytemAppName)
                    if (unityPath.exists()) {
                        def version = dir.name
                        editors[version] = unitySytemBinaryPath(unityPath)
                    }
                }
            }
        }
        editors
    }

    static String getDefaultEditorVersion() {
        def defaultEditor = null
        if (isAvailable) {
            def defaultEditorPath = new File(configLocation, HUB_DEFAULT_EDITOR)
            if(defaultEditorPath.exists()) {
                defaultEditorPath = new File(configLocation, HUB_DEFAULT_EDITOR)
                defaultEditor = new JsonSlurper().parse(defaultEditorPath) as String
            }
        }

        defaultEditor
    }

    static File getDefaultEditor() {
        def defaultEditorVersion = defaultEditorVersion
        if (defaultEditorVersion) {
            return installedEditors[defaultEditorVersion]
        }
        null
    }
}
