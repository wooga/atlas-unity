package wooga.gradle.unity.testutils

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import wooga.gradle.unity.utils.PackageManifestBuilder

/**
 * Sets up the project file structure as expected by Unity's package manager
 */
class SetupProjectLayoutTestTask extends DefaultTask {

    private final Property<String> unityProjectDirectoryName = project.objects.property(String)

    @Input
    Property<String> getUnityProjectDirectoryName() {
        unityProjectDirectoryName
    }

    private final Property<String> packageName = project.objects.property(String)

    @Input
    Property<String> getPackageName() {
        packageName
    }

    private final Property<String> packageDisplayName = project.objects.property(String)

    @Input
    Property<String> getPackageDisplayName() {
        packageDisplayName
    }

    private final Property<String> initialPackageVersion = project.objects.property(String)

    @Input
    Property<String> getInitialPackageVersion() {
        initialPackageVersion
    }

    @TaskAction
    void exec() {
        def packageDisplayName = packageDisplayName.get()
        def packageName = this.packageName.get()

        File unityProjectDir = new File(project.projectDir, unityProjectDirectoryName.get())

        File assetsDir = new File(unityProjectDir, "Assets")

        def packageDir = new File(assetsDir.path, "Wooga/${packageDisplayName}")
        packageDir.mkdirs()

        def packageJson = new File(packageDir.path, "package.json")
        packageJson.write(new PackageManifestBuilder(packageName, initialPackageVersion.get()).build())

        def readme = new File(packageDir.path, "README.MD")
        readme.write("Here lies package ${packageDisplayName}")

        def license = new File(packageDir.path, "LICENSE.MD")
        license.write("Be good to each other")

        def runtimeDirectory = new File(packageDir.path, "Runtime")
        runtimeDirectory.mkdir()
        def runtimeSource = new File(runtimeDirectory.path, "${packageDisplayName}.cs")
        runtimeSource.write("""\
            using System;
            public class ${packageDisplayName} {
            }
        """.stripIndent())

        def editorDirectory = new File(packageDir.path, "Editor")
        editorDirectory.mkdir()
        def editorSource = new File(editorDirectory.path, "${packageDisplayName}Editor.cs")
        editorSource.write("""\
            using System;
            public class ${packageDisplayName}Editor {
            }
        """.stripIndent())
    }
}
