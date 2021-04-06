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

package wooga.gradle.unity.batchMode.internal

import org.apache.maven.artifact.versioning.ArtifactVersion
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.internal.ConventionAwareHelper
import org.gradle.api.internal.ConventionMapping
import org.gradle.api.internal.IConventionAware
import org.gradle.api.internal.file.FileResolver
import org.gradle.internal.Factory
import org.gradle.internal.io.LineBufferingOutputStream
import org.gradle.internal.io.TextStream
import org.gradle.process.ExecResult
import org.gradle.process.internal.DefaultExecActionFactory
import org.gradle.process.internal.ExecException
import org.gradle.process.internal.ExecHandle
import org.gradle.process.internal.ExecHandleBuilder
import wooga.gradle.unity.UnityPlugin
import wooga.gradle.unity.UnityPluginExtension
import wooga.gradle.unity.batchMode.BatchModeAction
import wooga.gradle.unity.batchMode.BatchModeFlags
import wooga.gradle.unity.batchMode.BatchModeSpec
import wooga.gradle.unity.batchMode.BuildTarget
import wooga.gradle.unity.batchMode.UnityCommandLineOption
import wooga.gradle.unity.utils.internal.FileUtils
import wooga.gradle.unity.utils.internal.ForkTextStream
import wooga.gradle.unity.utils.internal.UnityVersionManager

class DefaultBatchModeAction implements BatchModeAction, IConventionAware {
    private final UnityPluginExtension extension
    protected final FileResolver fileResolver
    private final ConventionMapping conventionMapping
    private final Project project

    private File customUnityPath
    private File customProjectPath
    private BuildTarget customBuildTarget
    private String logCategory

    @Delegate()
    ExecHandleBuilder execHandleBuilder

    File getUnityPath() {
        if (customUnityPath) {
            return customUnityPath
        }

        extension.unityPath
    }

    void setUnityPath(File path) {
        customUnityPath = path
    }

    File getProjectPath() {
        if (customProjectPath) {
            return customProjectPath
        }

        extension.projectPath
    }

    protected ArtifactVersion getUnityVersion() {
        UnityVersionManager.retrieveUnityVersion(getUnityPath(), project.properties.get("defaultUnityTestVersion", "5.6.0").toString())
    }

    void setProjectPath(File path) {
        customProjectPath = path
    }

    private Factory<File> logFile

    File getLogFile() {
        if (logFile) {
            return logFile.create()
        }
        return null
    }

    @Override
    void setLogFile(File file) {
        setLogFile(file as Object)
    }

    void setLogFile(Object file) {
        logFile = null
        if (file) {
            logFile = new Factory<File>() {
                @Override
                File create() {
                    return fileResolver.resolve(file)
                }
            }
        }
    }

    private Boolean redirectStdOut

    @Override
    Boolean getRedirectStdOut() {
        if (redirectStdOut) {
            return redirectStdOut
        }
        extension.redirectStdOut
    }

    @Override
    void setRedirectStdOut(Boolean redirect) {
        this.redirectStdOut = redirect
    }

    @Override
    String getLogCategory() {
        if (this.logCategory) {
            return logCategory
        }
        extension.logCategory
    }

    @Override
    DefaultBatchModeAction logCategory(String value) {
        this.setLogCategory(value)
        return this
    }

    @Override
    void setLogCategory(String value) {
        this.logCategory = value
    }

    BuildTarget getBuildTarget() {
        if (customBuildTarget && customBuildTarget != BuildTarget.undefined) {
            return customBuildTarget
        }

        extension.defaultBuildTarget
    }

    Boolean quit = true
    Boolean batchMode = true
    Boolean noGraphics = false
    Map<UnityCommandLineOption, Boolean> commandLineOptions

    DefaultBatchModeAction(Project project, FileResolver fileResolver) {
        this.project = project
        this.fileResolver = fileResolver
        this.extension = project.getExtensions().findByName(UnityPlugin.EXTENSION_NAME) as UnityPluginExtension
        this.conventionMapping = new ConventionAwareHelper(this, project.getConvention())
        def execFactory = new DefaultExecActionFactory(fileResolver)
        this.execHandleBuilder = execFactory.newExec()
        this.commandLineOptions = UnityCommandLineOption.values().collectEntries(
                {[it, it.value] }
        )
    }

    @Override
    DefaultBatchModeAction args(Object... args) {
        execHandleBuilder.args(args)
        this
    }

    @Override
    DefaultBatchModeAction args(Iterable<?> args) {
        execHandleBuilder.args(args)
        return this
    }

    @Override
    DefaultBatchModeAction setArgs(List<String> arguments) {
        execHandleBuilder.setArgs(arguments)
        return this
    }

    @Override
    DefaultBatchModeAction setArgs(Iterable<?> arguments) {
        execHandleBuilder.setArgs(arguments)
        return this
    }

    @Override
    ConventionMapping getConventionMapping() {
        return conventionMapping
    }

