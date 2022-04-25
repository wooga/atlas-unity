package wooga.gradle.unity.testutils

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import wooga.gradle.unity.utils.PackageManifestBuilder

/**
 * Sets up the project file structure as expected by Unity's package manager
 */
class SetupProjectLayoutTestTask extends DefaultTask {

    public static String packageName = "Foobar"
    public static String unityProjectDirectoryName = "Wooga.${packageName}"

    @TaskAction
    void exec() {
        File unityProjectDir = new File(project.projectDir, unityProjectDirectoryName)

        File assetsDir = new File(unityProjectDir, "Assets")

        def packageDir = new File(assetsDir.path, "Wooga/${packageName}")
        packageDir.mkdirs()

        def packageJson = new File(packageDir.path, "package.json")
        packageJson.write(new PackageManifestBuilder("com.wooga.${packageName.toLowerCase()}", "0.0.0").build())

        def readme = new File(packageDir.path, "README.MD")
        readme.write("Here lies package ${packageName}")

        def license = new File(packageDir.path, "LICENSE.MD")
        license.write("Be good to each other")

        def runtimeDirectory = new File(packageDir.path, "Runtime")
        runtimeDirectory.mkdir()
        def runtimeSource = new File(runtimeDirectory.path, "${packageName}.cs")
        runtimeSource.write("""
using System;
public class ${packageName} {
}
""")

        def editorDirectory = new File(packageDir.path, "Editor")
        editorDirectory.mkdir()
        def editorSource = new File(editorDirectory.path, "${packageName}Editor.cs")
        editorSource.write("""
using System;
public class ${packageName}Editor {
}
""")
    }
}
