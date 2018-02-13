package wooga.gradle.unity

import wooga.gradle.unity.batchMode.BuildTarget

trait UnityActionConvention<T extends UnityPluginConvention> {

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
    File unityPath

    /**
     * Sets custom path to Unity executable.
     * @param path to Unity executable
     * @return this
     */
    abstract T unityPath(Object path)

    /**
     * Sets custom path to Unity executable.
     * @param path to Unity executable
     * @return this
     */
    abstract T unityPath(File path)

    /**
     * Returns the {@code File} path to the Unity project.
     * @default gradle {@code $projectDir}
     * @return the path to the unity project
     */
    File projectPath

    /** Sets the path to the Unity project.
     * @param path unity project path
     * @return this
     */
    abstract T projectPath(File path)

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
    Boolean redirectStdOut

    /**
     * Sets {@code Boolean} flag if Unity log should be redirected to stdout.
     * @param redirect {@code true} if Unity log should be redirected to stdout
     * @return this
     */
    abstract T redirectStdOut(Boolean redirect)

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
    String logCategory

    /**
     * Sets the log category value
     * @param value the new logCategory. Can be {@code NULL}
     * @return this
     */
    abstract T logCategory(String value)
}


/**
 * A Unity Plugin convention object.
 */
trait UnityPluginConvention<T extends UnityPluginConvention> extends UnityActionConvention {

    /**
     * Returns a {@code File} path to the Unity license directory.
     * Default value is system depending.
     *
     * @return the path to the Unity license directory
     * @see UnityPluginConsts#UNITY_LICENSE_DIRECTORY_MAC_OS
     * @see UnityPluginConsts#UNITY_LICENSE_DIRECTORY_WIN
     */
    abstract File getUnityLicenseDirectory()

    /**
     * Returns the {@code File} path to the reports output directory.
     * @default {@code buildDir/reports}
     * @return reports output directory path
     */
    File reportsDir

    /**
     * Sets the {@code File} path to the reports output directory.
     * @param reportsDir reports output directory
     */
    abstract void setReportsDir(Object reportsDir)

    /**
     * Sets the {@code File} path to the reports output directory.
     * @param reportsDir reports output directory
     * @return this
     */
    abstract T reportsDir(Object reportsDir)

    /**
     * Returns the {@code File} path to {@code Assets} directory.
     * @default {@code projectPath/Assets}
     * @return path to assets directory
     * @see #getProjectPath
     */
    File assetsDir

    /**
     * Sets the {@code File} path to {@code Assets} directory.
     * @param path path to assets directory
     * @return this
     */
    abstract T assetsDir(Object path)

    /**
     * Returns the {@code File} path to the plugins directory
     * @default {@code assetsDir/Plugins}
     * @return path to plugins directory
     * @see #getAssetsDir
     */
    File pluginsDir

    /**
     * Sets the {@code File} path to {@code Plugins} directory.
     * @param path path to plugins directory
     */
    abstract T pluginsDir(Object path)

    /**
     * Returns the default build target.
     * <p>
     * The default {@code defaultBuildTarget} is applied to all tasks of type {@code AbstractUnityTask}.
     *
     * @return the default build target
     * @see wooga.gradle.unity.batchMode.BuildTarget
     * @see wooga.gradle.unity.tasks.internal.AbstractUnityTask
     */
    BuildTarget defaultBuildTarget

    /**
     * Sets the default build target.
     * @param value an object that can be evaluated to a {@code BuildTarget} object
     */
    abstract void setDefaultBuildTarget(Object value)

    /**
     * Sets the default build target.
     * @param value the new default buildtarget
     * @return this
     */
    abstract T defaultBuildTarget(BuildTarget value)
}