    ExecResult execute() {
        def additionalArguments = getArgs()
        def batchModeArgs = []

        if (getUnityPath() == null || !getUnityPath().exists()) {
            throw new GradleException("Unity does not exist")
        }

        batchModeArgs << getUnityPath().path

        if (getBatchMode()) {
            batchModeArgs << BatchModeFlags.BATCH_MODE
        }

        if (projectPath) {
            batchModeArgs << BatchModeFlags.PROJECT_PATH << projectPath.path
        }

        if (buildTarget != BuildTarget.undefined) {
            batchModeArgs << BatchModeFlags.BUILD_TARGET << buildTarget.toString()
        }

        if (quit) {
            batchModeArgs << BatchModeFlags.QUIT
        }

        if (noGraphics) {
            batchModeArgs << BatchModeFlags.NO_GRAPHICS
        }

        // Set additional flags here
        for(option in commandLineOptions){
            if (option.value == true){
                batchModeArgs << option.key.value
            }
        }

        setupLogFile(batchModeArgs)

        ignoreExitValue = true
        commandLine = batchModeArgs
        args(additionalArguments)

        ExecHandle execHandle = this.build()
        ExecResult execResult = execHandle.start().waitForFinish()

        if (execResult.exitValue != 0) {
            String errorMessage = UnityLogErrorReader.readErrorMessageFromLog(getLogFile())
            throw new ExecException(String.format("Unity batchmode finished with non-zero exit value %d and error '%s'", Integer.valueOf(execResult.exitValue), errorMessage))
        }

        return execResult
    }

    protected void setupLogFile(List batchModeArgs) {
        if (getLogFile()) {
            FileUtils.ensureFile(getLogFile())
        }

        String osName = System.getProperty("os.name").toLowerCase()

        def unityVersion = getUnityVersion()

        if (osName.contains("windows") && unityVersion.majorVersion < 2019) {
            if (getLogFile()) {
                FileUtils.ensureFile(getLogFile())
                batchModeArgs << BatchModeFlags.LOG_FILE
                batchModeArgs << getLogFile().path
            }
        } else {
            if (getRedirectStdOut() || getLogFile()) {
                batchModeArgs << BatchModeFlags.LOG_FILE

                if (getRedirectStdOut()) {
                    if(unityVersion.majorVersion >= 2019) {
                        batchModeArgs << "-"
                    }

                    TextStream handler = new ForkTextStream()
                    def outStream = new LineBufferingOutputStream(handler)
                    this.setStandardOutput(outStream)

                    if (getLogFile()) {
                        handler.addWriter(getLogFile().newPrintWriter())
                    }
                    handler.addWriter(System.out.newPrintWriter())
                } else {
                    batchModeArgs << getLogFile().path
                }
            }
        }
    }

    @Override
    DefaultBatchModeAction redirectStdOut(Boolean redirect) {
        setRedirectStdOut(redirect)
        return this
    }

    @Override
    DefaultBatchModeAction unityPath(File path) {
        unityPath = path
        this
    }

    @Override
    DefaultBatchModeAction projectPath(File path) {
        projectPath = path
        this
    }

    @Override
    DefaultBatchModeAction logFile(Object file) {
        setLogFile(file)
        this
    }

    @Override
    DefaultBatchModeAction buildTarget(BuildTarget target) {
        this.customBuildTarget = target
        this
    }

    @Override
    void setBuildTarget(BuildTarget target) {
        buildTarget(target)
    }

    @Override
    DefaultBatchModeAction batchMode(Boolean value) {
        this.batchMode = value
        this
    }

    @Override
    DefaultBatchModeAction quit(Boolean value) {
        this.quit = value
        this
    }

    @Override
    DefaultBatchModeAction noGraphics(Boolean value) {
        this.noGraphics = value
        this
    }

    /*
        UnityCommandLineOption.disableAssemblyUpdater
     */
    @Override
    Boolean getDisableAssemblyUpdater() {
        commandLineOptions[UnityCommandLineOption.disableAssemblyUpdater]
    }

    @Override
    void setDisableAssemblyUpdater(Boolean value) {
        commandLineOptions[UnityCommandLineOption.disableAssemblyUpdater] = value
    }

    @Override
    DefaultBatchModeAction disableAssemblyUpdater(Boolean value) {
        this.disableAssemblyUpdater = value
        this
    }

    /*
        UnityCommandLineOption.deepProfiling
     */
    @Override
    Boolean getDeepProfiling() {
        commandLineOptions[UnityCommandLineOption.deepProfiling]
    }

    @Override
    void setDeepProfiling(Boolean value) {
        commandLineOptions[UnityCommandLineOption.deepProfiling] = value
    }

    @Override
    DefaultBatchModeAction deepProfiling(Boolean value) {
        this.deepProfiling = value
        this
    }

    /*
        UnityCommandLineOption.enableCodeCoverage
     */
    @Override
    Boolean getEnableCodeCoverage() {
        commandLineOptions[UnityCommandLineOption.enableCodeCoverage]
    }

    @Override
    void setEnableCodeCoverage(Boolean value) {
        commandLineOptions[UnityCommandLineOption.enableCodeCoverage] = value
    }

    @Override
    DefaultBatchModeAction enableCodeCoverage(Boolean value) {
        this.enableCodeCoverage = value
        this
    }


}
