package wooga.gradle.unity.tasks

import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional

class GenerateSolution extends RunCSScript {
    private final DirectoryProperty assetsDir = objects.directoryProperty()

    @InputDirectory
    @Optional
    DirectoryProperty getAssetsDir() {
        return assetsDir
    }

    void setAssetsDir(Provider<Directory> assetsDir) {
        this.assetsDir.set(assetsDir)
    }

    void setAssetsDir(Directory assetsDir) {
        this.assetsDir.set(assetsDir)
    }

    void setAssetsDir(File assetsDir) {
        this.assetsDir.set(assetsDir)
    }




    GenerateSolution() {
        outputs.upToDateWhen { false }

        // Not the usual approach we take. Usually we would like to put the default in the plugin conventions and such, but this
        // task seems to be designed to be independent from plugin configuration, so we still apply plugin configuration,
        // but we set __sensible defaults__.
        // This runs before the config block in the plugin, so it can and will be overwritten by plugin configuration when the plugin is applied as well
        this.assetsDir.convention(this.projectDirectory.dir("Assets").map {it.asFile.mkdirs();return it})

        def defaultScript = this.assetsDir
        .map { it.file("SolutionGenerator.cs") }
        .map { script ->
            script.asFile.text = GenerateSolution.classLoader.getResourceAsStream("DefaultSolutionGenerator.cs").text
            script.asFile.deleteOnExit()
            return script
        }
        this.sourceCsScript.convention(defaultScript)
        this.destCsScript.convention(this.sourceCsScript)
        this.executeMethod.set(project.provider {
            unityVersion.majorVersion >= 2022?
                    "Wooga.UnityPlugin.DefaultSolutionGenerator.GenerateSolution" :
                    "UnityEditor.SyncVS.SyncSolution"
        })

    }
}
