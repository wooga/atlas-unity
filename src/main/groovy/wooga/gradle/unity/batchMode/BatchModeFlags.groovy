/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package wooga.gradle.unity.batchMode

/**
 * Constant class with Unity commandline switches.
 */
class BatchModeFlags {

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
    static String BATCH_MODE = "-batchmode"

    /**
     * Open the project at the given path.
     * <p>
     * {code -projectPath <pathname>}
     */
    static String PROJECT_PATH = "-projectPath"

    /**
     * Specify where the Editor or Windows/Linux/OSX standalone log file are written.
     * <p>
     * {@code -logFile <pathname>}
     */
    static String LOG_FILE = "-logFile"

    /**
     * Allows the selection of an active build target before a project is loaded.
     * <p>
     * {@code -buildTarget <name>}
     * @see BuildTarget
     */
    static String BUILD_TARGET = "-buildTarget"

    /**
     * Quit the Unity Editor after other commands have finished executing.
     * <p>
     * Note that this can cause error messages to be hidden (however, they still appear in the Editor.log file).
     */
    static String QUIT = "-quit"

    /**
     * When running in batch mode, do not initialize the graphics device at all.
     * <p>
     * This makes it possible to run your automated workflows on machines that don’t even have a GPU
     * (automated workflows only work when you have a window in focus, otherwise you can’t send simulated input commands).
     * Please note that -nographics does not allow you to bake GI, since Enlighten requires GPU acceleration.
     */
    static String NO_GRAPHICS = "-nographics"

    /**
     * Create an empty project at the given path.
     * <p>
     * {@code -createProject <pathname>}
     */
    static String CREATE_PROJECT = "-createProject"

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
     * @see #BATCH_MODE
     * @see #QUIT
     */
    static String SERIAL = "-serial"

    /**
     * Enter a username into the log-in form during activation of the Unity Editor.
     * <p>
     * {@code -username <username>}
     */
    static String USER_NAME = "-username"

    /**
     * Enter a password into the log-in form during activation of the Unity Editor.
     * <p>
     * {@code -password <password>}
     */
    static String PASSWORD = "-password"

    /**
     * Return the currently active license to the license server.
     * <p>
     * Please allow a few seconds before the license file is removed,
     * because Unity needs to communicate with the license server.
     */
    static String RETURN_LICENSE = "-returnlicense"

    // Testing
    /**
     * Run Editor tests from the project.
     * <p>
     * This argument requires the projectPath, and quit is not required,
     * because the Editor automatically closes down after the run is finished.
     */
    static String RUN_TESTS = "-runTests"

    /**
     * Path where the result file should be placed.
     * <p>
     * {@code -editorTestsResultFile <path>}
     * If the path is a folder, a default file name is used. If not specified,
     * the results are placed in the project’s root folder.
     */
    static String TEST_RESULTS = "-testResults"

    /**
     * The testplatform to use for the tests.
     * <p>
     * {@code -testPlatform <platform>}
     * @see TestPlatform
     */
    static String TEST_PLATFORM = "-testPlatform"

    // Misc
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
    static String EXECUTE_METHOD = "-executeMethod"

    /**
     * Export a package, given a path (or set of given paths).
     * <p>
     * {@code -exportPackage <exportAssetPath1 exportAssetPath2 ExportAssetPath3 exportFileName>}
     * In this example exportAssetPath is a folder
     * (relative to to the Unity project root) to export from the Unity project, and exportFileName is the package name.
     * Currently, this option only exports whole folders at a time. This command normally needs
     * to be used with the {@code -projectPath} argument.
     */
    static String EXPORT_PACKAGE = "-exportPackage"

    private BatchModeFlags() {
        throw new AssertionError()
    }
}

/**
 * The build target values for Unity
 */
enum BuildTarget {
    undefined, win32, win64, osx, linux, linux64, ios, android, web, webstreamed, webgl, xboxone, ps4, psp2, wsaplayer, tizen, samsungtv
}

/**
 * The Unity test platform values.
 * <p>
 * Unity contains multiple test unit/integration test runner.
 * The @{code TestPlatform} value determines the which runner to invoke.
 */
enum TestPlatform {
    /**
     * Editmode tests are basic unit tests with no access or interaction with the Unity engine.
     */
    editmode,

    /**
     * Playmode tests will be executed within a running Unity engine.
     */
    playmode
}
