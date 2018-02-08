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

import org.gradle.api.Action
import org.gradle.api.internal.ConventionMapping
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.Factory
import org.gradle.process.ExecResult
import org.gradle.process.internal.ExecException
import wooga.gradle.unity.UnityAuthentication
import wooga.gradle.unity.UnityPluginExtension
import wooga.gradle.unity.batchMode.ActivationAction
import wooga.gradle.unity.batchMode.ActivationSpec
import wooga.gradle.unity.batchMode.BaseBatchModeSpec
import wooga.gradle.unity.tasks.internal.AbstractUnityTask

class Activate extends AbstractUnityTask implements ActivationSpec {

    @Override
    BaseBatchModeSpec retrieveAction() {
        return activationAction
    }

    @Override
    ConventionMapping getConventionMapping() {
        return activationAction.conventionMapping
    }

    private interface ExecuteExclude {
        ExecResult execute() throws ExecException
    }

    @Delegate(excludeTypes = [ExecuteExclude.class], interfaces = false)
    ActivationAction activationAction

    private ExecResult batchModeResult

    protected Factory<ActivationAction> retrieveActivationActionFactory() {
        return project.extensions.getByType(UnityPluginExtension).activationActionFactory
    }

    Activate() {
        super(Activate.class)
        this.activationAction = retrieveActivationActionFactory().create()
        onlyIf(new Spec<Activate>() {
            @Override
            boolean isSatisfiedBy(Activate task) {
                return ((task.userName && !task.userName.isEmpty())
                        && (task.password && !task.password.isEmpty())
                        && (task.serial && !task.serial.isEmpty()))
            }
        })
    }

    @TaskAction
    protected void activate() {
        batchModeResult = activationAction.activate()
    }

    @Input
    String getUserName() {
        return authentication.username
    }

    @Input
    String getPassword() {
        return authentication.password
    }

    @Optional
    @Input
    String getSerial() {
        return authentication.serial
    }

    @Override
    Activate authentication(Closure closure) {
        activationAction.authentication(closure)
        return this
    }

    @Override
    Activate authentication(Action<? super UnityAuthentication> action) {
        activationAction.authentication(action)
        return this
    }

    /**
     * {@inheritDoc}
     */
    @Input
    boolean isIgnoreExitValue() {
        return activationAction.isIgnoreExitValue()
    }
}
