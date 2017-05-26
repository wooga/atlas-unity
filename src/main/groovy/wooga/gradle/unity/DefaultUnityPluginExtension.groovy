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

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.internal.file.FileResolver
import org.gradle.internal.reflect.Instantiator
import org.gradle.process.ExecResult
import wooga.gradle.unity.batchMode.BatchModeAction
import wooga.gradle.unity.batchMode.BatchModeActionFactory
import wooga.gradle.unity.batchMode.BatchModeSpec
import wooga.gradle.unity.batchMode.DefaultBatchModeActionFactory

import static org.gradle.util.ConfigureUtil.configureUsing


class DefaultUnityPluginExtension implements UnityPluginExtension {

    static File UNITY_PATH_MAC_OS = new File("/Applications/Unity/Unity.app/Contents/MacOS/Unity")
    static File UNITY_PATH_WIN = new File("C:/Program Files/Unity/Editor/Unity.exe")
    static File UNITY_PATH_WIN_32 = new File("C:/Program Files (x86)/Unity/Editor/Unity.exe")
    static File UNITY_PATH_LINUX = new File("/opt/Unity/Editor/Unity")

    private final Instantiator instantiator
    private final FileResolver fileResolver
    private final Project project

    BatchModeActionFactory batchModeActionFactory

    private File customUnityPath

    File getUnityPath() {
        File unityPath = customUnityPath

        if (unityPath == null) {
            String osName = System.getProperty("os.name").toLowerCase()
            String osArch = System.getProperty("os.arch").toLowerCase()

            if (osName.contains("windows")) {
                if (osArch.contains("64")) {
                    unityPath = UNITY_PATH_WIN
                } else {
                    unityPath = UNITY_PATH_WIN_32
                }
            } else if (osName.contains("linux")) {
                unityPath = UNITY_PATH_LINUX
            } else if (osName.contains("mac os x")) {
                unityPath = UNITY_PATH_MAC_OS
            } else {
                throw new IllegalArgumentException("os: $osName not supported")
            }
        }

        if (!unityPath.exists()) {
            throw new IllegalArgumentException("unitypath $unityPath.path doesn't exist")
        }

        return unityPath
    }

    void setUnityPath(File path) {
        customUnityPath = path
    }

    File projectPath

    @Override
    DefaultUnityPluginExtension unityPath(File path) {
        customUnityPath = path
        this
    }

    @Override
    DefaultUnityPluginExtension projectPath(File path) {
        projectPath = path
        this
    }

    DefaultUnityPluginExtension(Project project, FileResolver fileResolver, Instantiator instantiator) {
        this.project = project
        this.fileResolver = fileResolver
        this.instantiator = instantiator
        this.batchModeActionFactory = instantiator.newInstance(DefaultBatchModeActionFactory, this, instantiator, fileResolver)
        projectPath = project.projectDir
    }

    ExecResult batchMode(Closure closure) {
        return batchMode(configureUsing(closure))
    }

    ExecResult batchMode(Action<? super BatchModeSpec> action) {
        BatchModeAction batchModeAction = batchModeActionFactory.newExecAction()
        action.execute(batchModeAction)
        return batchModeAction.execute()
    }
}
