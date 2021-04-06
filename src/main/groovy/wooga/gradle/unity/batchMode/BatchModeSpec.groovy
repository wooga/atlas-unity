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

package wooga.gradle.unity.batchMode

import org.gradle.process.BaseExecSpec

interface BatchModeSpec extends BaseBatchModeSpec ,BaseExecSpec {

    /**
     * Returns the arguments for the command to be executed. Defaults to an empty list.
     */
    List<String> getArgs()

    /**
     * Adds arguments for the command to be executed.
     *
     * @param args args for the command
     * @return this
     */
    BatchModeSpec args(Object... args)

    /**
     * Adds arguments for the command to be executed.
     *
     * @param args args for the command
     * @return this
     */
    BatchModeSpec args(Iterable<?> args)

    /**
     * Sets the arguments for the command to be executed.
     *
     * @param args args for the command
     * @return this
     */
    BatchModeSpec setArgs(Iterable<?> args)

    /**
     * Returns the build target for the current task.
     * @return the build target
     * @see wooga.gradle.unity.batchMode.BuildTarget
     */
    BuildTarget getBuildTarget()

    /**
     * Sets the build target for the current task.
     * @param target the build target
     * @see wooga.gradle.unity.batchMode.BuildTarget
     */
    void setBuildTarget(BuildTarget target)

    /**
     * Sets the build target for the current task.
     * @param target the build target
     * @return this
     * @see wooga.gradle.unity.batchMode.BuildTarget
     */
    BatchModeSpec buildTarget(BuildTarget target)

    /**
     * Returns a {@code Boolean} value indicating if Unity should shutdown after task execution.
     * @see wooga.gradle.unity.batchMode.BatchModeFlags#QUIT
     */
    Boolean getQuit()

    /**
     * Sets or unset the {@code quit} flag for the current task
     * @param value when {@code true} Unity will quit after task execution.
     * @see wooga.gradle.unity.batchMode.BatchModeFlags#QUIT
     */
    void setQuit(Boolean value)

    /**
     * Sets or unset the {@code quit} flag for the current task
     * @param value when {@code true} Unity will quit after task execution.
     * @return this
     * @see wooga.gradle.unity.batchMode.BatchModeFlags#QUIT
     */
    BatchModeSpec quit(Boolean value)

    /**
     * Returns a {@code Boolean} indicating if Unity should operate in {@code batchmode}.
     * @see wooga.gradle.unity.batchMode.BatchModeFlags#BATCH_MODE
     */
    Boolean getBatchMode()

    /**
     * Sets or unset the {@code -batchmode} flag for the current task
     * @param value when {@code true} Unity will execute task in batchmode.
     * @see wooga.gradle.unity.batchMode.BatchModeFlags#BATCH_MODE
     */
    void setBatchMode(Boolean value)

    /**
     * Sets or unset the {@code -batchmode} flag for the current task
     * @param value when {@code true} Unity will execute task in batchmode.
     * @return this
     * @see wooga.gradle.unity.batchMode.BatchModeFlags#BATCH_MODE
     */
    BatchModeSpec batchMode(Boolean value)

    /**
     * Returns a {@code Boolean} indicating if Unity should operate in {@code nographics} mode.
     * @see wooga.gradle.unity.batchMode.BatchModeFlags#NO_GRAPHICS
     */
    Boolean getNoGraphics()

    /**
     * Sets or unset the {@code -nographics} flag for the current task
     * @param value when {@code true} Unity will run with limited GPU features.
     * @return this
     * @see wooga.gradle.unity.batchMode.BatchModeFlags#NO_GRAPHICS
     */
    void setNoGraphics(Boolean value)

    /**
     * Sets or unset the {@code -nographics} flag for the current task
     * @param value when {@code true} Unity will run with limited GPU features.
     * @return this
     * @see wooga.gradle.unity.batchMode.BatchModeFlags#NO_GRAPHICS
     */
    BatchModeSpec noGraphics(Boolean value)

    Boolean getDisableAssemblyUpdater()
    void setDisableAssemblyUpdater(Boolean value)
    BatchModeSpec disableAssemblyUpdater(Boolean value)

    Boolean getDeepProfiling()
    void setDeepProfiling(Boolean value)
    BatchModeSpec deepProfiling(Boolean value)

    Boolean getEnableCodeCoverage()
    void setEnableCodeCoverage(Boolean value)
    BatchModeSpec enableCodeCoverage(Boolean value)
}
