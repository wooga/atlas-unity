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
import org.gradle.internal.Factory
import org.gradle.internal.reflect.Instantiator
import org.gradle.process.ExecResult
import wooga.gradle.unity.batchMode.*

import static org.gradle.util.ConfigureUtil.configureUsing

class DefaultUnityPluginExtension implements UnityPluginExtension {

    static File UNITY_PATH_MAC_OS = new File("/Applications/Unity/Unity.app/Contents/MacOS/Unity")
    static File UNITY_PATH_WIN = new File("C:\\Program Files\\Unity\\Editor\\Unity.exe")
    static File UNITY_PATH_WIN_32 = new File("C:\\Program Files (x86)\\Unity\\Editor\\Unity.exe")
    static File UNITY_PATH_LINUX = new File("/opt/Unity/Editor/Unity")

    static File UNITY_LICENSE_DIRECTORY_MAC_OS = new File("/Library/Application Support/Unity/")
    static File UNITY_LICENSE_DIRECTORY_WIN = new File("C:\\ProgramData\\Unity")

    static final String UNITY_PATH_OPTION = "unity.path"
    static final String UNITY_PATH_ENV_VAR = "UNITY_PATH"

    static File defaultUnityLocation() {
        File unityPath = null
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
        }
        unityPath
    }

    private Boolean autoReturnLicense
    private Boolean autoActivateUnity

    private final Instantiator instantiator
    private final FileResolver fileResolver
    private final Project project
    private final UnityAuthentication authentication

    Factory<BatchModeAction> batchModeActionFactory
    Factory<ActivationAction> activationActionFactory

    private Factory<File> reportsDir
    private Factory<File> customUnityPath

    File getUnityPathFromEnv(Map<String, ?> properties, Map<String, String> env) {
        String unityPath = properties[UNITY_PATH_OPTION] ?: env[UNITY_PATH_ENV_VAR]
        if (unityPath) {
            return new File(unityPath)
        } else {
            return null
        }
    }

    File getUnityPath() {
        File unityPath
        if (customUnityPath) {
            return customUnityPath.create()
        }

        unityPath = getUnityPathFromEnv(project.properties, System.getenv())

        if (unityPath == null) {
            unityPath = defaultUnityLocation()
        }

        return unityPath
    }

    void setUnityPath(Object path) {
        customUnityPath = fileResolver.resolveLater(path)
    }

    @Override
    File getUnityLicenseDirectory() {
        File licensePath = null
        String osName = System.getProperty("os.name").toLowerCase()

        if (osName.contains("windows")) {
            licensePath = UNITY_LICENSE_DIRECTORY_WIN
        } else if (osName.contains("mac os x")) {
            licensePath = UNITY_LICENSE_DIRECTORY_MAC_OS
        }
        licensePath
    }

    File getReportsDir() {
        if (reportsDir) {
            return reportsDir.create()
        }
        return null
    }

    void setReportsDir(File file) {
        reportsDir = fileResolver.resolveLater(file)
    }

    void setReportsDir(Object file) {
        reportsDir = fileResolver.resolveLater(file)
    }

    File projectPath

    @Override
    DefaultUnityPluginExtension unityPath(Object path) {
        customUnityPath = fileResolver.resolveLater(path)
        this
    }

    @Override
    DefaultUnityPluginExtension projectPath(File path) {
        projectPath = path
        this
    }

    UnityAuthentication getAuthentication() {
        return authentication
    }

    void setAuthentication(UnityAuthentication authentication) {
        this.authentication.username = authentication.username
        this.authentication.password = authentication.password
        this.authentication.serial = authentication.serial
    }

    UnityPluginExtension authentication(Closure closure) {
        return authentication(configureUsing(closure))
    }

    UnityPluginExtension authentication(Action<? super UnityAuthentication> action) {
        action.execute(this.authentication)
        return this
    }

    @Override
    Boolean getAutoReturnLicense() {
        return autoActivateUnity && autoReturnLicense
    }

    @Override
    void setAutoReturnLicense(Boolean value) {
        autoReturnLicense = value
    }

    @Override
    UnityPluginExtension autoReturnLicense(Boolean value) {
        autoReturnLicense = value
        return this
    }

    @Override
    Boolean getAutoActivateUnity() {
        return autoActivateUnity
    }

    @Override
    void setAutoActivateUnity(Boolean value) {
        autoActivateUnity = value
    }

    @Override
    UnityPluginExtension autoActivateUnity(Boolean value) {
        autoActivateUnity = value
        return this
    }

    DefaultUnityPluginExtension(Project project, FileResolver fileResolver, Instantiator instantiator) {
        this.project = project
        this.fileResolver = fileResolver
        this.instantiator = instantiator
        this.authentication = new UnityAuthentication(project.rootProject.properties, System.getenv())
        this.batchModeActionFactory = instantiator.newInstance(DefaultBatchModeActionFactory, this, instantiator, fileResolver)
        this.activationActionFactory = instantiator.newInstance(DefaultActivationActionFactory, this, instantiator, fileResolver, authentication)
        projectPath = project.projectDir

        autoActivateUnity = true
        autoReturnLicense = true
    }

    ExecResult batchMode(Closure closure) {
        return batchMode(configureUsing(closure))
    }

    ExecResult batchMode(Action<? super BatchModeSpec> action) {
        BatchModeAction batchModeAction = batchModeActionFactory.create()
        action.execute(batchModeAction)
        return batchModeAction.execute()
    }

    @Override
    ExecResult activate(Closure closure) {
        return activate(configureUsing(closure))
    }

    @Override
    ExecResult activate(Action<? super ActivationSpec> action) {
        ActivationAction activationAction = activationActionFactory.create()
        action.execute(activationAction)
        return activationAction.activate()
    }

    @Override
    ExecResult returnLicense(Closure closure) {
        return returnLicense(configureUsing(closure))
    }

    @Override
    ExecResult returnLicense(Action<? super BaseBatchModeSpec> action) {
        ActivationAction activationAction = activationActionFactory.create()
        action.execute(activationAction)
        return activationAction.returnLicense()
    }
}
