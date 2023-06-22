package wooga.gradle.unity.tasks

import com.wooga.gradle.BaseSpec
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.bundling.Compression
import org.gradle.api.tasks.bundling.Tar
import org.gradle.internal.impldep.com.google.common.collect.ImmutableMap

/**
 * A task that will generate an UPM package from a given Unity project
 */
class GenerateUpmPackage extends Tar implements BaseSpec {

    /**
     * A custom message that is presented when the packaging task fails
     */
    enum Message {
        packageDirectoryNotSet("No package directory was set"),
        packageManifestFileNotFound("No package manifest file (package.json) was found"),
        packageNameNotSet("No package name was set"),
        versionNotSet("No version was set for the package by the archive")

        String message

        String getMessage() {
            message
        }

        Message(String message) {
            this.message = message
        }
    }

    /**
     * @return The directory where the package source files are located
     */
    @InputDirectory
    DirectoryProperty getPackageDirectory() {
        packageDirectory
    }

    private final DirectoryProperty packageDirectory = project.objects.directoryProperty()

    void setPackageDirectory(Provider<Directory> value) {
        packageDirectory.set(value)
    }

    void setPackageDirectory(File value) {
        packageDirectory.set(value)
    }

    /**
     * @return All files in the package, if present
     */
    @SkipWhenEmpty
    @InputFiles
    FileCollection getPackageFiles() {
        if (packageDirectory.present) {
            return project.fileTree(packageDirectory)
        }
        project.files()
    }

    /**
     * @return The package manifest file, `package.json`, which defines the package dependencies and other metadata.
     */
    @Internal("part of packageFiles")
    Provider<RegularFile> getPackageManifestFile() {
        packageManifestFile
    }

    private final Provider<RegularFile> packageManifestFile = packageDirectory.file(packageManifestFileName)

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
     * The default file name for the package manifest, as decided by Unity
     */
    public static final packageManifestFileName = "package.json"

    GenerateUpmPackage() {

        setCompression(Compression.GZIP)

        // Creates a root directory inside the package
        into("package")
        // The name of the package, in reverse domain name notation
        Provider<String> packageNameOnFile = packageManifestFile.map({
            def slurper = new JsonSlurper()
            if (!it.asFile.exists()) {
                return null
            }
            slurper.parse(it.asFile)["name"].toString()
        })

        Provider<String> packageVersionOnFile = packageManifestFile.map({
            def slurper = new JsonSlurper()
            if (!it.asFile.exists()) {
                return null
            }
            slurper.parse(it.asFile)["version"].toString()
        })

        packageName.convention(packageNameOnFile)
        archiveVersion.set(packageVersionOnFile)
        archiveBaseName.set(packageName)
        preserveFileTimestamps = false
        reproducibleFileOrder = true

        filesMatching(packageManifestFileName) {
            if (it.getFile().absolutePath.startsWith(packageDirectory.get().asFile.absolutePath)) {
                it.exclude()
            }
        }

        onlyIf(new Spec<GenerateUpmPackage>() {
            @Override
            boolean isSatisfiedBy(GenerateUpmPackage t) {
                if (!packageDirectory.present) {
                    logger.warn(Message.packageDirectoryNotSet.message)
                    return false
                }
                if (!t.archiveVersion.present) {
                    logger.warn(Message.versionNotSet.message)
                    return false
                }
                if (t.packageManifestFile.present && !t.packageManifestFile.get().asFile.exists()) {
                    logger.warn(Message.packageManifestFileNotFound.message)
                    return false
                }
                if (!t.packageName.present || t.packageName.get().empty) {
                    logger.warn(Message.packageNameNotSet.message)
                    return false
                }
                true
            }
        })
    }

    @Override
    protected void copy() {
        if (!packageDirectory.present) {
            logger.warn(Message.packageDirectoryNotSet.message)
        }

        project.copy {
            from(packageDirectory)
            include(packageManifestFileName)
            into(temporaryDir)
        }

        adjustManifestFile()

        from(temporaryDir)
        from(packageDirectory)
        super.copy()
    }

    /**
     * Modifies the project manifest file (package.json) before it is packaged along with the project)
     */
    protected void adjustManifestFile() {
        def manifest = new File(temporaryDir, packageManifestFileName)

        if (manifest.exists()) {

            // Read
            def json = new JsonSlurper()
            Map manifestContent = json.parse(manifest)

            // Apply the default properties
            if (packageName.present) {
                manifestContent['name'] = packageName.get()
            }

            if (archiveVersion.present) {
                manifestContent['version'] = archiveVersion.get()
            }

            if (dependencies.present) {
                manifestContent['dependencies'] = dependencies.get()
            }

            def _patches = patches.get().collectEntries {
                Object value = null
                if (it.value instanceof Provider) {
                    value = ((Provider) it.value).get()
                } else {
                    value = it.value
                }
                [it.key, value]
            }
            manifestContent = merge(manifestContent, _patches)

            // Write back
            manifest.text = JsonOutput.prettyPrint(JsonOutput.toJson(manifestContent))
        }
    }

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

    private static Map merge(Map lhs, Map rhs) {

        // Have to do this since we cannot clone an ImmutableMap
        def clone = new HashMap(lhs)

        return rhs.inject(clone) { map, entry ->
            if (map[entry.key] instanceof Map && entry.value instanceof Map) {
                map[entry.key] = merge(map[entry.key], entry.value)
            } else {
                Object value = null
                // Unwrap Provider if needed
                if (entry.value instanceof Provider) {
                    value = ((Provider)entry.value).get()
                } else{
                    value = entry.value
                }
                map[entry.key] = value
            }
            return map
        } as Map
    }
}


