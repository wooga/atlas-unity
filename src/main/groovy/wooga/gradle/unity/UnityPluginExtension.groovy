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
import org.gradle.process.ExecResult
import wooga.gradle.unity.batchMode.ActivationSpec
import wooga.gradle.unity.batchMode.BaseBatchModeSpec
import wooga.gradle.unity.batchMode.BatchModeSpec
import wooga.gradle.unity.batchMode.BuildTarget

/**
 * Extension point for the unity plugin.
 */
interface UnityPluginExtension<T extends UnityPluginExtension> extends UnityPluginConvention, UnityPluginAuthentication {

    /**
     * Executes a command in Unity.
     * <p>
     * The closure configures a {@link wooga.gradle.unity.batchMode.BatchModeSpec}.
     *
     * @param closure The closure for configuring the execution.
     * @return the result of the execution
     */
    ExecResult batchMode(Closure closure)

    /**
     * Executes a command in Unity.
     * <p>
     * The given action configures a {@link wooga.gradle.unity.batchMode.BatchModeSpec}, which is used to launch the process.
     * This method blocks until the process terminates, with its result being returned.
     *
     * @param action The action for configuring the execution.
     * @return the result of the execution
     */
    ExecResult batchMode(Action<? super BatchModeSpec> action)

    /**
     * Executes Unity license activation.
     * <p>
     * The closure configures a {@link wooga.gradle.unity.batchMode.ActivationSpec}.
     * @param closure The closure for configuring the activation.
     * @return the result of the execution
     */
    ExecResult activate(Closure closure)

    /**
     * Executes Unity license activation.
     * <p>
     * The given action configures a {@link wooga.gradle.unity.batchMode.ActivationSpec}, which is used to launch the process.
     * This method blocks until the process terminates, with its result being returned.
     *
     * @param action The action for configuring the execution.
     * @return the result of the execution
     */
    ExecResult activate(Action<? super ActivationSpec> action)

    /**
     * Executes Unity license return command.
     * <p>
     * The closure configures a {@link wooga.gradle.unity.batchMode.BaseBatchModeSpec}.
     * @param closure The closure for configuring the activation.
     * @return the result of the execution
     */
    ExecResult returnLicense(Closure closure)

    /**
     * Executes Unity license activation.
     * <p>
     * The given action configures a {@link wooga.gradle.unity.batchMode.BaseBatchModeSpec}, which is used to launch the process.
     * This method blocks until the process terminates, with its result being returned.
     *
     * @param action The action for configuring the execution.
     * @return the result of the execution
     */
    ExecResult returnLicense(Action<? super BaseBatchModeSpec> action)

    /**
     * Returns a {@link java.util.Set} of {@link wooga.gradle.unity.batchMode.BuildTarget} objects to construct unity
     * editmode/playmode tasks.
     * <p>
     * The plugin constructs a series of test tasks based on the returned {@link java.util.Set set}.
     * <p>
     * <b>Example Test Task structure with two test build targets:</b>
     * <pre>
     * {@code
     * :check
     * |--- :test
     * +--- :testEditMode
     * |    +--- :testEditModeAndroid
     * |    |--- :testEditModeIos
     * |--- :testPlayMode
     *      +--- :testPlayModeAndroid
     *      |--- :testPlayModeIos
     * }
     *
     * @return the buildtargets to generate test tasks for
     * @default the a set with the {@code defaultBuildTarget}
     * @see wooga.gradle.unity.UnityPluginConvention#defaultBuildTarget
     */
    Set<BuildTarget> getTestBuildTargets()

    /**
     * Adds one or more test build target objects.
     * <p>
     * The provided objects can be a {@link groovy.lang.Closure}, {@link java.lang.String} or {@link wooga.gradle.unity.batchMode.BuildTarget} objects
     *
     * @param targets test build targets to add
     * @return this
     */
    T testBuildTargets(Object... targets)

    /**
     * Adds one or more test build target objects.
     * <p>
     * The provided objects can be a {@link groovy.lang.Closure}, {@link java.lang.String} or {@link wooga.gradle.unity.batchMode.BuildTarget} objects
     *
     * @param targets test build targets to add
     * @return this
     */
    T testBuildTargets(Iterable<?> targets)

    /**
     * Sets one or more test build target objects.
     * <p>
     * The provided objects can be a {@link groovy.lang.Closure}, {@link java.lang.String} or {@link wooga.gradle.unity.batchMode.BuildTarget} objects
     *
     * @param targets test build targets to add
     * @return this
     */
    void setTestBuildTargets(Iterable<?> targets)

}
