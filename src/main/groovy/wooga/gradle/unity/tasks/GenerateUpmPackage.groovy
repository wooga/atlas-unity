package wooga.gradle.unity.tasks

import com.wooga.gradle.BaseSpec
import groovy.json.JsonSlurper
import groovyjarjarcommonscli.MissingArgumentException
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.bundling.Compression
import org.gradle.api.tasks.bundling.Tar

/**
 * A task that will generate an UPM package from a given Unity project
 */
class GenerateUpmPackage extends Tar implements BaseSpec {

    enum Failure {
        packageDirectoryNotSet("No package directory was set"),
        packageManifestFileNotFound("No package manifest file (package.json) was found"),
        packageNameNotSet("No package name was set"),
        versionNotSet("No version was set for the package by the archive")
        // TODO: Add if the gradle error for it isnt enough
        //packageManifestNotFound("No package manifest (package.json) was found in the package directory"),

        String message

        String getMessage() {
            message
        }

        Failure(String message) {
            this.message = message
        }
    }

    /**
     * @return The directory where the package source files are located
     */
    @SkipWhenEmpty
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
     * @return The package manifest file, `package.json`, which defines the package dependencies and other metadata.
     */
    @InputFile
    Provider<RegularFile> getPackageManifestFile() {
        packageManifestFile
    }

    private final Provider<RegularFile> packageManifestFile = packageDirectory.file(packageManifestFileName)

    /**
     * @return The officially registered package name. This name must conform to the Unity Package Manager naming convention,
     * which uses reverse domain name notation.
     */
    @Input
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

    public static final packageManifestFileName = "package.json"


    GenerateUpmPackage() {

        setCompression(Compression.GZIP)

        // Gathers the sources of the package. (We have to wrap it like this in case package directory is never set)
        from(providers.provider({
            if (!packageDirectory.present) {
                throw new MissingArgumentException(Failure.packageDirectoryNotSet.message)
            }
            packageDirectory
        }))
        // Creates a root directory inside the package
        into("package")
        // The name of the package, in reverse domain name notation
        Provider<String> packageNameOnFile = packageManifestFile.map({
            def slurper = new JsonSlurper()
            if (!it.asFile.exists()){
                return null
            }
            slurper.parse(it.asFile)["name"].toString()
        })
        packageName.convention(packageNameOnFile)
        archiveBaseName.set(packageName)
        filesMatching(packageManifestFileName) {
            filter { it.replaceAll(/"name" : ".*?"/, "\"name\" : \"${packageName.get()}\"") }
            filter { it.replaceAll(/"version" : ".*?"/, "\"version\" : \"${archiveVersion.get()}\"") }
        }

        onlyIf(new Spec<GenerateUpmPackage>() {
            @Override
            boolean isSatisfiedBy(GenerateUpmPackage t) {
                if (!t.archiveVersion.present) {
                    logger.error(Failure.versionNotSet.message)
                    return false
                }
                if (t.packageManifestFile.present && !t.packageManifestFile.get().asFile.exists()){
                    logger.error(Failure.packageManifestFileNotFound.message)
                    return false
                }
                if (!t.packageName.present || t.packageName.get().empty) {
                    logger.error(Failure.packageNameNotSet.message)
                    return false
                }
                true
            }
        })
    }
}


