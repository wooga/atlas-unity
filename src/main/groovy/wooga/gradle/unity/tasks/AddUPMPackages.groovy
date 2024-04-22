package wooga.gradle.unity.tasks


import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Internal
import wooga.gradle.unity.models.UnityProjectManifest
import wooga.gradle.unity.traits.AddUnityPackagesSpec

/**
 * Adds the given packages onto the project's manifest
 */
class AddUPMPackages extends ProjectManifestTask
    implements AddUnityPackagesSpec {

    /**
     * @deprecated Use {@code manifestFile} instead
     */
    @Internal
    @Deprecated
    RegularFileProperty getManifestPath() {
        projectManifestFile
    }

    /**
     * @deprecated Use {@code manifestFile} instead
     */
    @Deprecated
    void setManifestPath(File value) {
        projectManifestFile.set(value)
    }

    /**
     * @deprecated Use {@code manifestFile} instead
     */
    @Deprecated
    void setManifestPath(Provider<RegularFile> value) {
        projectManifestFile.set(value)
    }

    AddUPMPackages() {
        description = "Adds UPM packages to Unity project"
    }

    @Override
    void modifyProjectManifest(UnityProjectManifest manifest) {
        conventionUpmPackages.getOrElse([:]).each {
            if(!manifest.getDependencyVersion(it.key)) {
                manifest.addDependency(it.key, it.value)
            }
        }
        manifest.addDependencies(upmPackages.get())

    }
}
