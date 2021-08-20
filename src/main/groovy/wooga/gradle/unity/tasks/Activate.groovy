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

package wooga.gradle.unity.tasks

import wooga.gradle.unity.UnityTask
import wooga.gradle.unity.models.DefaultUnityAuthentication
import wooga.gradle.unity.models.UnityCommandLineOption
import wooga.gradle.unity.traits.UnityAuthenticationSpec

/**
 * Activates a Unity installation with unity account and a serial number.
 * Example:
 * <pre>
 * {@code
 *     task activateUnity(type:wooga.gradle.unity.tasks.Activate) {*         authentication {*             username = "user@something.com"
 *             password = "thePassword"
 *             serial = "unitySerialNumber"
 *}*}*}
 * </pre>
 *
 * Make sure that license file folder exists, and has appropriate permissions before running this task.
 */
class Activate extends UnityTask implements UnityAuthenticationSpec {

    Activate() {
        description = " Activates a Unity installation with unity account and a serial number"
        authentication = new DefaultUnityAuthentication(project.objects)
        onlyIf({

            // If all 3 haven't been assigned, don't throw since this is optional
            if (!authentication.username.isPresent()
                    && !authentication.password.isPresent()
                    && !authentication.serial.isPresent()) {
                logger.warn("No authentication fields have been set")
                return false
            }

            if (!authentication.username.get()) {
                throw new Exception("The username is missing")
            }

            if (!authentication.password.get()) {
                throw new Exception("The password is missing")
            }
            if (!authentication.serial.get()) {
                throw new Exception("The serial is missing")
            }

            return true
        })
    }

    @Override
    protected void preExecute() {
        super.preExecute()
        setCommandLineOptionConvention(UnityCommandLineOption.userName, authentication.username)
        setCommandLineOptionConvention(UnityCommandLineOption.password, authentication.password)
        setCommandLineOptionConvention(UnityCommandLineOption.serial, authentication.serial)
    }
}
