/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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

    private final FileResolver fileResolver
    private FileCollection inputFiles
    private String baseName
    private String appendix
    private String version
    private String extension
    private File destinationDir

    /**
     * File extension value for Unity packages (unitypackage).
     */
    public static final String UNITY_PACKAGE_EXTENSION = "unitypackage"

    private String customName

    /**
     * Returns the directory where the archive is generated into.
     *
     * @return the directory
     */
    File getDestinationDir() {
        return destinationDir
    }

    void setDestinationDir(File destinationDir) {
        this.destinationDir = destinationDir
    }

    /**
     * Returns the base name of the archive.
     *
     * @return the base name.
     */
    @Internal("Represented as part of archiveName")
    String getBaseName() {
        return baseName
    }

    void setBaseName(String baseName) {
        this.baseName = baseName
    }

    /**
     * Returns the appendix part of the archive name, if any.
     *
     * @return the appendix. May be null
     */
    @Internal("Represented as part of archiveName")
    String getAppendix() {
        return appendix
    }

    void setAppendix(String appendix) {
        this.appendix = appendix
    }

    /**
     * Returns the version part of the archive name, if any.
     *
     * @return the version. May be null.
     */
    @Internal("Represented as part of archiveName")
    String getVersion() {
        return version
    }

    void setVersion(String version) {
        this.version = version
    }

    /**
     * Returns the extension part of the archive name.
     */
    @Internal("Represented as part of archiveName")
    String getExtension() {
        return extension
    }

    void setExtension(String extension) {
        this.extension = extension
    }

    /**
     * Returns the archive name. If the name has not been explicitly set, the pattern for the name is:
     * <code>[baseName]-[appendix]-[version].[extension]</code>
     *
     * @return the archive name.
     */
    @Internal("Represented as part of archivePath")
    String getArchiveName() {
        if (this.customName != null) {
            return this.customName
        } else {
            String name = (String) GUtil.elvis(getBaseName(), "") + maybe(getBaseName(), getAppendix())
            name = name + this.maybe(name, getVersion())
            name = name + (GUtil.isTrue(getExtension()) ? "." + getExtension() : "")
            return name
        }
    }

    /**
     * Sets the archive name.
     *
     * @param name the archive name.
     */
    void setArchiveName(String name) {
        customName = name
    }

    private String maybe(String prefix, String value) {
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
     * The files to pack into the unity package.
     * Currently, this option only exports whole folders at a time.
     * This means that {@code File} objects pointing to files not directories
     * will be converted to the parent.
     * @return
     */
    @SkipWhenEmpty
    @InputFiles
    FileCollection getInputFiles() {
        inputFiles
    }

    //TODO rething this API
    /*
    Make a final filecollection and add when passing a new file with method accessor
    and delete and add when using the setter.
     */
    void setInputFiles(FileCollection files) {
        inputFiles = files
    }

    void inputFiles(FileCollection source) {
        if (!inputFiles) {
            inputFiles = source
        } else {
            inputFiles = inputFiles + source
        }
    }

    void inputFiles(File source) {
        inputFiles(project.files([source]))
    }

    void inputFiles(String source) {
        inputFiles(project.files([source]))
    }

    @Inject
    UnityPackage(FileResolver fileResolver) {
        super(UnityPackage.class)
        this.fileResolver = fileResolver
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
