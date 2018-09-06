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

import wooga.gradle.unity.batchMode.BatchModeFlags

class ExportPackageIntegrationSpec extends UnityIntegrationSpec {
    def "skips with no-source when input files are empty"() {
        given: "a build script with fake test unity location"
        buildFile << """
            task (mUnity, type: wooga.gradle.unity.tasks.UnityPackage)
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("mUnity")

        then:
        result.standardOutput.contains(":mUnity NO-SOURCE") || result.standardOutput.contains("Skipping task ':mUnity' as it has no source files")
        !result.standardOutput.contains(BatchModeFlags.EXPORT_PACKAGE)
        !result.standardOutput.contains("${moduleName}.unitypackage")
    }

    def "calls unity with -export-package and relative input sources as tree"() {
        given: "a fake file"
        def assetsDirString = "Assets/Test"
        def assetsDir = directory(assetsDirString)
        createFile("fakeFile.cs", assetsDir)
        createFile("fakeFile2.cs", assetsDir)

        and: "a build script with fake test unity location"
        buildFile << """
            task (mUnity, type: wooga.gradle.unity.tasks.UnityPackage) {
                inputFiles(fileTree(dir: 'Assets/Test', include: '**/*.cs'))
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("mUnity")

        then:
        result.standardOutput.contains(BatchModeFlags.EXPORT_PACKAGE + " " + assetsDirString)
        result.standardOutput.contains("${moduleName}.unitypackage")
    }

    def "calls unity with -export-package and relative input sources as file tree and removes file duplications"() {
        given: "a fake file"
        def assetsDirString = "Assets/Test"
        def assetsDirString2 = "Assets/Test2"
        def assetsDir = directory(assetsDirString)
        def assetsDir2 = directory(assetsDirString2)
        createFile("fakeFile.cs", assetsDir)
        createFile("fakeFile2.cs", assetsDir)

        createFile("fakeFile.cs", assetsDir2)
        createFile("fakeFile2.cs", assetsDir2)

        and: "a build script with fake test unity location"
        buildFile << """
            task (mUnity, type: wooga.gradle.unity.tasks.UnityPackage) {
                inputFiles(fileTree(dir: 'Assets', include: '**/*.cs'))
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("mUnity")

        then:
        result.standardOutput.contains(BatchModeFlags.EXPORT_PACKAGE + " " + assetsDirString + " " + assetsDirString2)
        result.standardOutput.contains("${moduleName}.unitypackage")
    }

    def "calls unity with -export-package and relative input sources as file collection and removes file duplications"() {
        given: "a fake file"
        def assetsDirString = "Assets/Test"
        def assetsDirString2 = "Assets/Test2"
        def assetsDir = directory(assetsDirString)
        def assetsDir2 = directory(assetsDirString2)
        File fake1 = createFile("fakeFile.cs", assetsDir)
        File fake2 = createFile("fakeFile2.cs", assetsDir)

        File fake3 = createFile("fakeFile.cs", assetsDir2)
        File fake4 = createFile("fakeFile2.cs", assetsDir2)

        and: "a build script with fake test unity location"
        buildFile << """
            task (mUnity, type: wooga.gradle.unity.tasks.UnityPackage) {
                inputFiles file("${escapedPath(fake1.path)}")
                inputFiles file("${escapedPath(fake2.path)}")
                inputFiles file("${escapedPath(fake3.path)}")
                inputFiles file("${escapedPath(fake4.path)}")
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("mUnity")

        then:
        result.standardOutput.contains(BatchModeFlags.EXPORT_PACKAGE + " " + assetsDirString + " " + assetsDirString2)
        result.standardOutput.contains("${moduleName}.unitypackage")
    }

    def "calls unity with -export-package and relative input sources as file collection directories and removes file duplications"() {
        given: "a fake file"
        def assetsDirString = "Assets/Test"
        def assetsDirString2 = "Assets/Test2"
        def assetsDir = directory(assetsDirString)
        def assetsDir2 = directory(assetsDirString2)

        and: "a build script with fake test unity location"
        buildFile << """
            task (mUnity, type: wooga.gradle.unity.tasks.UnityPackage) {
                inputFiles file("${escapedPath(assetsDir.path)}")
                inputFiles file("${escapedPath(assetsDir2.path)}")
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("mUnity")

        then:
        result.standardOutput.contains(BatchModeFlags.EXPORT_PACKAGE + " " + assetsDirString + " " + assetsDirString2)
        result.standardOutput.contains("${moduleName}.unitypackage")
    }

    def "archive name can be changed"() {
        given: "a fake file"
        def assetsDirString = "Assets/Test"
        def assetsDir = directory(assetsDirString)
        createFile("fakeFile.cs", assetsDir)
        createFile("fakeFile2.cs", assetsDir)

        and: "a build script with fake test unity location"
        buildFile << """
            task (mUnity, type: wooga.gradle.unity.tasks.UnityPackage) {
                inputFiles(fileTree(dir: 'Assets/Test', include: '**/*.cs'))
                archiveName = "myPackage.zip"
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("mUnity")
        then:
        !result.standardOutput.contains("mUnity-unspecified.unitypackage")
        result.standardOutput.contains("myPackage.zip")
    }

    def "archive name can be changed by setting archive name values"() {
        given: "a fake file"
        def assetsDirString = "Assets/Test"
        def assetsDir = directory(assetsDirString)
        createFile("fakeFile.cs", assetsDir)
        createFile("fakeFile2.cs", assetsDir)

        and: "a build script with fake test unity location"
        buildFile << """
            task (mUnity, type: wooga.gradle.unity.tasks.UnityPackage) {
                inputFiles(fileTree(dir: 'Assets/Test', include: '**/*.cs'))
                baseName = "wooga"
                appendix = "test"
                version = "0.4.1"
                extension = "bar"
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("mUnity")
        then:
        !result.standardOutput.contains("mUnity-unspecified.unitypackage")
        result.standardOutput.contains("wooga-test-0.4.1.bar")
    }

    def "archive name takes project version by default"() {
        given: "a fake file"
        def assetsDirString = "Assets/Test"
        def assetsDir = directory(assetsDirString)
        createFile("fakeFile.cs", assetsDir)
        createFile("fakeFile2.cs", assetsDir)

        and: "a build script with fake test unity location"
        buildFile << """
            version = "3.0.0"
            task (mUnity, type: wooga.gradle.unity.tasks.UnityPackage) {
                inputFiles(fileTree(dir: 'Assets/Test', include: '**/*.cs'))
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("mUnity")
        then:
        !result.standardOutput.contains("${moduleName}.unitypackage")
        result.standardOutput.contains("${moduleName}-3.0.0.unitypackage")
    }
}
