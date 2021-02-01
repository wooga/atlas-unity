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

package wooga.gradle.unity.internal

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.internal.file.FileResolver
import org.gradle.internal.Factory
import org.gradle.internal.reflect.Instantiator
import org.gradle.process.ExecResult
import org.gradle.util.GUtil
import wooga.gradle.unity.UnityAuthentication
import wooga.gradle.unity.UnityPlugin
import wooga.gradle.unity.UnityPluginConsts
import wooga.gradle.unity.UnityPluginConvention
import wooga.gradle.unity.UnityPluginExtension
import wooga.gradle.unity.batchMode.*
import wooga.gradle.unity.batchMode.internal.DefaultActivationActionFactory
import wooga.gradle.unity.batchMode.internal.DefaultBatchModeActionFactory
import wooga.gradle.unity.utils.internal.UnityHub

import java.util.concurrent.Callable

import static org.gradle.util.ConfigureUtil.configureUsing

class DefaultUnityPluginExtension implements UnityPluginExtension, UnityPluginActionExtension {

    static File defaultUnityLocation() {
        File unityPath = null
        String osName = System.getProperty("os.name").toLowerCase()
        String osArch = System.getProperty("os.arch").toLowerCase()

        if (osName.contains("windows")) {
            if (osArch.contains("64")) {
                unityPath = UnityPluginConsts.UNITY_PATH_WIN
            } else {
                unityPath = UnityPluginConsts.UNITY_PATH_WIN_32
            }
        } else if (osName.contains("linux")) {
            unityPath = UnityPluginConsts.UNITY_PATH_LINUX
        } else if (osName.contains("mac os x")) {
            unityPath = UnityPluginConsts.UNITY_PATH_MAC_OS
        }
        unityPath
    }

    private Boolean autoReturnLicense
    private Boolean autoActivateUnity
    private Boolean batchModeForPlayModeTest
    private Boolean batchModeForEditModeTest

    private final Instantiator instantiator
    protected final FileResolver fileResolver
    private final Project project
    private final UnityAuthentication authentication

    Factory<BatchModeAction> batchModeActionFactory
    Factory<ActivationAction> activationActionFactory

    private Factory<File> reportsDir
    private Factory<File> assetsDir
    private Factory<File> pluginsDir
    private Factory<File> customUnityPath
    private Object defaultBuildTarget
    private final List<Object> testBuildTargets = new ArrayList<Object>()
    private Boolean redirectStdOut = false
    private String logCategory

    static File getUnityPathFromEnv(Map<String, ?> properties, Map<String, String> env) {
        String unityPath = properties[UnityPluginConsts.UNITY_PATH_OPTION] ?: env[UnityPluginConsts.UNITY_PATH_ENV_VAR]
        if (unityPath) {
            return new File(unityPath)
        } else {
            return null
        }
    }

    static String getUnityLogCategory(Map<String, ?> properties, Map<String, String> env) {
        properties[UnityPluginConsts.UNITY_LOG_CATEGORY_OPTION] ?: env[UnityPluginConsts.UNITY_LOG_CATEGORY_ENV_VAR]
    }

    File getUnityPath() {
        File unityPath
        if (customUnityPath) {
            return customUnityPath.create()
        }

        unityPath = getUnityPathFromEnv(project.properties, System.getenv())

        if (unityPath == null) {
            //read unity-hub version if available
            unityPath = UnityHub.defaultEditor
        }

        if (unityPath == null) {
            unityPath = defaultUnityLocation()
        }

        return unityPath
    }

    void setUnityPath(File unityPath) {
        setUnityPath(unityPath as Object)
    }

    void setUnityPath(Object path) {
        customUnityPath = new Factory<File>() {
            @Override
            File create() {
                fileResolver.resolve(path)
            }
        }
    }

    private static Boolean getRedirectStdOutFromEnv(Map<String, ?> properties, Map<String, String> env) {
        String rawValue = (properties[UnityPluginConsts.REDIRECT_STDOUT_OPTION] ?: env[UnityPluginConsts.REDIRECT_STDOUT_ENV_VAR]).toString().toLowerCase()
        rawValue = (rawValue == "1" || rawValue == "yes") ? "true" : rawValue
        return Boolean.valueOf(rawValue)
    }

    Boolean getRedirectStdOut() {
        if (redirectStdOut) {
            return redirectStdOut
        }

        return getRedirectStdOutFromEnv(project.properties, System.getenv())
    }

    void setRedirectStdOut(Boolean redirect) {
        redirectStdOut = redirect
    }

    UnityPluginExtension redirectStdOut(Boolean redirect) {
        setRedirectStdOut(redirect)
        return this
    }

