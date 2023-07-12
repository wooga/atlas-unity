package wooga.gradle.unity.traits

import com.wooga.gradle.BaseSpec
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.SkipWhenEmpty
import wooga.gradle.unity.tasks.GenerateUpmPackage

// TODO: Had issues in making this a trait rather than interface
/**
 * Generates an UPM package with optional configuration of its manifest
 */
trait GenerateUpmPackageSpec extends BaseSpec {

/**
 * @return The directory where the package source files are located
 */
    @InputDirectory
    DirectoryProperty getPackageDirectory() {
        packageDirectory
    }

    private final DirectoryProperty packageDirectory = objects.directoryProperty()

    void setPackageDirectory(Provider<Directory> value) {
        packageDirectory.set(value)
    }

    void setPackageDirectory(File value) {
        packageDirectory.set(value)
    }

    /**
     * @return The package manifest file, `package.json`, which defines the package dependencies and other metadata.
     */
    @Internal("part of packageFiles")
    Provider<RegularFile> getPackageManifestFile() {
        if (packageManifestFile == null) {
            packageManifestFile =  packageDirectory.file(GenerateUpmPackage.packageManifestFileName)
        }
        packageManifestFile
    }
    private Provider<RegularFile> packageManifestFile

    /**
     * @return The officially registered package name. This name must conform to the Unity Package Manager naming convention,
     * which uses reverse domain name notation.
     */
    @Input
    @Optional
    Provider<String> getPackageName() {
        packageName
    }

    private final Property<String> packageName = objects.property(String)

    void setPackageName(Provider<String> value) {
        packageName.set(value)
    }

    void setPackageName(String value) {
        packageName.set(value)
    }

    /**
     * @return Optional dependencies to set
     */
    @Input
    @Optional
    MapProperty<String, String> getDependencies() {
        dependencies
    }

    private MapProperty<String, String> dependencies = objects.mapProperty(String, String)

    /**
     * @return Optional patches to be applied onto the manifest before it is packaged
     */
    @Input
    @Optional
    MapProperty<String, Object> getPatches() {
        patches
    }

    private MapProperty<String, Object> patches = objects.mapProperty(String, Object)

    /**
     * Patches the version of a single dependency in the manifest
     * @param name The name of the package
     * @param version The version to set
     */
    void patchDependency(String name, Object version) {
        def key = "dependencies"
        if (!patches.get().containsKey(key)) {
            patches[key] = new HashMap<String, Object>()
        }

        def dependencies = patches[key].get() as Map
        dependencies[name] = version
    }

    /**
     * Patches a single property in the manifest
     * @param key The property to patch
     * @param value The value to set
     */
    void patch(String key, Object value) {
        def provider = providers.provider({ value })
        patches.put(key, provider)
    }

}
