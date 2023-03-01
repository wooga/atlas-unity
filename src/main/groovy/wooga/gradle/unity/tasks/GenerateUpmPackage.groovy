package wooga.gradle.unity.tasks

import com.wooga.gradle.BaseSpec
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFile
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

/**
 * A task that will generate an UPM package from a given Unity project
 */
class GenerateUpmPackage extends Tar implements BaseSpec {

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

    @SkipWhenEmpty
    @InputFiles
    FileCollection getPackageFiles() {
        if (packageDirectory.present) {
            return project.fileTree(packageDirectory)
        }
        project.files()
    }

    private final Provider<RegularFile> packageManifestFile = packageDirectory.file(packageManifestFileName)

    /**
     * @return The package manifest file, `package.json`, which defines the package dependencies and other metadata.
     */
    @Internal("part of packageFiles")
    Provider<RegularFile> getPackageManifestFile() {
        packageManifestFile
    }


    private final Property<String> packageName = objects.property(String)

    /**
     * @return The officially registered package name. This name must conform to the Unity Package Manager naming convention,
     * which uses reverse domain name notation.
     */
    @Input
    @Optional
    Provider<String> getPackageName() {
        packageName
    }

    void setPackageName(Provider<String> value) {
        packageName.set(value)
    }

    void setPackageName(String value) {
        packageName.set(value)
    }

    public static final packageManifestFileName = "package.json"

    protected void adjustManifestFile() {
        def manifest = new File(temporaryDir, packageManifestFileName)
        if(manifest.exists()) {
            def j = new JsonSlurper()
            def manifestContent = j.parse(manifest)

            if(packageName.present) {
                manifestContent['name'] = packageName.get()
            }

            if(archiveVersion.present) {
                manifestContent['version'] = archiveVersion.get()
            }

            String json = JsonOutput.toJson(manifestContent)
            manifest.text = JsonOutput.prettyPrint(json)
        }
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
            if(it.getFile().absolutePath.startsWith(packageDirectory.get().asFile.absolutePath)) {
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
}


