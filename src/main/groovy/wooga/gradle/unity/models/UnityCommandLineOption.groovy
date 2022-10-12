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

package wooga.gradle.unity.models

/**
 * You can run the Unity Editor and build Unity applications with additional commands and information on startup.
 * These are some of the command line options available.
 */
enum UnityCommandLineOption {

    /**
     * Run Unity in batch mode.
     * <p>
     * This should always be used in conjunction with the other command line arguments,
     * because it ensures no pop-up windows appear and eliminates the need for any human intervention.
     * When an exception occurs during execution of the script code, the Asset server updates fail,
     * or other operations that fail, Unity immediately exits with return code 1.
     * Note that in batch mode, Unity sends a minimal version of its log output to the console. However,
     * the Log Files still contain the full log information.
     * Opening a project in batch mode while the Editor has the same project open is not supported;
     * only a single instance of Unity can run at a time.
     */
    batchMode("-batchmode", false, false),
    /**
     * Open the project at the given path.
     * <p>
     *{code -projectPath <pathname>}*/
    projectPath("-projectPath", true),

    /**
     * Specify where the Editor or Windows/Linux/OSX standalone log file are written.
     * <p>
     * {@code -logFile <pathname>}
     */
    logFile("-logFile", true, false),

    /**
     * Allows the selection of an active build target before a project is loaded.
     * <p>
     * {@code -buildTarget <name>}
     * @see BuildTarget
     */
    buildTarget("-buildTarget", UnityCommandLineOptionType.Argument, false),

    /**
     * Quit the Unity Editor after other commands have finished executing.
     * <p>
     * Note that this can cause error messages to be hidden (however, they still appear in the Editor.log file).
     */
    quit("-quit", false, false),
    /**
     * When running in batch mode, do not initialize the graphics device at all.
     * <p>
     * This makes it possible to run your automated workflows on machines that don’t even have a GPU
     * (automated workflows only work when you have a window in focus, otherwise you can’t send simulated input commands).
     * Please note that -nographics does not allow you to bake GI, since Enlighten requires GPU acceleration.
     */
    noGraphics("-nographics"),

    /**
     * Create an empty project at the given path.
     * <p>
     * {@code -createProject <pathname>}
     */
    createProject("-createProject", true),

    // Authentication
    /**
     * Activate Unity with the specified serial key.
     * <p>
     * {@code -serial <serial>}
     * It is good practice to pass the {@code -batchmode} and {@code -quit} arguments as well,
     * in order to quit Unity when done, if using this for automated activation of Unity.
     * Please allow a few seconds before the license file is created, because Unity needs to communicate with
     * the license server. Make sure that license file folder exists, and has appropriate permissions
     * before running Unity with this argument. If activation fails, see the {@code Editor.log} for info.
     *
     * @see #batchMode
     * @see #quit
     */
    serial("-serial", true),

    /**
     * Enter a username into the log-in form during activation of the Unity Editor.
     * <p>
     * {@code -username <username>}
     */
    userName("-username", true),

    /**
     * Enter a password into the log-in form during activation of the Unity Editor.
     * <p>
     * {@code -password <password>}
     */
    password("-password", true),

    /**
     * Return the currently active license to the license server.
     * <p>
     * Please allow a few seconds before the license file is removed,
     * because Unity needs to communicate with the license server.
     */
    returnLicense("-returnlicense"),

    // Testing commands provided by the Unity Test Framework
    // https://docs.unity3d.com/Packages/com.unity.test-framework@1.1/manual/reference-command-line.html

    /**
     * Don't save your current Project into the Unity launcher/hub history.
     */
    forgetProjectPath("-forgetProjectPath"),

    /**
     * Run Editor tests from the project.
     * <p>
     * This argument requires the projectPath, and quit is not required,
     * because the Editor automatically closes down after the run is finished.
     */
    runTests("-runTests"),

    /**
     * A semicolon-separated list of test categories to include in the run. A semi-colon separated list
     * should be formatted as a string enclosed in quotation marks, e.g. testCategory "firstCategory;secondCategory".
     * If using both testFilter and testCategory, then only tests that match both are run.
     * This argument supports negation using '!'.
     * If using '!MyCategory' then no tests with the 'MyCategory' category will be included in the run.
     */
    testCategory("-testCategory", true),

    /**
     * A semicolon-separated list of test names to run, or a regular expression pattern
     * to match tests by their full name. A semi-colon separated list should be formatted
     * as a string enclosed in quotation marks, e.g. testFilter "Low;Medium".
     * This argument supports negation using '!'. If using the test filter '!MyNamespace.Something.MyTest',
     * then all tests except that test will be run.
     */
    testFilter("-testFilter", true),

