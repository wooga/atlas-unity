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

package wooga.gradle.unity.internal

import wooga.gradle.unity.UnityAuthentication

class DefaultUnityAuthentication implements UnityAuthentication {

    static final String UNITY_PASSWORD_ENV = "UNITY_PWD"
    static final String UNITY_USER_ENV = "UNITY_USR"
    static final String UNITY_SERIAL_ENV = "UNITY_SERIAL"

    static final String UNITY_PASSWORD_PROPERTY = "unity.password"
    static final String UNITY_USER_PROPERTY = "unity.username"
    static final String UNITY_SERIAL_PROPERTY = "unity.serial"

    private Map<String, ?> env
    private Map<String, ?> properties
    private String username
    private String password
    private String serial

    String getUsername() {
        if (this.username) {
            return username
        }
        return properties[UNITY_USER_PROPERTY] ?: env[UNITY_USER_ENV]
    }

    void setUsername(String username) {
        this.username = username
    }

    String getPassword() {
        if (this.password) {
            return password
        }
        return properties[UNITY_PASSWORD_PROPERTY] ?: env[UNITY_PASSWORD_ENV]
    }

    void setPassword(String password) {
        this.password = password
    }

    String getSerial() {
        if (this.serial) {
            return serial
        }
        return properties[UNITY_SERIAL_PROPERTY] ?: env[UNITY_SERIAL_ENV]
    }

    void setSerial(String serial) {
        this.serial = serial
    }

    DefaultUnityAuthentication(Map<String, ?> properties, Map<String, ?> env) {
        this.properties = properties
        this.env = env
    }

    DefaultUnityAuthentication(String username, String password, String serial) {
        this.username = username
        this.password = password
        this.serial = serial
        this.env = [:]
        this.properties = [:]
    }

    @Override
    String getName() {
        return "unity"
    }
}
