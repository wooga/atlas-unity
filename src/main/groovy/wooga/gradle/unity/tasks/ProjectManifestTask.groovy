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

    ProjectManifestTask() {
        onlyIf {
            def manifestFile = projectManifestFile.asFile.getOrNull()
            def condition = manifestFile && manifestFile.exists()
            if(!condition) {
                project.logger.warn("${manifestFile.name} not found, skipping UPM packages install")
            }
            return condition
        }
    }

    @TaskAction
    void execute() {
        def manifestFile = projectManifestFile.asFile.getOrNull()
        // Deserialize
        def manifest = UnityProjectManifest.deserialize(manifestFile)
        // Modify
        modifyProjectManifest(manifest)
        // Serialize
        def serialization = manifest.serialize()
        manifestFile.write(serialization)
    }
}


