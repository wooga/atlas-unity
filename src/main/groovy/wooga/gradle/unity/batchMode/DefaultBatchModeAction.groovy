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

package wooga.gradle.unity.batchMode

import org.gradle.api.GradleException
import org.gradle.internal.Factory
import org.gradle.internal.file.PathToFileResolver
import org.gradle.internal.io.LineBufferingOutputStream
import org.gradle.internal.io.TextStream
import org.gradle.process.ExecResult
import org.gradle.process.internal.DefaultExecHandleBuilder
import org.gradle.process.internal.ExecException
import org.gradle.process.internal.ExecHandle
import wooga.gradle.FileUtils
import wooga.gradle.unity.UnityPluginExtension
import wooga.gradle.unity.utils.ForkTextStream

class DefaultBatchModeAction extends DefaultExecHandleBuilder implements BatchModeAction {
    private final UnityPluginExtension extension
    private final PathToFileResolver fileResolver

    private File customUnityPath
    private File customProjectPath
    private BuildTarget customBuildTarget

    File getUnityPath() {
        if (customUnityPath) {
            return customUnityPath
        }

        return extension.unityPath
    }

    void setUnityPath(File path) {
        customUnityPath = path
    }

    File getProjectPath() {
        if (customProjectPath) {
            return customProjectPath
        }

        return extension.projectPath
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

    void setLogFile(Object file) {
        logFile = null
        if(file) {
            logFile = fileResolver.resolveLater(file)
        }
    }

    private Boolean redirectStdOut

    @Override
    Boolean getRedirectStdOut() {
        return redirectStdOut
    }

    @Override
    void setRedirectStdOut(Boolean redirect) {
        this.redirectStdOut = redirect
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

    DefaultBatchModeAction(UnityPluginExtension extension, PathToFileResolver fileResolver) {
        super(fileResolver)
        this.fileResolver = fileResolver
        this.extension = extension

    }

    ExecResult execute() {
        def additionalArguments = getAllArguments()
        def batchModeArgs = []

        if (getUnityPath() == null || !getUnityPath().exists()) {
            throw new GradleException("Unity does not exist")
        }

        batchModeArgs << getUnityPath().path

        if (batchMode) {
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

        String osName = System.getProperty("os.name").toLowerCase()
        if (osName.contains("windows")) {
            if(getLogFile()) {
                FileUtils.ensureFile(getLogFile())
                batchModeArgs << BatchModeFlags.LOG_FILE
                batchModeArgs << getLogFile().path
            }
        }
        else {
            if (getRedirectStdOut() || getLogFile()) {
                batchModeArgs << BatchModeFlags.LOG_FILE

                if (getRedirectStdOut()) {
                    TextStream handler = new ForkTextStream()
                    def outStream = new LineBufferingOutputStream(handler)
                    this.standardOutput = outStream

                    if (getLogFile()) {
                        handler.addWriter(getLogFile().newPrintWriter())
                    }
                    handler.addWriter(System.out.newPrintWriter())
                } else {
                    batchModeArgs << getLogFile().path
                }
            }
        }

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

    @Override
    BaseBatchModeSpec redirectStdOut(Boolean redirect) {
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
    BatchModeSpec batchMode(Boolean value) {
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

    @Override
    DefaultBatchModeAction args(Object... args) {
        return DefaultBatchModeAction.cast(super.args(args))
    }

    @Override
    DefaultBatchModeAction args(Iterable<?> args) {
        return DefaultBatchModeAction.cast(super.args(args))
    }

    @Override
    DefaultBatchModeAction setArgs(Iterable<?> arguments) {
        return DefaultBatchModeAction.cast(super.setArgs(arguments))
    }
}
