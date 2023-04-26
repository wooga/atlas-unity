package wooga.gradle.unity.traits

import com.wooga.gradle.BaseSpec
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Internal
import wooga.gradle.unity.models.UnityProjectManifest

trait UnityPackageSpec extends BaseSpec {

    /**
     * @return The project manifest file (manifest.json), which manages UPM packages
     */
    @Internal
    RegularFileProperty getProjectManifestFile() {
        return projectManifestFile
    }

    private final RegularFileProperty projectManifestFile = objects.fileProperty()

    void setProjectManifestFile(File value) {
        this.projectManifestFile.set(value)
    }

    void setProjectManifestFile(Provider<RegularFile> value) {
        this.projectManifestFile.set(value)
    }

    private final RegularFileProperty projectLockFile = objects.fileProperty()

    /**
     * @return The project package lock file (packages-lock.json), where the resolved UPM packages are recorded
     */
    @Internal
    RegularFileProperty getProjectLockFile() {
        projectLockFile
    }

    void setProjectLockFile(Provider<RegularFile> value) {
        projectLockFile.set(value)
    }

    void setProjectLockFile(File value) {
        projectLockFile.set(value)
    }

    @Internal
    Provider<UnityProjectManifest> getProjectManifest() {
        projectManifestFile.map({UnityProjectManifest.deserialize(it.asFile)})
    }

}
