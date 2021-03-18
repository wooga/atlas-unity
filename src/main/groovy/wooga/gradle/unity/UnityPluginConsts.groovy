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

/**
 * Unity Plugin constants
 * <p>
 * This class contains only static constants we use in the plugin.
 */
class UnityPluginConsts {
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
     * {@code File} to Unity license directory on windows.
     * @value "C:\ProgramData\Unity"
     */
    static File UNITY_LICENSE_DIRECTORY_WIN = new File("C:\\ProgramData\\Unity")

    /**
     * Gradle property name to set the default value for {@code unityPath}.
     * @value "unity.path"
     * @see UnityPluginConvention#unityPath
     */
    static final String UNITY_PATH_OPTION = "unity.path"


    /**
     * Environment variable name to set the default value for {@code unityPath}.
     * @value "UNITY_PATH"
     * @see UnityPluginConvention#unityPath
     */
    static final String UNITY_PATH_ENV_VAR = "UNITY_PATH"

    /**
     * Gradle property name to set the default value for {@code logCategory}.
     * @value "unity.logCategory"
     * @see UnityPluginConvention#logCategory
     */
    static final String UNITY_LOG_CATEGORY_OPTION = "unity.logCategory"

    /**
     * Environment variable name to set the default value for {@code logCategory}.
     * @value "UNITY_LOG_CATEGORY"
     * @see UnityPluginConvention#logCategory
     */
    static final String UNITY_LOG_CATEGORY_ENV_VAR = "UNITY_LOG_CATEGORY"

    /**
     * Gradle property name to set the default value for {@code redirectStdOut}.
     * @value "unity.redirectStdout"
     * @see UnityPluginConvention#redirectStdOut
     */
    static final String REDIRECT_STDOUT_OPTION = "unity.redirectStdout"

    /**
     * Environment variable name to set the default value for {@code redirectStdOut}.
     * @value "UNITY_REDIRECT_STDOUT"
     * @see UnityPluginConvention#redirectStdOut
     */
    static final String REDIRECT_STDOUT_ENV_VAR = "UNITY_REDIRECT_STDOUT"

    /**
     * Gradle property name to set the default value for {@code batchModeForEditModeTest}.
     * @value "unity.batchModeForEditModeTest"
     * @see UnityPluginConvention#batchModeForEditModeTest
     */
    static final String BATCH_MODE_FOR_EDIT_MODE_TEST_OPTION = "unity.batchModeForEditModeTest"

    /**
     * Environment variable name to set the default value for {@code batchModeForEditModeTest}.
     * @value "UNITY_BATCH_MODE_FOR_EDIT_MODE_TEST"
     * @see UnityPluginConvention#batchModeForEditModeTest
     */
    static final String BATCH_MODE_FOR_EDIT_MODE_TEST_ENV_VAR = "UNITY_BATCH_MODE_FOR_EDIT_MODE_TEST"

    /**
     * Gradle property name to set the default value for {@code batchModeForPlayModeTest}.
     * @value "unity.batchModeForPlayModeTest"
     * @see UnityPluginConvention#batchModeForPlayModeTest
     */
    static final String BATCH_MODE_FOR_PLAY_MODE_TEST_OPTION = "unity.batchModeForPlayModeTest"

    /**
     * Environment variable name to set the default value for {@code batchModeForPlayModeTest}.
     * @value "UNITY_BATCH_MODE_FOR_PLAY_MODE_TEST"
     * @see UnityPluginConvention#batchModeForPlayModeTest
     */
    static final String BATCH_MODE_FOR_PLAY_MODE_TEST_ENV_VAR = "UNITY_BATCH_MODE_FOR_PLAY_MODE_TEST"

    /**
     * Gradle property name to set the default value for {@code authentication.username}.
     * @value "unity.username"
     * @see UnityAuthentication#username
     */
    static final String UNITY_USER_PROPERTY = "unity.username"

    /**
     * Environment variable name to set the default value for {@code authentication.username}.
     * @value "UNITY_USR"
     * @see UnityAuthentication#username
     */
    static final String UNITY_USER_ENV = "UNITY_USR"

    /**
     * Gradle property name to set the default value for {@code authentication.password}.
     * @value "unity.password"
     * @see UnityAuthentication#password
     */
    static final String UNITY_PASSWORD_PROPERTY = "unity.password"

    /**
     * Environment variable name to set the default value for {@code authentication.password}.
     * @value "UNITY_PWD"
     * @see UnityAuthentication#password
     */
    static final String UNITY_PASSWORD_ENV = "UNITY_PWD"

    /**
     * Gradle property name to set the default value for {@code authentication.serial}.
     * @value "unity.serial"
     * @see UnityAuthentication#serial
     */
    static final String UNITY_SERIAL_PROPERTY = "unity.serial"

    /**
     * Environment variable name to set the default value for {@code authentication.serial}.
     * @value "UNITY_SERIAL"
     * @see UnityAuthentication#serial
     */
    static final String UNITY_SERIAL_ENV = "UNITY_SERIAL"

    static final String UNITY_API_COMPATIBILITY_LEVEL_OPTION = "unity.apiCompatibilityLevel"
    static final String UNITY_API_COMPATIBILITY_LEVEL_ENV_VAR = "UNITY_API_COMPATIBILITY_LEVEL"

}
