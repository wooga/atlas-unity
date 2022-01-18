package wooga.gradle.unity.internal

import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.file.FileTreeElement
import org.gradle.api.provider.Provider
import wooga.gradle.unity.UnityPluginExtension
import wooga.gradle.unity.UnityTask

class InputFileTreeFactory {

    private final Project project
    private final UnityPluginExtension extension

    InputFileTreeFactory(Project project, UnityPluginExtension extension) {
        this.project = project
        this.extension = extension
    }

    static ConfigurableFileCollection inputFilesForUnityTask(Project project, UnityPluginExtension extension, UnityTask task) {
        return new InputFileTreeFactory(project, extension).inputFilesForUnityTask(task)
    }

    ConfigurableFileCollection inputFilesForBuildTarget(Provider<Directory> projectDirectory, Provider<String> buildTarget) {
        def assetsDir = extension.assetsDir
        def assetsFileTree = project.fileTree(assetsDir)
        assetsFileTree.include { FileTreeElement elem ->
            def path = elem.getRelativePath().getPathString().toLowerCase()
            def name = elem.name.toLowerCase()
            if (path.contains("plugins") && !((name == "plugins") || (name == "plugins.meta"))) {
                return isPluginElemFromBuildTarget(elem, buildTarget)
            } else {
                return true
            }
        }

        def projectSettingsDir = projectDirectory.map {it.dir("ProjectSettings") }
        def projectSettingsFileTree = project.fileTree(projectSettingsDir)

        def packageManagerDir = projectDirectory.map{it.dir("UnityPackageManager") }
        def packageManagerDirFileTree = project.fileTree(packageManagerDir)

        return project.files(assetsFileTree, projectSettingsFileTree, packageManagerDirFileTree)
    }

    ConfigurableFileCollection inputFilesForUnityTask(UnityTask unityTask) {
        return inputFilesForBuildTarget(unityTask.projectDirectory, unityTask.buildTarget)
    }

    private static boolean isPluginElemFromBuildTarget(FileTreeElement element, Provider<String> buildTarget) {
        /*
         Why can we use / here? Because {@code element} is a {@code FileTreeElement} object.
         The getPath() method is not the same as {@code File.getPath()}
         From the docs:

         * Returns the path of this file, relative to the root of the containing file tree. Always uses '/' as the hierarchy
         * separator, regardless of platform file separator. Same as calling <code>getRelativePath().getPathString()</code>.
         *
         * @return The path. Never returns null.
         */
        def path = element.getRelativePath().getPathString().toLowerCase()
        if (buildTarget.isPresent()) {
            return path.contains("plugins/" + buildTarget.get())
        } else {
            return true
        }
    }
}
