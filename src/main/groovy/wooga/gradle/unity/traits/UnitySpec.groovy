/*
 * Copyright 2021 Wooga GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package wooga.gradle.unity.traits

import org.gradle.api.model.ObjectFactory
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.internal.impldep.org.eclipse.jgit.errors.NotSupportedException
import wooga.gradle.unity.UnityPluginConventions
import wooga.gradle.unity.models.BuildTarget
import wooga.gradle.unity.utils.ProjectSettingsFile

import javax.inject.Inject

trait UnitySpec extends UnityBaseSpec {

    private DirectoryProperty projectDirectory = objects.directoryProperty()
    @Internal
    DirectoryProperty getProjectDirectory() {
        projectDirectory
    }
    void setProjectDirectory(Provider<Directory> value) {
        projectDirectory.set(value)
    }

    private Property<ProjectSettingsFile> projectSettings = objects.property(ProjectSettingsFile)
    @Internal
    Property<ProjectSettingsFile> getProjectSettings() {
        projectSettings
    }
    void setProjectSettings(Provider<ProjectSettingsFile> value) {
        projectSettings.set(value)
    }

    private RegularFileProperty unityPath = objects.fileProperty()
    @InputFile
    RegularFileProperty getUnityPath() {
        unityPath
    }
    void setUnityPath(Provider<RegularFile> value) {
        unityPath.set(value)
    }

    private RegularFileProperty unityLogFile = objects.fileProperty()
    @OutputFile
    RegularFileProperty getUnityLogFile(){
        unityLogFile
    }
    void setUnityLogFile(Provider<RegularFile> value){
        unityLogFile.set(value)
    }

    private final Property<String> logCategory = objects.property(String)

    @Internal
    Property<String> getLogCategory() {
        logCategory
    }

    void setLogCategory(Provider<String> value) {
        logCategory.set(value)
    }
    void setLogCategory(String value) {
        logCategory.set(value)
    }

    private final Property<Boolean> logToStdOut = objects.property(Boolean)
    @Internal
    Property<Boolean> getLogToStdout() {
        logToStdOut
    }

}
