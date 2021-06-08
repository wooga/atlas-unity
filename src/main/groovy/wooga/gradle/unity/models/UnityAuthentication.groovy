/*
 * Copyright 2021 Wooga GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package wooga.gradle.unity.models

import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

trait UnityAuthentication {

    private final Property<String> username

    /**
     * Returns the unity account username.
     * <p>
     * The value can be set in multiple ways (gradle properties, environment variable, parameter in code)
     * The precedence order is:
     * <ul>
     *    <li><b>direct parameter in code</b>
     *    <li><b>gradle properties</b>
     *    <li><b>environment variables</b>
     *    <li><b>hardcoded value</b>
     * </ul>
     * @return the username
     */
    @Input
    @Optional
    Property<String> getUsername() {
        username
    }

    void setUsername(Provider<String> value) {
        username.set(value)
    }

    void setUsername(String value) {
        username.set(value)
    }

    private final Property<String> password

    /**
     * Returns the unity account password.
     * <p>
     * The value can be set in multiple ways (gradle properties, environment variable, parameter in code)
     * The precedence order is:
     * <ul>
     *    <li><b>direct parameter in code</b>
     *    <li><b>gradle properties</b>
     *    <li><b>environment variables</b>
     *    <li><b>hardcoded value</b>
     * </ul>
     * @return the password
     */
    @Input
    @Optional
    Property<String> getPassword() {
        password
    }

    void setPassword(Provider<String> value) {
        password.set(value)
    }

    void setPassword(String value) {
        password.set(value)
    }

    private final Property<String> serial

    /**
     * Returns the Unity serial number.
     * <p>
     * The value can be set in multiple ways (gradle properties, environment variable, parameter in code)
     * The precedence order is:
     * <ul>
     *    <li><b>direct parameter in code</b>
     *    <li><b>gradle properties</b>
     *    <li><b>environment variables</b>
     *    <li><b>hardcoded value</b>
     * </ul>
     * @return the serial number
     */
    @Input
    @Optional
    Property<String> getSerial() {
        serial
    }

    void setSerial(Provider<String> value) {
        serial.set(value)
    }

    void setSerial(String value) {
        serial.set(value)
    }

}
