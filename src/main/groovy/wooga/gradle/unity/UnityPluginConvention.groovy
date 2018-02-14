package wooga.gradle.unity

import wooga.gradle.unity.batchMode.BuildTarget

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
     * @see wooga.gradle.unity.batchMode.BuildTarget
     * @see wooga.gradle.unity.tasks.internal.AbstractUnityTask
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
}