    /**
     * The platform to run tests on. Accepted values:
     * - EditMode: Edit Mode tests. Equivalent to running tests from the EditMode tab of the Test Runner window.
     * - PlayMode: Play Mode tests that run in the Editor. Equivalent to running tests from the PlayMode tab
     *             of the Test Runner window.
     * - Any value from the BuildTarget enum: Play Mode tests that run on a player built for the specified platform.
     *   Equivalent to using the Run all tests (<target_platform>) dropdown in the PlayMode tab of the Test Runner window.
     * Note: If no value is specified for this argument, tests run in Edit Mode.
     */
    testPlatform("-testPlatform", true),

    /**
     * A semicolon-separated list of test assemblies to include in the run. A semi-colon separated list
     * should be formatted as a string enclosed in quotation marks, e.g. assemblyNames "firstAssembly;secondAssembly"
     */
    assemblyNames("-assemblyNames", true),

    /**
     * Path where the result file should be placed.
     * <p>
     * {@code -editorTestsResultFile <path>}
     * If the path is a folder, a default file name is used. If not specified,
     * the results are placed in the project’s root folder.
     */
    testResults("-testResults", true),

    /**
     * The time, in seconds, the editor should wait for heartbeats after starting a test run on a player.
     * This defaults to 10 minutes.
     */
    playerHeartbeatTimeout("-playerHeartbeatTimeout", true),

    /**
     * If included, the test run will run tests synchronously, guaranteeing that all tests runs in one editor
     * update call. Note that this is only supported for EditMode tests, and that tests which take multiple frames
     * (i.e. [UnityTest] tests, or tests with [UnitySetUp] or [UnityTearDown] scaffolding) will be filtered out.
     */
    runSynchronously("-runSynchronously"),

    // General

    /**
     * Execute the static method as soon as Unity is started,
     * the project is open and after the optional Asset server update has been performed.
     * <p>
     * {@code -executeMethod <ClassName.MethodName>}
     * This can be used to do tasks such as continous integration, performing Unit Tests,
     * making builds or preparing data. To return an error from the command line process,
     * either throw an exception which causes Unity to exit with return code 1, or call {@code EditorApplication.Exit}
     * with a non-zero return code. To pass parameters, add them to the command line and retrieve them inside the
     * function using {@code System.Environment.GetCommandLineArgs}. To use @{code -executeMethod}, you need to place
     * the enclosing script in an Editor folder. The method to be executed must be defined as static.
     */
    executeMethod("-executeMethod", true),

    /**
     * Export a package, given a path (or set of given paths).
     * <p>
     * {@code -exportPackage <exportAssetPath1 exportAssetPath2 ExportAssetPath3 exportFileName>}
     * In this example exportAssetPath is a folder
     * (relative to to the Unity project root) to export from the Unity project, and exportFileName is the package name.
     * Currently, this option only exports whole folders at a time. This command normally needs
     * to be used with the {@code -projectPath} argument.
     */
    exportPackage("-exportPackage", true),
    /**
     * Specify a space-separated list of assembly names as parameters for Unity to ignore on automatic updates.
     The space-separated list of assembly names is optional: pass the command line options without any assembly names
     to ignore all assemblies.
     <p>Example 1
     unity.exe -disable-assembly-updater
     <p>Example 2
     unity.exe -disable-assembly-updater A1.dll subfolder/A2.dll
     <p>(Example 2 has two assembly names, one with a pathname. Example 2 ignores A1.dll, no matter what folder it is stored
     in, and ignores A2.dll only if it is stored under subfolder folder)

     <p>If you list an assembly in the -disable-assembly-updater command line parameter (or if you don’t specify assemblies),
     Unity logs the following message to Editor.log:
     [Assembly Updater] warning: Ignoring assembly [assembly_path] as requested by command line parameter.”).

     <p>Use this to avoid unnecessary API Updater overheads when importing assemblies.
     It is useful for importing assemblies which access a Unity API when you know the Unity API doesn’t need updating.
     It is also useful when importing assemblies which do not access Unity APIs at all (for example, if you have built
     your source code, or some of it, outside of Unity, and you want to import the resulting assemblies into your Unity project).

     <p>Note: If you disable the update of any assembly that does need updating, you may get errors at compile time,
     run time, or both, that are hard to track.
     */
    disableAssemblyUpdater("-disable-assembly-updater"),
    /**
     * Enable Deep Profiling option for the CPU profiler.
     */
    deepProfiling("-deepprofiling"),
    /**
     * Enables code coverage and allows access to the Coverage API.
     */
    enableCodeCoverage("-enableCodeCoverage", false, false),
    /**
     * Sets the location where the coverage results and report will be saved to.
     * The default location is the project's path.
     * https://docs.unity3d.com/Packages/com.unity.testtools.codecoverage@1.1/manual/CoverageBatchmode.html
     */
    coverageResultsPath("-coverageResultsPath", true, false),
    /**
     * Sets the location where the coverage report history will be saved to.
     * The default location is the project's path.
     * https://docs.unity3d.com/Packages/com.unity.testtools.codecoverage@1.1/manual/CoverageBatchmode.html
     */
    coverageHistoryPath("-coverageHistoryPath", true),
    /**
     *  Passes extra options. This is semicolon separated.
     * https://docs.unity3d.com/Packages/com.unity.testtools.codecoverage@1.1/manual/CoverageBatchmode.html
     */
    coverageOptions("-coverageOptions", true, false),
    /**
     * Enables debug code optimization mode, overriding the current default code optimization mode for the session.
     * Needed for code coverage on 2020.1 and older versions.
     */
    debugCodeOptimization("-debugCodeOptimization", false, false),

