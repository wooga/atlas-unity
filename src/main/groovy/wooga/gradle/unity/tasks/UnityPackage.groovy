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
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.Factory
import org.gradle.util.GUtil
import wooga.gradle.unity.batchMode.BatchModeFlags

import javax.inject.Inject

class UnityPackage extends AbstractUnityTask {

    private final FileResolver fileResolver
    private Factory<File> exportFile
    private FileCollection inputFiles

    public static final String UNITY_PACKAGE_EXTENSION = "unitypackage"

    @Internal("Represented as part of archivePath")
    File destinationDir = project.file("${project.buildDir}/outputs/")

    private String customName

    @Internal("Represented as part of archiveName")
    String baseName = name

    @Internal("Represented as part of archiveName")
    String appendix

    @Internal("Represented as part of archiveName")
    String version = project.version

    @Internal("Represented as part of archiveName")
    String extension = UNITY_PACKAGE_EXTENSION

    @Internal("Represented as part of archivePath")
    String getArchiveName() {
        if (this.customName != null) {
            return this.customName
        } else {
            String name = (String) GUtil.elvis(baseName, "") + maybe(baseName, appendix)
            name = name + this.maybe(name, version)
            name = name + (GUtil.isTrue(extension) ? "." + extension : "")
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
        return new File(this.destinationDir, archiveName)
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

    @Inject
    UnityPackage(FileResolver fileResolver) {
        super(UnityPackage.class)
        this.fileResolver = fileResolver
    }

    @TaskAction
    @Override
    protected void exec() {
        def exportArgs = []
        exportArgs << BatchModeFlags.EXPORT_PACKAGE
        Set exportFiles = []
        inputFiles.each { File f ->
            if(f.file) {
                f = f.parentFile
            }

            String relative = project.file(projectPath).toURI().relativize(f.toURI()).getPath()
            if (relative.endsWith("/")) {
                relative = relative.substring(0, relative.length() - 1)
            }
            exportFiles << relative
        }
        exportArgs << exportFiles.join(" ")
        exportArgs << getArchivePath().path
        args(exportArgs)
        super.exec()
    }
}
