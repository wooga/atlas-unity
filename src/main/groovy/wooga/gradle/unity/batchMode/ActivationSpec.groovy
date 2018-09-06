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

import org.gradle.api.Action
import wooga.gradle.unity.UnityAuthentication

/**
 * Specification for a activation action.
 */
interface ActivationSpec extends BaseBatchModeSpec {

    /**
     * Returns the {@link wooga.gradle.unity.UnityAuthentication} object.
     * <p>
     * This object contains the user and serial credentials used to activate the Unity installation.
     * @return the Unity authentication credentials
     * @default empty credentials object
     */
    UnityAuthentication getAuthentication()

    /**
     * Sets the authentication object.
     * @param authentication
     */
    void setAuthentication(UnityAuthentication authentication)

    /**
     * Configures the activation credentials.
     * <pre>
     * {@code
     *     authentication {
     *         username = "user@something.com"
     *         password = "thePassword"
     *         serial = "unitySerialNumber"
     *     }
     * }
     * </pre>
     * @param closure the configuration closure
     * @return the activation task
     */
    ActivationSpec authentication(Closure closure)

    /**
     * Configures the activation credentials.
     * <pre>
     * {@code
     *     this.authentication(new Action<UnityAuthentication>() {
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
    ActivationSpec authentication(Action<? super UnityAuthentication> action)
}
