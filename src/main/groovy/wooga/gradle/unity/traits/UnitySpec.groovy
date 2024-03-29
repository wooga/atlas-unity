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

import com.wooga.gradle.BaseSpec
import org.apache.maven.artifact.versioning.ArtifactVersion
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import wooga.gradle.unity.UnityPluginConventions
import wooga.gradle.unity.utils.ProjectSettingsFile
import wooga.gradle.unity.utils.UnityFileTree
import wooga.gradle.unity.utils.UnityVersionManager

trait UnitySpec extends BaseSpec {

    private DirectoryProperty projectDirectory = objects.directoryProperty()
    /**
     * @return The Unity project directory (at the root)
     */
    @Internal
    DirectoryProperty getProjectDirectory() {
        projectDirectory
    }
    void setProjectDirectory(Provider<Directory> value) {
        projectDirectory.set(value)
    }

    private Property<ProjectSettingsFile> projectSettings = objects.property(ProjectSettingsFile)
    /**
     * @return A reference to the Unity project's ProjectSettings file, which contains serialized
     * configuration of a project
     */
    @Internal
    Property<ProjectSettingsFile> getProjectSettings() {
        projectSettings
    }
    void setProjectSettings(Provider<ProjectSettingsFile> value) {
        projectSettings.set(value)
    }

    private RegularFileProperty unityPath = objects.fileProperty()
    /**
     * @return The path to the Unity Editor application
     */
    @InputFile
    RegularFileProperty getUnityPath() {
        unityPath
    }
    void setUnityPath(Provider<RegularFile> value) {
        unityPath.set(value)
    }

    private Provider<UnityFileTree> getUnityFileTree() {
        return unityPath.map({ RegularFile unityExec ->
            UnityPluginConventions.getUnityFileTree(unityExec.asFile)
        }.memoize())
    }

    /**
     * @return The path to the Unity root directory
     */
    @Internal
    Provider<Directory> getUnityRootDir() {
        return layout.dir(getUnityFileTree().map({it.unityRoot}.memoize()))
    }

    /**
     * @return The path to Unity .NET Core dotnet executable
     */
    @Internal
    Provider<RegularFile> getDotnetExecutable() {
        return layout.file(getUnityFileTree().map({it.dotnetExecutable}.memoize()))
    }

    /**
     * @return The path to the Unity mono framework directory (MonoBleedingEdge)
     */
    @Internal
    Provider<Directory> getMonoFrameworkDir() {
        return layout.dir(getUnityFileTree().map({it.unityMonoFramework}.memoize()))
    }

    private RegularFileProperty unityLogFile = objects.fileProperty()
    /**
     * @return The path to the log file generated by Unity Editor process
     */
    @OutputFile
    RegularFileProperty getUnityLogFile(){
        unityLogFile
    }
    void setUnityLogFile(Provider<RegularFile> value){
        unityLogFile.set(value)
    }

    private final Property<String> logCategory = objects.property(String)

    /**
     * @return If set, any log files will be under a directory by this name
     */
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
}

