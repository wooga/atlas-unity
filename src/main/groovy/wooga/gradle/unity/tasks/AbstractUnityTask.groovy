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

import org.gradle.api.internal.ConventionTask
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.Factory
import org.gradle.process.ExecResult
import org.gradle.process.ProcessForkOptions
import wooga.gradle.unity.UnityPluginExtension
import wooga.gradle.unity.batchMode.BatchModeAction
import wooga.gradle.unity.batchMode.BatchModeSpec
import wooga.gradle.unity.batchMode.BuildTarget

abstract class AbstractUnityTask<T extends AbstractUnityTask> extends ConventionTask implements BatchModeSpec {

    static Logger logger = Logging.getLogger(AbstractUnityTask)

    private final Class<T> taskType

    private BatchModeAction batchModeAction
    private ExecResult batchModeResult

    protected Factory<BatchModeAction> getBatchModeActionFactory() {
        return project.extensions.getByType(UnityPluginExtension).batchModeActionFactory
    }

    AbstractUnityTask(Class<T> taskType) {
        this.batchModeAction = getBatchModeActionFactory().create()
        this.batchModeAction.logFile("${project.buildDir}/logs/${name}.log")
        this.taskType = taskType
    }

    @Override
    void setExecutable(String executable) {
        batchModeAction.executable(executable)

    }

    @Override
    void setWorkingDir(File dir) {
        batchModeAction.setWorkingDir(dir)
    }

    @TaskAction
    protected void exec() {
        batchModeResult = batchModeAction.execute()
    }

    @Optional
    @Input
    @Override
    File getUnityPath() {
        batchModeAction.unityPath
    }

    @Override
    T unityPath(File path) {
        batchModeAction.unityPath = path
        return taskType.cast(this)
    }

    @Override
    void setUnityPath(File path) {
        batchModeAction.unityPath = path
    }

    @Optional
    @Input
    @Override
    File getProjectPath() {
        batchModeAction.projectPath
    }

    @Override
    T projectPath(File path) {
        batchModeAction.projectPath = path
        return taskType.cast(this)
    }

    @Override
    void setProjectPath(File path) {
        batchModeAction.projectPath = path
    }

    @Optional
    @Internal
    @Override
    File getLogFile() {
        batchModeAction.logFile
    }

    @Override
    T logFile(Object file) {
        batchModeAction.logFile = file
        return taskType.cast(this)
    }

    @Override
    void setLogFile(Object file) {
        batchModeAction.logFile = file
    }

    @Optional
    @Input
    @Override
    BuildTarget getBuildTarget() {
        batchModeAction.buildTarget
    }

    @Override
    T buildTarget(BuildTarget target) {
        batchModeAction.buildTarget = target
        return taskType.cast(this)
    }

    @Override
    void setBuildTarget(BuildTarget target) {
        batchModeAction.buildTarget = target
    }

    @Optional
    @Input
    @Override
    Boolean getBatchMode() {
        return batchModeAction.batchMode
    }

    @Optional
    @Input
    @Override
    BatchModeSpec batchMode(Boolean value) {
        batchModeAction.batchMode = value
        return taskType.cast(this)

    }

    @Optional
    @Input
    @Override
    void setBatchMode(Boolean value) {
        batchModeAction.batchMode = value
    }

    @Optional
    @Input
    @Override
    Boolean getQuit() {
        return batchModeAction.quit
    }

    @Override
    T quit(Boolean value) {
        batchModeAction.quit = value
        return taskType.cast(this)
    }

    @Override
    void setQuit(Boolean value) {
        batchModeAction.quit = value
    }

    @Optional
    @Input
    @Override
    Boolean getNoGraphics() {
        batchModeAction.noGraphics
    }

    @Override
    T noGraphics(Boolean value) {
        batchModeAction.noGraphics = value
        return taskType.cast(this)
    }

    @Override
    void setNoGraphics(Boolean value) {
        batchModeAction.noGraphics = value
    }

    /**
     * {@inheritDoc}
     */
    T commandLine(Object... arguments) {
        //batchModeAction.commandLine(arguments)
        return taskType.cast(this)
    }

    /**
     * {@inheritDoc}
     */
    T commandLine(Iterable<?> args) {
        //batchModeAction.commandLine(args)
        return taskType.cast(this)
    }

    /**
     * {@inheritDoc}
     */
    T args(Object... args) {
        batchModeAction.args(args)
        return taskType.cast(this)
    }

