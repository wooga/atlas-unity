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

package wooga.gradle.unity

import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.incremental.IncrementalTaskInputs
import org.gradle.util.GUtil

//class UnityPackage extends AbstractUnityTask {
//
//    public static final String UNITYPACKAGE_EXTENSION = "unitypackage"
//
//    @Internal("Represented as part of archivePath")
//    File destinationDir = project.file("${project.buildDir}/outputs/")
//
//    private String customName
//
//    @Internal("Represented as part of archiveName")
//    String baseName
//
//    @Internal("Represented as part of archiveName")
//    String appendix
//
//    @Internal("Represented as part of archiveName")
//    String version
//
//    @Internal("Represented as part of archiveName")
//    String extension = UNITYPACKAGE_EXTENSION
//
//    @Internal("Represented as part of archivePath")
//    String getArchiveName() {
//        if (this.customName != null) {
//            return this.customName
//        } else {
//            String name = (String) GUtil.elvis(baseName, "") + maybe(baseName, appendix)
//            name = name + this.maybe(name, version)
//            name = name + (GUtil.isTrue(extension) ? "." + extension : "")
//            return name
//        }
//    }
//
//    void setArchiveName(String name) {
//        customName = name
//    }
//
//    private String maybe(String prefix, String value) {
//        return GUtil.isTrue(value) ? (GUtil.isTrue(prefix) ? "-".concat(value) : value) : ""
//    }
//
//    @OutputFile
//    File getArchivePath() {
//        return new File(this.destinationDir, archiveName)
//    }
//
//    @InputFiles
//    FileCollection inputFiles
//
//    UnityPackage inputFiles(Object source) {
//        if (!inputFiles) {
//            inputFiles = project.files(source)
//        } else {
//            inputFiles = inputFiles + project.files(source)
//        }
//        return this
//    }
//
//    UnityPackage() {
//        super(UnityPackage.class)
//    }
//
//    @Override
//    protected List<String> configureArguments() {
//        def args = []
//        args << "-exportPackage"
//
//        inputFiles.each { File f ->
//            String relative = project.file(projectPath).toURI().relativize(f.toURI()).getPath()
//
//            //hack because unity doesn't like paths ending with /
//            if (relative.endsWith("/")) {
//                relative = relative.substring(0, relative.length() - 1)
//            }
//
//            args << relative
//        }
//
//        args << archivePath
//
//        return null
//    }
//
//    @Override
//    void performUnityAction(IncrementalTaskInputs inputs) {
//        def rebuild = false
//        inputs.outOfDate { change ->
//            println "out of date: ${change.file}"
//            rebuild = true
//        }
//
//        if (rebuild) {
//            super.performUnityAction(inputs)
//        }
//    }
//}
