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
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecResult
import wooga.gradle.unity.UnityAuthentication
import wooga.gradle.unity.batchMode.ActivationSpec
import wooga.gradle.unity.tasks.internal.AbstractUnityActivationTask

class Activate extends AbstractUnityActivationTask implements ActivationSpec {

    private ExecResult batchModeResult

    Activate() {
        super(Activate.class)
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
