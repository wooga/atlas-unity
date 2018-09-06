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

/**
 * Activates a Unity installation with unity account and a serial number.
 * Example:
 * <pre>
 * {@code
 *     task activateUnity(type:wooga.gradle.unity.tasks.Activate) {
 *         authentication {
 *             username = "user@something.com"
 *             password = "thePassword"
 *             serial = "unitySerialNumber"
 *         }
 *     }
 * }
 * </pre>
 *
 * Make sure that license file folder exists, and has appropriate permissions before running this task.
 */
class Activate extends AbstractUnityActivationTask implements ActivationSpec {

    /**
     * OnlyIf {@code Spec < Activate >} predicate type.
     */
    static class ActivateSpec implements Spec<Activate> {

        /**
         * Returns a {@code Boolean} value indicating if the given element is valid.
         * <p>
         * Checks if the {@link Activate} task properties are satisfied.
         * The method will check if the {@code username}, {@code password} and {@code serial} properties are provided.
         * If any of the parameters is either {@code empty} or {@code null} the method returns {@code false}.
         *
         * @param element the element to check if the predicate is satisfied
         * @return {@code true} if the predicate is satisfied.
         * @see wooga.gradle.unity.tasks.Activate#getUsername
         * @see wooga.gradle.unity.tasks.Activate#getPassword
         * @see wooga.gradle.unity.tasks.Activate#getSerial
         */
        boolean isSatisfiedBy(Activate element) {
            return ((element.username && !element.username.isEmpty())
                    && (element.password && !element.password.isEmpty())
                    && (element.serial && !element.serial.isEmpty()))
        }
    }

    private ExecResult batchModeResult

    Activate() {
        super(Activate.class)
        onlyIf(new ActivateSpec())
    }

    @TaskAction
    protected void activate() {
        batchModeResult = activationAction.activate()
    }

    /**
     * Returns the unity account username.
     * @return the username
     */
    @Input
    String getUsername() {
        return authentication.username
    }

    /**
     * Returns the unity account password.
     * @return the password
     */
    @Input
    String getPassword() {
        return authentication.password
    }

    /**
     * Returns the Unity serial number.
     * @return the serial number
     */
    @Optional
    @Input
    String getSerial() {
        return authentication.serial
    }

    /**
     * Configures the activation credentials.
     * <pre>
     * {@code
     *     authentication{
     *         username = "user@something.com"
     *         password = "thePassword"
     *         serial = "unitySerialNumber"
     *     }
     * }
     * </pre>
     * @param closure the configuration closure
     * @return the activation task
     */
    @Override
    Activate authentication(Closure closure) {
        activationAction.authentication(closure)
        return this
    }

    /**
     * Configures the activation credentials.
     * <pre>
     * {@code
     *     this.authentication ( new Action <UnityAuthentication> () {
     *         @Override
     *         void execute(UnityAuthentication authentication) {
     *             authentication.username = "user@something.com"
     *             authentication.password = "thePassword"
     *             authentication.serial = "unitySerialNumber"
     *         }
     *     }
     * }
     * </pre>
     * @param action the configuration action
     * @return the activation task
     */
    @Override
    Activate authentication(Action<? super UnityAuthentication> action) {
        activationAction.authentication(action)
        return this
    }
}
