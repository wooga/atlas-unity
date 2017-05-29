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

import org.gradle.internal.file.PathToFileResolver
import org.gradle.process.ExecResult
import org.gradle.process.internal.DefaultExecHandleBuilder
import org.gradle.process.internal.ExecHandle
import wooga.gradle.unity.UnityPluginExtension

class DefaultBatchModeAction extends DefaultExecHandleBuilder implements BatchModeAction {

    private final UnityPluginExtension extension

    File unityPath
    File projectPath
    File logFile
    BuildTarget buildTarget = BuildTarget.undefined

    Boolean quit = true
    Boolean batchMode = true
    Boolean noGraphics = false

    DefaultBatchModeAction(UnityPluginExtension extension, PathToFileResolver fileResolver) {
        super(fileResolver)
        this.extension = extension
        unityPath = extension.unityPath
        projectPath = extension.projectPath
    }

    ExecResult execute() {
        def additionalArguments = getAllArguments()
        def batchModeArgs = []

        batchModeArgs << unityPath.path

        if(batchMode)
        {
            batchModeArgs << BatchModeFlags.BATCH_MODE
        }

        if (projectPath) {
            batchModeArgs << BatchModeFlags.PROJECT_PATH << projectPath.path
        }

        if(logFile) {
            batchModeArgs << BatchModeFlags.LOG_FILE << logFile.path
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

        ignoreExitValue = true
        commandLine = batchModeArgs
        args(additionalArguments)

        ExecHandle execHandle = this.build()
        ExecResult execResult = execHandle.start().waitForFinish()

        return execResult
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
    DefaultBatchModeAction logFile(File file) {
        this.logFile = file
        this
    }

    @Override
    DefaultBatchModeAction buildTarget(BuildTarget target) {
        this.buildTarget = target
        this
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

    @Override
    DefaultExecHandleBuilder executable(Object executable) {
        return super.executable(executable)
    }

    @Override
    DefaultExecHandleBuilder commandLine(Object... arguments) {
        return super.commandLine(arguments)
    }

    @Override
    DefaultExecHandleBuilder commandLine(Iterable<?> args) {
        return super.commandLine(args)
    }

    @Override
    void setCommandLine(Object... args) {
        super.setCommandLine(args)
    }

    @Override
    void setCommandLine(Iterable<?> args) {
        super.setCommandLine(args)
    }
}
