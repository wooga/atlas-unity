/*
 * Copyright 2018 Wooga GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package wooga.gradle.unity.tasks

import org.gradle.api.file.FileCollection
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.tasks.*
import org.gradle.util.GUtil
import wooga.gradle.unity.utils.internal.FileUtils
import wooga.gradle.unity.batchMode.BatchModeFlags
import wooga.gradle.unity.tasks.internal.AbstractUnityProjectTask

import javax.inject.Inject

/**
 * Exports a <code>*.unitypackage</code> file from configured directories.
 *
 * Example:
 * <pre>
 * {@code
 *     task exportPackage(type:wooga.gradle.unity.tasks.UnityPackage) {
 *         inputFiles [file('Assets/Dir1'), file('Assets/Dir2')]
 *         destinationDir = file('out')
 *         archiveName = "myExport"
 *     }
 * }
 * </pre>
 */

class UnityPackage extends AbstractUnityProjectTask {

    /**
     * Returns the base name of the archive.
     *
     * @return the base name.
     */
    @Internal("Represented as part of archiveName")
    String baseName

    /**
     * Returns the appendix part of the archive name, if any.
     *
     * @return the appendix. May be null
     */
    @Internal("Represented as part of archiveName")
    String appendix

    /**
     * Returns the version part of the archive name, if any.
     *
     * @return the version. May be null.
     */
    @Internal("Represented as part of archiveName")
    String version

    /**
     * Returns the extension part of the archive name.
     */
    @Internal("Represented as part of archiveName")
    String extension

    /**
     * Returns the directory where the archive is generated into.
     *
     * @return the directory
     */
    @OutputDirectory
    File destinationDir

    /**
     * File extension value for Unity packages (unitypackage).
     */
    public static final String UNITY_PACKAGE_EXTENSION = "unitypackage"

    /**
     * Returns the archive name. If the name has not been explicitly set, the pattern for the name is:
     * <code>[baseName]-[appendix]-[version].[extension]</code>
     *
     * @return the archive name.
     */
    @Internal("Represented as part of archivePath")
    String archiveName

    /**
     * Returns the archive name. If the name has not been explicitly set, the pattern for the name is:
     * <code>[baseName]-[appendix]-[version].[extension]</code>
     *
     * @return the archive name.
     */
    @Internal("Represented as part of archivePath")
    String getArchiveName() {
        if (this.archiveName) {
            return this.archiveName
        } else {
            String name = (String) GUtil.elvis(getBaseName(), "") + maybe(getBaseName(), getAppendix())
            name = name + maybe(name, getVersion())
            name = name + (GUtil.isTrue(getExtension()) ? "." + getExtension() : "")
            return name
        }
    }

    private static String maybe(String prefix, String value) {
        return GUtil.isTrue(value) ? (GUtil.isTrue(prefix) ? "-".concat(value) : value) : ""
    }

    /**
     * The path where the archive is constructed. The path is simply the {@code destinationDir} plus the {@code archiveName}.
     *
     * @return a File object with the path to the archive
     */
    @OutputFile
    File getArchivePath() {
        return new File(this.getDestinationDir(), getArchiveName())
    }

    /**
     * Returns the files to pack into the unity package.
     * Currently, this option only exports whole folders at a time.
     * This means that {@code File} objects pointing to files not directories
     * will be converted to the parent.
     * @return
     */
    @SkipWhenEmpty
    @InputFiles
    FileCollection inputFiles

    /**
     * Sets the input file collection
     * @param files input files
     */
    void setInputFiles(FileCollection files) {
        inputFiles = files
    }

    /**
     * Sets the input file collection with a single {@code File} object
     * The file object will be packed into a {@code FileCollection} object.
     * @param files input files
     */
    void setInputFiles(File source) {
        inputFiles = project.files([source])
    }

    /**
     * Sets the input file collection with a single {@code String} file path.
     * The patht will be converted into a {@code File} obbject and
     * packed into a {@code FileCollection} object.
     * @param files input files
     */
    void setInputFiles(String source) {
        inputFiles = project.files([source])
    }

    /**
     * Adds a {@code FileCollection} to the current collection of input files.
     * @param source a collection of files to add
     */
    void inputFiles(FileCollection source) {
        if (!inputFiles) {
            inputFiles = source
        } else {
            inputFiles = inputFiles + source
        }
    }

    /**
     * Adds a {@code File} to the current collection of input files.
     * @param source a file to add
     */
    void inputFiles(File source) {
        inputFiles(project.files([source]))
    }

    /**
     * Adds a {@code String} filepath to the current collection of input files.
     * The value will be converted to a {@code File} object.
     * @param source a file to add
     */
    void inputFiles(String source) {
        inputFiles(project.files([source]))
    }

    UnityPackage() {
        super(UnityPackage.class)
        this.extension = UNITY_PACKAGE_EXTENSION
        outputs.upToDateWhen { false }
    }

    @TaskAction
    @Override
    protected void exec() {
        def exportArgs = []
        exportArgs << BatchModeFlags.EXPORT_PACKAGE
        Set exportFiles = []
        getInputFiles().each { File f ->
            if (f.file) {
                f = f.parentFile
            }

            String relative = project.file(projectPath).toURI().relativize(f.toURI()).getPath()
            if (relative.endsWith("/")) {
                relative = relative.substring(0, relative.length() - 1)
            }
            exportFiles << relative
        }
        exportArgs << exportFiles.join(" ")

        FileUtils.ensureFile(getArchivePath())
        exportArgs << getArchivePath().path
        args(exportArgs)
        super.exec()
    }
}
