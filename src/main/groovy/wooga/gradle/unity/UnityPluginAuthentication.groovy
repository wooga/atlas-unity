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

package wooga.gradle.unity

import org.gradle.api.Action

interface UnityPluginAuthentication {

    /**
     * Returns a {@code true} if the Unity license should be returned after all Unity tasks have been executed.
     * @return {@code true} if the license should be auto returned
     * @default true
     * @see wooga.gradle.unity.tasks.ReturnLicense
     */
    Boolean getAutoReturnLicense()

    /**
     * Sets the {@code autoReturnLicense} flag.
     * @param value {@code true} if the license should be auto returned
     */
    void setAutoReturnLicense(Boolean value)

    /**
     * Sets the {@code autoReturnLicense} flag.
     * @param value {@code true} if the license should be auto returned
     * @return this
     */
    UnityPluginAuthentication autoReturnLicense(Boolean value)

    /**
     * Returns a {@code true} if the Unity should be activated before the first Unity task starts executing.
     * @return {@code true} if the should be auto activated
     * @default true
     * @see wooga.gradle.unity.tasks.Activate
     */
    Boolean getAutoActivateUnity()

    /**
     * Sets the {@code autoActivateUnity} flag.
     * @param value {@code true} if the should be auto activated
     */
    void setAutoActivateUnity(Boolean value)

    /**
     * Sets the {@code autoActivateUnity} flag.
     * @param value {@code true} if the should be auto activated
     * @return this
     */
    abstract UnityPluginAuthentication autoActivateUnity(Boolean value)

    /**
     * Returns the {@link wooga.gradle.unity.UnityAuthentication} object.
     * <p>
     * This object contains the user and serial credentials used to activate the Unity installation.
     * @return the Unity authentication credentials
     * @default empty credentials object
     */
    UnityAuthentication getAuthentication()

    /**
     * Sets the authentication object values with a {@link wooga.gradle.unity.UnityAuthentication} object.
     * <p>
     * The authentication values within the provided {@link wooga.gradle.unity.UnityAuthentication} object will be copied
     * to the internal {@link wooga.gradle.unity.UnityAuthentication authentication} object.
     * @param authentication
     */
    void setAuthentication(UnityAuthentication authentication)

    /**
     * Sets the authentication object values with a {@link wooga.gradle.unity.UnityAuthentication} object.
     * <p>
     * The authentication values within the provided {@link wooga.gradle.unity.UnityAuthentication} object will be copied
     * to the internal {@link wooga.gradle.unity.UnityAuthentication authentication} object.
     * @param authentication
     * @return this
     */
    abstract UnityPluginAuthentication authentication(UnityAuthentication authentication)

    /**
     * Configures the {@link wooga.gradle.unity.UnityAuthentication authentication} object with a closure.
     * <p>
     * The closure configures a {@link wooga.gradle.unity.UnityAuthentication} object.
     *
     * @param closure The closure for configuring the authentication.
     * @return this
     */
    abstract UnityPluginAuthentication authentication(Closure closure)

    /**
     * Configures the {@link wooga.gradle.unity.UnityAuthentication authentication} object with an action.
     * <p>
     * The given action configures a {@link wooga.gradle.unity.UnityAuthentication} object.
     *
     * @param action The action for configuring the authentication.
     * @return this
     */
    abstract UnityPluginAuthentication authentication(Action<? super UnityAuthentication> action)
}