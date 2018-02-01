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
import org.gradle.api.internal.ConventionAwareHelper
import org.gradle.api.internal.ConventionMapping
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.tasks.*
import org.gradle.util.GUtil
import wooga.gradle.FileUtils
import wooga.gradle.unity.batchMode.BatchModeFlags

import javax.inject.Inject

class UnityPackage extends AbstractBatchModeTask {

    private final ConventionMapping conventionMapping
    private final FileResolver fileResolver
    private FileCollection inputFiles
    private String baseName
    private String appendix
    private String version
    private String extension


    public static final String UNITY_PACKAGE_EXTENSION = "unitypackage"

    @Internal("Represented as part of archivePath")
    File destinationDir

    private String customName

    File getDestinationDir() {
        return destinationDir
    }

    void setDestinationDir(File destinationDir) {
        this.destinationDir = destinationDir
    }

    @Internal("Represented as part of archiveName")
    String getBaseName() {
        return baseName
    }

    void setBaseName(String baseName) {
        this.baseName = baseName
    }

    @Internal("Represented as part of archiveName")
    String getAppendix() {
        return appendix
    }

    void setAppendix(String appendix) {
        this.appendix = appendix
    }

    @Internal("Represented as part of archiveName")
    String getVersion() {
        return version
    }

    void setVersion(String version) {
        this.version = version
    }

    @Internal("Represented as part of archiveName")
    String getExtension() {
        return extension
    }

    void setExtension(String extension) {
        this.extension = extension
    }

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

    void setArchiveName(String name) {
        customName = name
    }

    private String maybe(String prefix, String value) {
        return GUtil.isTrue(value) ? (GUtil.isTrue(prefix) ? "-".concat(value) : value) : ""
    }

    @OutputFile
    File getArchivePath() {
        return new File(this.getDestinationDir(), getArchiveName())
    }

    @SkipWhenEmpty
    @InputFiles
    FileCollection getInputFiles() {
        inputFiles
    }

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

    @Override
    ConventionMapping getConventionMapping() {
        return this
    }

    @Inject
    UnityPackage(FileResolver fileResolver) {
        super(UnityPackage.class)
        this.conventionMapping = new ConventionAwareHelper(this, project.getConvention())
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
