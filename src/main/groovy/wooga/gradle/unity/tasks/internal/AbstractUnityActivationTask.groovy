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

package wooga.gradle.unity.tasks.internal

import org.gradle.api.internal.ConventionMapping
import org.gradle.api.tasks.Input
import org.gradle.internal.Factory
import org.gradle.process.ExecResult
import org.gradle.process.internal.ExecException
import wooga.gradle.unity.batchMode.ActivationAction
import wooga.gradle.unity.batchMode.BaseBatchModeSpec
import wooga.gradle.unity.internal.UnityPluginActionExtension

abstract class AbstractUnityActivationTask<T extends AbstractUnityActivationTask> extends AbstractUnityTask {

    @Override
    protected BaseBatchModeSpec retrieveAction() {
        return activationAction
    }

    @Override
    ConventionMapping getConventionMapping() {
        return activationAction.conventionMapping
    }

    interface ExecuteExclude {
        ExecResult activate() throws ExecException

        ExecResult returnLicense() throws ExecException
    }

    @Delegate(excludeTypes = [ExecuteExclude.class], interfaces = false)
    protected ActivationAction activationAction

    AbstractUnityActivationTask(Class taskType) {
        super(taskType)
        this.activationAction = retrieveActivationActionFactory().create()
    }

    protected Factory<ActivationAction> retrieveActivationActionFactory() {
        return project.extensions.getByType(UnityPluginActionExtension).activationActionFactory
    }

    /**
     * Tells whether a non-zero exit value is ignored, or an exception thrown. Defaults to <code>false</code>.
     *
     * @return whether a non-zero exit value is ignored, or an exception thrown
     */
    @Input
    boolean isIgnoreExitValue() {
        return activationAction.isIgnoreExitValue()
    }
}
