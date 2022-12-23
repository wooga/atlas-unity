package wooga.gradle.unity.tasks


import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import wooga.gradle.unity.models.UnityProjectManifest
import wooga.gradle.unity.traits.UnityPackageSpec

abstract class ProjectManifestTask extends DefaultTask implements
    UnityPackageSpec {

    final static String manifestFileName = "manifest.json"
    final static String lockFileName = "packages-lock.json"

    abstract void modifyProjectManifest(UnityProjectManifest manifest)

    @TaskAction
    void execute() {
        def manifestFile = projectManifestFile.asFile.getOrNull()
        if (manifestFile && manifestFile.exists()) {
            // Deserialize
            def manifest = UnityProjectManifest.deserialize(manifestFile)
            // Modify
            modifyProjectManifest(manifest)
            // Serialize
            def serialization = manifest.serialize()
            manifestFile.write(serialization)
        } else {
            project.logger.warn("${manifestFileName} not found, skipping UPM packages install: ${upmPackages.get()}")
        }
    }
}


