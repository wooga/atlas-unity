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

class BatchModeFlags {
    static String BATCH_MODE = "-batchmode"
    static String PROJECT_PATH = "-projectPath"
    static String LOG_FILE = "-logFile"
    static String BUILD_TARGET = "-buildTarget"
    static String QUIT = "-quit"
    static String NO_GRAPHICS = "-nographics"

    static String CREATE_PROJECT = "-createProject"

    // Editor Test Flags
    static String RUN_EDITOR_TESTS = "-runEditorTests"
    static String EDITOR_TEST_VERBOSE_LOG = "-editorTestsVerboseLog"
    static String EDITOR_TEST_CATEGORIES = "-editorTestsCategories"
    static String EDITOR_TEST_FILTER = "-editorTestsFilter"
    static String EDITOR_TEST_RESULTS_FILE = "-editorTestsResultFile"

    // Unit Testing >5.6

    static String RUN_TESTS = "-runTests"
    static String TEST_RESULTS = "-testResults"
    static String TEST_PLATFORM = "-testPlatform"

    static String EXECUTE_METHOD = "-executeMethod"
    static String EXPORT_PACKAGE = "-exportPackage"

    private BatchModeFlags() {
        throw new AssertionError()
    }
}

enum BuildTarget {
    undefined, win32, win64, osx, linux, linux64, ios, android, web, webstreamed, webgl, xboxone, ps4, psp2, wsaplayer, tizen, samsungtv
}

enum TestPlatform {
    editmode, playmode
}