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

import org.gradle.process.BaseExecSpec

trait BatchModeSpec<T extends BatchModeSpec> extends BaseBatchModeSpec implements BaseExecSpec {

    /**
     * Returns the arguments for the command to be executed. Defaults to an empty list.
     */
    List<String> args

    /**
     * Adds arguments for the command to be executed.
     *
     * @param args args for the command
     * @return this
     */
    abstract T args(Object... args)

    /**
     * Adds arguments for the command to be executed.
     *
     * @param args args for the command
     * @return this
     */
    abstract T args(Iterable<?> args)

    /**
     * Sets the arguments for the command to be executed.
     *
     * @param args args for the command
     * @return this
     */
    abstract void setArgs(Iterable<?> args)

    /**
     * Returns the build target for the current task.
     * @return the build target
     * @see wooga.gradle.unity.batchMode.BuildTarget
     */
    BuildTarget buildTarget

    /**
     * Sets the build target for the current task.
     * @param target the build target
     * @return this
     * @see wooga.gradle.unity.batchMode.BuildTarget
     */
    abstract T buildTarget(BuildTarget target)

    /**
     * Returns a {@code Boolean} value indicating if Unity should shutdown after task execution.
     * @see wooga.gradle.unity.batchMode.BatchModeFlags#QUIT
     */
    Boolean quit

    /**
     * Sets or Unsets the {@code quit} flag for the current task
     * @param value when {@code true} Unity will quit after task execution.
     * @return this
     * @see wooga.gradle.unity.batchMode.BatchModeFlags#QUIT
     */
    abstract T quit(Boolean value)

    /**
     * Returns a {@code Boolean} indicating if Unity should operate in {@code batchmode}.
     * @see wooga.gradle.unity.batchMode.BatchModeFlags#BATCH_MODE
     */
    Boolean batchMode

    /**
     * Sets or Unsets the {@code -batchmode} flag for the current task
     * @param value when {@code true} Unity will execute task in batchmode.
     * @return this
     * @see wooga.gradle.unity.batchMode.BatchModeFlags#BATCH_MODE
     */
    abstract T batchMode(Boolean value)

    /**
     * Returns a {@code Boolean} indicating if Unity should operate in {@code nographics} mode.
     * @see wooga.gradle.unity.batchMode.BatchModeFlags#NO_GRAPHICS
     */
    Boolean noGraphics

    /**
     * Sets or Unsets the {@code -nographics} flag for the current task
     * @param value when {@code true} Unity will run with limited GPU features.
     * @return this
     * @see wooga.gradle.unity.batchMode.BatchModeFlags#NO_GRAPHICS
     */
    abstract T noGraphics(Boolean value)
}
