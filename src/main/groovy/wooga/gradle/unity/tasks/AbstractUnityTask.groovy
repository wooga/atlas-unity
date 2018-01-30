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
import org.gradle.process.internal.ExecException
import wooga.gradle.unity.UnityPluginExtension
import wooga.gradle.unity.batchMode.BatchModeAction
import wooga.gradle.unity.batchMode.BatchModeSpec
import wooga.gradle.unity.batchMode.BuildTarget

abstract class AbstractUnityTask<T extends AbstractUnityTask> extends ConventionTask implements BatchModeSpec {

    static Logger logger = Logging.getLogger(AbstractUnityTask)

    private final Class<T> taskType

    private interface ExecuteExclude {
        ExecResult execute() throws ExecException
    }

    @Delegate(excludeTypes=[ExecuteExclude.class], interfaces = false)
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

    @Optional
    @Input
    @Override
    File getProjectPath() {
        batchModeAction.projectPath
    }

    @Optional
    @Internal
    @Override
    File getLogFile() {
        batchModeAction.logFile
    }

    @Optional
    @Input
    @Override
    BuildTarget getBuildTarget() {
        batchModeAction.buildTarget
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
    Boolean getQuit() {
        return batchModeAction.quit
    }

    @Optional
    @Input
    @Override
    Boolean getNoGraphics() {
        batchModeAction.noGraphics
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
    @Input
    boolean isIgnoreExitValue() {
        return batchModeAction.isIgnoreExitValue()
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
