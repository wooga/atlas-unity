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

interface UnityAuthentication {

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
     * @see UnityPluginConsts#UNITY_USER_PROPERTY
     * @see UnityPluginConsts#UNITY_USER_ENV
     */
    String getUsername()

    /**
     * Sets username value
     * @param username the username
     */
    void setUsername(String username)

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
     * @see UnityPluginConsts#UNITY_PASSWORD_PROPERTY
     * @see UnityPluginConsts#UNITY_PASSWORD_ENV
     */
    String getPassword()

    /**
     * Sets the password
     * @param password
     */
    void setPassword(String password)

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
     * @see UnityPluginConsts#UNITY_SERIAL_PROPERTY
     * @see UnityPluginConsts#UNITY_SERIAL_ENV
     */
    String getSerial()

    /**
     * Sets the serial
     * @param serial the serial number
     * @return
     */
    void setSerial(String serial)
}