    File getUnityLicenseDirectory() {
        File licensePath = null
        String osName = System.getProperty("os.name").toLowerCase()

        if (osName.contains("windows")) {
            licensePath = UnityPluginConsts.UNITY_LICENSE_DIRECTORY_WIN
        } else if (osName.contains("mac os x")) {
            licensePath = UnityPluginConsts.UNITY_LICENSE_DIRECTORY_MAC_OS
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
        reportsDir = new Factory<File>() {
            @Override
            File create() {
                fileResolver.resolve(file)
            }
        }
    }

    void setReportsDir(Object file) {
        reportsDir = new Factory<File>() {
            @Override
            File create() {
                fileResolver.resolve(file)
            }
        }
    }

    UnityPluginExtension reportsDir(Object reportsDir) {
        this.setReportsDir(project.file(reportsDir))
        return this
    }

    File getPluginsDir() {
        if (pluginsDir) {
            return pluginsDir.create()
        }

        return null
    }

    void setPluginsDir(File path) {
        pluginsDir = new Factory<File>() {
            @Override
            File create() {
                fileResolver.resolve(path)
            }
        }
    }

    UnityPluginExtension pluginsDir(Object path) {
        this.setPluginsDir(project.file(path))
        return this
    }

    File getAssetsDir() {
        if (assetsDir) {
            return assetsDir.create()
        }

        return null
    }

    void setAssetsDir(File path) {
        assetsDir = new Factory<File>() {
            @Override
            File create() {
                return fileResolver.resolve(path)
            }
        }
    }

    UnityPluginExtension assetsDir(Object path) {
        this.setAssetsDir(project.file(path))
        return this
    }

    File projectPath

    DefaultUnityPluginExtension unityPath(Object path) {
        this.setUnityPath(path)
        this
    }

    /**
     * {@inheritDoc}
     */
    UnityPluginConvention unityPath(File path) {
        this.setUnityPath(path)
        return this
    }

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

    UnityPluginExtension authentication(UnityAuthentication authentication) {
        this.authentication.username = authentication.username
        this.authentication.password = authentication.password
        this.authentication.serial = authentication.serial
        this
    }

    UnityPluginExtension authentication(Closure closure) {
        return authentication(configureUsing(closure))
    }

    UnityPluginExtension authentication(Action<? super UnityAuthentication> action) {
        action.execute(this.authentication)
        return this
    }

    Boolean getAutoReturnLicense() {
        return autoActivateUnity && autoReturnLicense
    }

    void setAutoReturnLicense(Boolean value) {
        autoReturnLicense = value
    }

    UnityPluginExtension autoReturnLicense(Boolean value) {
        autoReturnLicense = value
        return this
    }

    Boolean getAutoActivateUnity() {
        return autoActivateUnity
    }

    void setAutoActivateUnity(Boolean value) {
        autoActivateUnity = value
    }


    UnityPluginExtension autoActivateUnity(Boolean value) {
        autoActivateUnity = value
        return this
    }


    void setDefaultBuildTarget(BuildTarget value) {
        this.setDefaultBuildTarget(value as Object)
    }


    void setDefaultBuildTarget(Object value) {
        defaultBuildTarget = value
    }


    UnityPluginExtension defaultBuildTarget(BuildTarget value) {
        this.setDefaultBuildTarget(value)
        return this
    }


    void setLogCategory(String value) {
        logCategory = value
    }


    String getLogCategory() {
        return (logCategory ?: getUnityLogCategory(project.properties, System.getenv())) ?: ""
    }


    UnityPluginExtension logCategory(String value) {
        this.setLogCategory(value)
        return this
    }


    BuildTarget getDefaultBuildTarget() {
        if (!defaultBuildTarget) {
            return BuildTarget.undefined
        }
        if (defaultBuildTarget instanceof Callable) {
            defaultBuildTarget.call()
        } else if (defaultBuildTarget instanceof String) {
            defaultBuildTarget.toLowerCase() as BuildTarget
        } else {
            defaultBuildTarget as BuildTarget
        }
    }

    DefaultUnityPluginExtension(Project project, FileResolver fileResolver, Instantiator instantiator) {
        this.project = project
        this.fileResolver = fileResolver
        this.instantiator = instantiator
        this.authentication = new DefaultUnityAuthentication(project.rootProject.properties, System.getenv())
        this.batchModeActionFactory = instantiator.newInstance(DefaultBatchModeActionFactory, project, instantiator, fileResolver)
        this.activationActionFactory = instantiator.newInstance(DefaultActivationActionFactory, project, instantiator, fileResolver, authentication)
        projectPath = project.projectDir

        autoActivateUnity = true
        autoReturnLicense = true
    }

    ExecResult batchMode(Closure closure) {
        return batchMode(configureUsing(closure))
    }

    ExecResult batchMode(Action<? super BatchModeSpec> action) {
        BatchModeAction batchModeAction = batchModeActionFactory.create()

        def conventionMapper = batchModeAction.conventionMapping
        UnityPlugin.applyBaseConvention(conventionMapper, project.extensions.getByType(UnityPluginExtension))

        action.execute(batchModeAction)
        return batchModeAction.execute()
    }

    ExecResult activate(Closure closure) {
        return activate(configureUsing(closure))
    }


    ExecResult activate(Action<? super ActivationSpec> action) {
        ActivationAction activationAction = activationActionFactory.create()
        action.execute(activationAction)
        return activationAction.activate()
    }


    ExecResult returnLicense(Closure closure) {
        return returnLicense(configureUsing(closure))
    }

    ExecResult returnLicense(Action<? super BaseBatchModeSpec> action) {
        ActivationAction activationAction = activationActionFactory.create()
        action.execute(activationAction)
        return activationAction.returnLicense()
    }


    UnityPluginExtension testBuildTargets(Object... targets) {
        if (targets == null) {
            throw new IllegalArgumentException("targets == null!")
        }
        testBuildTargets.addAll(Arrays.asList(targets))
        return this
    }

    UnityPluginExtension testBuildTargets(Iterable<?> targets) {
        GUtil.addToCollection(testBuildTargets, targets)
        return this
    }

    void setTestBuildTargets(Iterable<?> targets) {
        testBuildTargets.clear()
        testBuildTargets.addAll(targets)
    }

    Set<BuildTarget> getTestBuildTargets() {
        if (testBuildTargets.empty && project.properties.containsKey("unity.testBuildTargets")) {
            return EnumSet.copyOf(project.properties.get("unity.testBuildTargets").toString().split(",").collect({
                it as BuildTarget
            }))
        } else if (testBuildTargets.empty && getDefaultBuildTarget() == BuildTarget.undefined) {
            return EnumSet.noneOf(BuildTarget)
        }

        List<BuildTarget> targets = new ArrayList<BuildTarget>()
        for (
                Object t
                        : testBuildTargets) {
            if (t != BuildTarget.undefined) {
                targets.add(t.toString() as BuildTarget)
            }
        }

        if (getDefaultBuildTarget() != BuildTarget.undefined) {
            targets.add(getDefaultBuildTarget())
        }

        return EnumSet.copyOf(targets)
    }

    private static Boolean getBatchModeForPlayModeTestFromEnv(Map<String, ?> properties, Map<String, String> env) {
        String rawValue = (properties[UnityPluginConsts.BATCH_MODE_FOR_PLAY_MODE_TEST_OPTION] ?: env[UnityPluginConsts.BATCH_MODE_FOR_PLAY_MODE_TEST_ENV_VAR]).toString().toLowerCase()
        rawValue = (rawValue == "1" || rawValue == "yes") ? "true" : rawValue
        return Boolean.valueOf(rawValue)
    }

    @Override
    Boolean getBatchModeForPlayModeTest() {
        if (batchModeForPlayModeTest) {
            return batchModeForPlayModeTest
        }

        return getBatchModeForPlayModeTestFromEnv(project.properties, System.getenv())
    }

    @Override
    void setBatchModeForPlayModeTest(Boolean value) {
        batchModeForPlayModeTest = value
    }

    @Override
    UnityPluginConvention batchModeForPlayModeTest(Boolean value) {
        setBatchModeForPlayModeTest(value)
        return this
    }

    private static Boolean getBatchModeForEditModeTestFromEnv(Map<String, ?> properties, Map<String, String> env) {
        String rawValue = (properties[UnityPluginConsts.BATCH_MODE_FOR_EDIT_MODE_TEST_OPTION] ?: env[UnityPluginConsts.BATCH_MODE_FOR_EDIT_MODE_TEST_ENV_VAR]).toString().toLowerCase()
        rawValue = (rawValue == "1" || rawValue == "yes") ? "true" : rawValue
        return Boolean.valueOf(rawValue)
    }

    @Override
    Boolean getBatchModeForEditModeTest() {
        if (batchModeForEditModeTest) {
            return batchModeForEditModeTest
        }

        return getBatchModeForEditModeTestFromEnv(project.properties, System.getenv())
    }

    @Override
    void setBatchModeForEditModeTest(Boolean value) {
        batchModeForEditModeTest = value
    }

    @Override
    UnityPluginConvention batchModeForEditModeTest(Boolean value) {
        setBatchModeForEditModeTest(value)
        return this
    }

    Boolean getBatchMode(TestPlatform testPlatform) {
        switch (testPlatform) {
            case TestPlatform.editmode:
                return getBatchModeForEditModeTest()
            case TestPlatform.playmode:
                return getBatchModeForPlayModeTest()
            default:
                return true
        }
    }
}
