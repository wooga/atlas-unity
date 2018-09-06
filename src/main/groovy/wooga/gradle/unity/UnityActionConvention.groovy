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
 * Base type for all unity action classes.
 * <p>
 * Every Unity Action/Task implements this interface to be able to set and retrieve
 * basic configuration properties
 */
interface UnityActionConvention {

    /**
     * Returns a {@code File} path to a Unity installation.
     * <p>
     * The value can be set in multiple ways (gradle properties, environment variable, parameter in code)
     * The precedence order is:
     * <ul>
     *    <li><b>direct parameter in code</b>
     *    <li><b>gradle properties</b>
     *    <li><b>environment variables</b>
     *    <li><b>hardcoded value</b>
     * </ul>
     *
     * The hardcoded default value is system dependent.
     *
     * @see UnityPluginConsts#UNITY_PATH_OPTION
     * @see UnityPluginConsts#UNITY_PATH_ENV_VAR
     * @see UnityPluginConsts#UNITY_PATH_MAC_OS
     * @see UnityPluginConsts#UNITY_PATH_MAC_OS
     * @see UnityPluginConsts#UNITY_PATH_WIN
     * @see UnityPluginConsts#UNITY_PATH_WIN_32
     * @see UnityPluginConsts#UNITY_PATH_LINUX
     * @return the path to unity executable
     */
    File getUnityPath()

    /**
     * Sets custom path to Unity executable.
     * @param path to Unity executable
     */
    void setUnityPath(File path)

    /**
     * Sets custom path to Unity executable.
     * @param path to Unity executable
     * @return this
     */
    UnityActionConvention unityPath(File path)

    /**
     * Returns the {@code File} path to the Unity project.
     * @default gradle {@code $projectDir}
     * @return the path to the unity project
     */
    File getProjectPath()

    /** Sets the path to the Unity project.
     * @param path unity project path
     * @return this
     */
    void setProjectPath(File path)

    /** Sets the path to the Unity project.
     * @param path unity project path
     * @return this
     */
    UnityActionConvention projectPath(File path)

    /**
     * Returns if the external unity command should redirect stdOut.
     * <p>
     * By default Unity logs all output into a logfile. This property determines if the Unity log output
     * will be redirected to stdout.
     * <p>
     * If a custom logFile is provided, the output will be logged to the stdout and the logfile.
     * This property works only on macOS.
     * @return true if log should be redirected to stdout
     * @default false
     */
    Boolean getRedirectStdOut()

    /**
     * Sets {@code Boolean} flag if Unity log should be redirected to stdout.
     * @param redirect {@code true} if Unity log should be redirected to stdout
     */
    void setRedirectStdOut(Boolean redirect)

    /**
     * Sets {@code Boolean} flag if Unity log should be redirected to stdout.
     * @param redirect {@code true} if Unity log should be redirected to stdout
     * @return this
     */
    UnityActionConvention redirectStdOut(Boolean redirect)

    /**
     * Returns the log category.
     * <p>
     * The log category property is used to instruct unity to log its output to specific sub directories in the logs folder.
     * This helper is intended for tools such as Jenkins which doesn't allow to override the file path when archiving artifacts.
     * <p>
     * The value can be set in multiple ways (gradle properties, environment variable, parameter in code)
     * The precedence order is:
     * <ul>
     *    <li><b>direct parameter in code</b>
     *    <li><b>gradle properties</b>
     *    <li><b>environment variables</b>
     *    <li><b>hardcoded value</b>
     * </ul>
     * @return a category value
     * @default ""
     * @see UnityPluginConsts#UNITY_LOG_CATEGORY_OPTION
     * @see UnityPluginConsts#UNITY_LOG_CATEGORY_ENV_VAR
     */
    String getLogCategory()

    /**
     * Sets the log category value
     * @param value the new logCategory. Can be {@code NULL}
     */
    void setLogCategory(String category)

    /**
     * Sets the log category value
     * @param value the new logCategory. Can be {@code NULL}
     * @return this
     */
    UnityActionConvention logCategory(String value)
}