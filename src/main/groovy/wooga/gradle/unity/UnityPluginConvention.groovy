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

import wooga.gradle.unity.batchMode.BuildTarget
import wooga.gradle.unity.batchMode.TestPlatform

/**
 * A Unity Plugin convention object.
 */
interface UnityPluginConvention extends UnityActionConvention {

    /**
     * Returns a {@code File} path to the Unity license directory.
     * Default value is system depending.
     *
     * @return the path to the Unity license directory
     * @see UnityPluginConsts#UNITY_LICENSE_DIRECTORY_MAC_OS
     * @see UnityPluginConsts#UNITY_LICENSE_DIRECTORY_WIN
     */
    File getUnityLicenseDirectory()

    /**
     * Returns the {@code File} path to the reports output directory.
     * @default {@code buildDir/reports}
     * @return reports output directory path
     */
    File getReportsDir()

    /**
     * Sets the {@code File} path to the reports output directory.
     * @param reportsDir reports output directory
     */
    void setReportsDir(File reportsDir)

    /**
     * Sets the {@code File} path to the reports output directory.
     * @param reportsDir reports output directory
     */
    void setReportsDir(Object reportsDir)

    /**
     * Sets the {@code File} path to the reports output directory.
     * @param reportsDir reports output directory
     * @return this
     */
    UnityPluginConvention reportsDir(Object reportsDir)

    /**
     * Returns the {@code File} path to {@code Assets} directory.
     * @default {@code projectPath/Assets}
     * @return path to assets directory
     * @see #getProjectPath
     */
    File getAssetsDir()

    /**
     * Sets the {@code File} path to {@code Assets} directory.
     * @param path path to assets directory
     */
    void setAssetsDir(File path)

    /**
     * Sets the {@code File} path to {@code Assets} directory.
     * @param path path to assets directory
     * @return this
     */
    UnityPluginConvention assetsDir(Object path)

    /**
     * Returns the {@code File} path to the plugins directory
     * @default {@code assetsDir/Plugins}
     * @return path to plugins directory
     * @see #getAssetsDir
     */
    File getPluginsDir()

    /**
     * Sets the {@code File} path to {@code Plugins} directory.
     * @param path path to plugins directory
     */
    void setPluginsDir(File path)

    /**
     * Sets the {@code File} path to {@code Plugins} directory.
     * @param path path to plugins directory
     */
    UnityPluginConvention pluginsDir(Object path)

    /**
     * Returns the default build target.
     * <p>
     * The default {@code defaultBuildTarget} is applied to all tasks of type {@code AbstractUnityTask}.
     *
     * @return the default build target
     * @see wooga.gradle.unity.batchMode.BuildTarget* @see wooga.gradle.unity.tasks.internal.AbstractUnityTask
     */
    BuildTarget getDefaultBuildTarget()

    /**
     * Sets the default build target.
     * @param value an object that can be evaluated to a {@code BuildTarget} object
     */
    void setDefaultBuildTarget(Object value)

    /**
     * Sets the default build target.
     * @param value the new default buildtarget
     * @return this
     */
    UnityPluginConvention defaultBuildTarget(BuildTarget value)

    /**
     * Returns if batchmode should be enabled for edit mode tests
     * @return a{@code Boolean} value
     */
    Boolean getBatchModeForEditModeTest()

    /**
     * Sets the value if batchmode should be enabled for edit mode tests
     */
    void setBatchModeForEditModeTest(Boolean value)

    /**
     * Sets the value if batchmode should be enabled for edit mode tests
     */
    UnityPluginConvention batchModeForEditModeTest(Boolean value)

    /**
     * Returns if batchmode should be enabled for play mode tests
     * @return a{@code Boolean} value
     */
    Boolean getBatchModeForPlayModeTest()

    /**
     * Sets the value if batchmode should be enabled for play mode tests
     */
    void setBatchModeForPlayModeTest(Boolean value)

    /**
     * Sets the value if batchmode should be enabled for play mode tests
     */
    UnityPluginConvention batchModeForPlayModeTest(Boolean value)

    Boolean getBatchMode(TestPlatform testPlatform)
}