    /**
     * Tells Unity to use the newer Accelerator Cache Server. You must also use -cacheServerEndpoint to specify the address.
     * If you haven’t configured the Accelerator or cache server in Project Settings, you must add -adb2 on the command line.
     */
    enableCacheServer("-EnableCacheServer"),
    /**
     * Specifies the endpoint address if you are using the newer Accelerator Cache Server.
     * <p>Example: -cacheServerEndpoint 127.0.0.1:10080. This overrides any configuration stored in the Editor Preferences.
     * <p>Use this to connect multiple instances of Unity to different Cache Servers.
     */
    cacheServerEndPoint("-cacheServerEndpoint", true),
    /**
     * Set the namespace prefix for the newer Accelerator Cache Server. Used to group data together on the Cache Server.
     * <p>Example: -cacheServerNamespacePrefix MyProject
     */
    cacheServerNamespacePrefix("-cacheServerNamespacePrefix", true),
    /**
     * Enable downloading from the newer Accelerator Cache Server.
     * <p>Example:-cacheServerEnableDownload true
     */
    cacheServerEnableDownload("-cacheServerEnableDownload", true),
    /**
     * Enable uploading to the newer Accelerator Cache Server.
     * <p>Example:-cacheServerEnableUpload false
     */
    cacheServerEnableUpload("--cacheServerEnableUpload", true),
    /**
     * Enables usage of the older (v1) Cache Server, and specifies the IP Address to connect to on startup.
     * This overrides any configuration stored in the Editor Preferences.
     * Use this to connect multiple instances of Unity to different v1 Cache Servers.
     */
    cacheServerIPAddress("-CacheServerIPAddress", true)

    private final String flag
    private final String environmentKey
    private final Boolean map
    private final UnityCommandLineOptionType type

    /**
     * @return The type of this option
     */
    UnityCommandLineOptionType getType() {
        type
    }

    /**
     * @return Whether this option has arguments
     */
    Boolean getHasArguments() {
        type == UnityCommandLineOptionType.Argument
    }

    /**
     * @return The name of the option as seen in the shell; as -$NAME
     */
    String getFlag() {
        flag
    }

    /**
     * @return The name of the environment key, such as UNITY_FOOBAR
     */
    String getEnvironmentKey() {
        environmentKey
    }

    /**
     * @return Whether this option should be automatically mapped to environment/gradle properties
     */
    Boolean getMap() {
        map
    }

    /**
     * Options which require an argument
     */
    static List<UnityCommandLineOption> argumentFlags = values().findAll { it -> it.hasArguments }
    /**
     * Options which require no argument, act as switches when present
     */
    static List<UnityCommandLineOption> flags = values().findAll { it -> !it.hasArguments }

    UnityCommandLineOption(String flag, Boolean hasArguments, Boolean map = true) {
        this.flag = flag
        this.type = hasArguments
            ? UnityCommandLineOptionType.Argument
            : UnityCommandLineOptionType.Flag
        this.environmentKey = null
        this.map = map
    }

    UnityCommandLineOption(String flag) {
        this.flag = flag
        this.type = UnityCommandLineOptionType.Flag
        this.environmentKey = null
        this.map = true
    }

    UnityCommandLineOption(String flag, String environmentKey) {
        this.flag = flag
        this.type = UnityCommandLineOptionType.Flag
        this.environmentKey = environmentKey
    }

    UnityCommandLineOption(String flag, UnityCommandLineOptionType type, Boolean map) {
        this.flag = flag
        this.type = type
        this.map = map
    }
}
/**
 * The type of command line option
 */
enum UnityCommandLineOptionType {
    Argument,
    Flag
}

