package wooga.gradle.unity.tasks

import com.wooga.gradle.BaseSpec
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

import org.gradle.api.file.FileCollection
import org.gradle.api.internal.file.copy.CopyAction
import org.gradle.api.provider.Provider
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.*
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import wooga.gradle.unity.traits.GenerateUpmPackageSpec

import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream

/**
 * A task that will generate an UPM package from a given Unity project
 */
class GenerateUpmPackage extends AbstractArchiveTask implements BaseSpec, GenerateUpmPackageSpec {

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
     * The default file name for the package manifest, as decided by Unity
     */
    public static final packageManifestFileName = "package.json"

    GenerateUpmPackage() {
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
        archiveExtension.set("tgz")

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
    protected CopyAction createCopyAction() {
        return null
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

        def tarFile = archiveFile.get().asFile
        new TarArchiveOutputStream(new GzipCompressorOutputStream(new FileOutputStream(tarFile))).withCloseable {
            it.longFileMode = TarArchiveOutputStream.LONGFILE_POSIX

            processSourceDir(it, temporaryDir, "package", null)
            processSourceDir(it,packageDirectory.asFile.get(), "package", "package.json")

            it.close()
        }
    }

    /**
     * Adds files to the tar stream recursively.
     */
    protected void processSourceDir(TarArchiveOutputStream stream, File dir, String prefix, String exclude){
        def sourceDirFile = dir
        def files = sourceDirFile.listFiles().sort { it.name }

        files.each { file ->
            if(exclude && file.name == exclude) {
                return
            }
            def entry = new TarArchiveEntry(file, "${prefix}/${file.name}")
            entry.setModTime(System.currentTimeMillis())

            if (file.isFile()) {
                stream.putArchiveEntry(entry)
                new FileInputStream(file).withCloseable {
                    it.eachByte { b ->
                        stream.write(b)
                    }
                }
                stream.closeArchiveEntry()
            } else {
                processSourceDir(stream,file, "${prefix}/${file.name}".toString(),exclude)
            }
        }
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
                Map additions = dependencies.get()
                if (additions.size() > 0) {
                    Map previous = manifestContent['dependencies'] as Map
                    manifestContent['dependencies'] = merge(previous, additions)
                }
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
     * @return The merged contents of two maps
     */
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
                    value = ((Provider) entry.value).get()
                } else {
                    value = entry.value
                }
                map[entry.key] = value
            }
            return map
        } as Map
    }
}