    /**
     * {@inheritDoc}
     */
    T args(Iterable<?> args) {
        batchModeAction.args(args)
        return taskType.cast(this)
    }

    /**
     * {@inheritDoc}
     */
    T setArgs(Iterable<?> arguments) {
        batchModeAction.setArgs(arguments)
        return taskType.cast(this)
    }

    /**
     * {@inheritDoc}
     */
    @Optional
    @Input
    List<String> getArgs() {
        return batchModeAction.getArgs()
    }

    /**
     * {@inheritDoc}
     */
    @Internal
    List<String> getCommandLine() {
        return batchModeAction.getCommandLine()
    }

    /**
     * {@inheritDoc}
     */
    void setCommandLine(Iterable<?> args) {
        batchModeAction.setCommandLine(args)
    }

    /**
     * {@inheritDoc}
     */
    void setCommandLine(Object... args) {
        batchModeAction.setCommandLine(args)
    }

    /**
     * {@inheritDoc}
     */
    @Optional
    @Internal
    String getExecutable() {
        return batchModeAction.getExecutable()
    }

    /**
     * {@inheritDoc}
     */
    void setExecutable(Object executable) {
        //batchModeAction.setExecutable(executable)
    }

    /**
     * {@inheritDoc}
     */
    T executable(Object executable) {
        //batchModeAction.executable(executable)
        return taskType.cast(this)
    }

    /**
     * {@inheritDoc}
     */
    @Internal
    // TODO:LPTR Should be a content-less @InputDirectory
    File getWorkingDir() {
        return batchModeAction.getWorkingDir()
    }

    /**
     * {@inheritDoc}
     */
    void setWorkingDir(Object dir) {
        batchModeAction.setWorkingDir(dir)
    }

    /**
     * {@inheritDoc}
     */
    T workingDir(Object dir) {
        batchModeAction.workingDir(dir)
        return taskType.cast(this)
    }

    /**
     * {@inheritDoc}
     */
    @Internal
    Map<String, Object> getEnvironment() {
        return batchModeAction.getEnvironment()
    }

    /**
     * {@inheritDoc}
     */
    void setEnvironment(Map<String, ?> environmentVariables) {
        batchModeAction.setEnvironment(environmentVariables)
    }

    /**
     * {@inheritDoc}
     */
    T environment(String name, Object value) {
        batchModeAction.environment(name, value)
        return taskType.cast(this)
    }

    /**
     * {@inheritDoc}
     */
    T environment(Map<String, ?> environmentVariables) {
        batchModeAction.environment(environmentVariables)
        return taskType.cast(this)
    }

    /**
     * {@inheritDoc}
     */
    T copyTo(ProcessForkOptions target) {
        batchModeAction.copyTo(target)
        return taskType.cast(this)
    }

    /**
     * {@inheritDoc}
     */
    T setStandardInput(InputStream inputStream) {
        batchModeAction.setStandardInput(inputStream)
        return taskType.cast(this)
    }

    /**
     * {@inheritDoc}
     */
    @Internal
    InputStream getStandardInput() {
        return batchModeAction.getStandardInput()
    }

    /**
     * {@inheritDoc}
     */
    T setStandardOutput(OutputStream outputStream) {
        batchModeAction.setStandardOutput(outputStream)
        return taskType.cast(this)
    }

    /**
     * {@inheritDoc}
     */
    @Internal
    OutputStream getStandardOutput() {
        return batchModeAction.getStandardOutput()
    }

    /**
     * {@inheritDoc}
     */
    T setErrorOutput(OutputStream outputStream) {
        batchModeAction.setErrorOutput(outputStream)
        return taskType.cast(this)
    }

    /**
     * {@inheritDoc}
     */
    @Internal
    OutputStream getErrorOutput() {
        return batchModeAction.getErrorOutput()
    }

    /**
     * {@inheritDoc}
     */
    T setIgnoreExitValue(boolean ignoreExitValue) {
        batchModeAction.setIgnoreExitValue(ignoreExitValue)
        return taskType.cast(this)
    }

    /**
     * {@inheritDoc}
     */
    @Input
    boolean isIgnoreExitValue() {
        return batchModeAction.isIgnoreExitValue()
    }

    void setExecAction(BatchModeAction batchModeAction) {
        this.batchModeAction = batchModeAction
    }

    /**
     * Returns the result for the command run by this task. Returns {@code null} if this task has not been executed yet.
     *
     * @return The result. Returns {@code null} if this task has not been executed yet.
     */
    @Internal
    ExecResult getBatchModeResult() {
        return batchModeResult
    }
}
