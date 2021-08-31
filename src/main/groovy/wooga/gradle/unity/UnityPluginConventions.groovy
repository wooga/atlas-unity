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

import com.wooga.gradle.PropertyLookup
import wooga.gradle.unity.utils.PlatformUtils
import wooga.gradle.unity.utils.PlatformUtilsImpl
import wooga.gradle.unity.utils.UnityFileTree

class UnityPluginConventions implements PlatformUtilsImpl {

    /**
     * {@code File} to Unity executable on macOS.
     * @value "/Applications/Unity/Unity.app/Contents/MacOS/Unity"
     */
    static File UNITY_PATH_MAC_OS = new File("/Applications/Unity/Unity.app/Contents/MacOS/Unity")

    /**
     * {@code File} to Unity executable on windows 64bit.
     * @value "C:\Program Files\Unity\Editor\Unity.exe"
     */
    static File UNITY_PATH_WIN = new File("C:\\Program Files\\Unity\\Editor\\Unity.exe")

    /**
     * {@code File} to Unity executable on windows 32bit.
     * @value "C:\Program Files (x86)\Unity\Editor\Unity.exe"
     */
    static File UNITY_PATH_WIN_32 = new File("C:\\Program Files (x86)\\Unity\\Editor\\Unity.exe")

    /**
     * {@code File} to Unity executable on linux.
     * @value "/opt/Unity/Editor/Unity"
     */
    static File UNITY_PATH_LINUX = new File("/opt/Unity/Editor/Unity")

    /**
     * {@code File} to Unity license directory on macOS.
     * @value "/Library/Application Support/Unity/"
     */
    static File UNITY_LICENSE_DIRECTORY_MAC_OS = new File("/Library/Application Support/Unity/")

    /**
     * {@code File} to Unity license directory on Linux.
     * @value "/Library/Application Support/Unity/"
     */
    static File UNITY_LICENSE_DIRECTORY_LINUX = new File("${PlatformUtils.unixUserHomePath}/share/unity3d/Unity/")

    /**
     * {@code File} to Unity license directory on Windows.
     * @value "C:\ProgramData\Unity"
     */
    static File UNITY_LICENSE_DIRECTORY_WIN = new File("C:\\ProgramData\\Unity")

    /**
     * The major default Unity version in use
     */
    static final PropertyLookup unityVersion = new PropertyLookup("2019.4")

    /**
     * The path to the Unity Editor executable
     */
    static final PropertyLookup unityPath = new PropertyLookup(["UNITY_UNITY_PATH", "UNITY_PATH"], "unity.unityPath", { getPlatformUnityPath().absolutePath })    /**
    /**
     * Used for authentication with Unity's servers
     */
    static final PropertyLookup user = new PropertyLookup(["UNITY_USR", "UNITY_AUTHENTICATION_USERNAME"], "unity.authentication.username", null)
    /**
     * Used for authentication with Unity's servers
     */
    static final PropertyLookup password = new PropertyLookup("UNITY_PWD", "unity.authentication.password", null)
    /**
     * Used for authentication with Unity's servers
     */
    static final PropertyLookup serial = new PropertyLookup("UNITY_AUTHENTICATION_SERIAL", "unity.authentication.serial", null)
    /**
     * Used in specifying the directory where unity logs are written to
     *
     * @environmentVariable "XCODEBUILD_LOGS_DIR"
     * @propertyName "xcodebuild.logsDir"
     * @defaultValue "logs"
     */
    static final PropertyLookup logDirectory = new PropertyLookup("UNITY_LOG_DIR", "unity.logDir", "logs")
    /**
     * If provided, a sub-directory within the main logs directory
     */
    static final PropertyLookup logCategory = new PropertyLookup("UNITY_LOG_CATEGORY", "unity.logCategory", "unity")
    /**
     * Whether batchmode should be set automatically for edit mode tests
     */
    static final PropertyLookup batchModeForEditModeTest = new PropertyLookup("UNITY_BATCH_MODE_FOR_EDIT_MODE_TEST", "unity.batchModeForEditModeTest", true)
    /**
     * Whether batchmode should be set automatically for play mode tests
     */
    static final PropertyLookup batchModeForPlayModeTest = new PropertyLookup("UNITY_BATCH_MODE_FOR_PLAY_MODE_TEST", "unity.batchModeForPlayModeTest", true)
    /**
     * The default unity test version
     */
    static final PropertyLookup defaultUnityTestVersion = new PropertyLookup("UNITY_DEFAULT_UNITY_TEST_VERSION", "defaultUnityTestVersion", "2019")
    /**
     * Targets to generate tests for
     */
    static final PropertyLookup testBuildTargets = new PropertyLookup("UNITY_TEST_BUILD_TARGETS", "unity.testBuildTargets", null)
    /**
     * Targets to generate tests for
     */
    static final PropertyLookup enableTestCodeCoverage = new PropertyLookup("UNITY_ENABLE_TEST_COVERAGE", "unity.enableTestCodeCoverage", false)

    /**
     * The path to the Unity license directory
     */
    static File getLicenseDirectory() {
        File licensePath

        if (isWindows()) {
            licensePath = UnityPluginConventions.UNITY_LICENSE_DIRECTORY_WIN
        } else if (isMac()) {
            licensePath = UnityPluginConventions.UNITY_LICENSE_DIRECTORY_MAC_OS
        } else {
            licensePath = UnityPluginConventions.UNITY_LICENSE_DIRECTORY_LINUX

        }

        licensePath
    }

    /**
     * Returns
     * @return the default location for platform specific Unity installation
     */
    static File getPlatformUnityPath() {
        File unityPath = null
        if (isWindows()) {
            if (is64BitArchitecture()) {
                unityPath = UnityPluginConventions.UNITY_PATH_WIN
            } else {
                unityPath = UnityPluginConventions.UNITY_PATH_WIN_32
            }
        } else if (isLinux()) {
            unityPath = UnityPluginConventions.UNITY_PATH_LINUX
        } else if (isMac()) {
            unityPath = UnityPluginConventions.UNITY_PATH_MAC_OS
        }
        unityPath
    }

    static UnityFileTree getUnityFileTree(File unityExec) {
        return UnityFileTree.fromUnityExecutable(unityExec)
     }

}




