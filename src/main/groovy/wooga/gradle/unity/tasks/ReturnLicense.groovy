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

import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import wooga.gradle.unity.tasks.internal.AbstractUnityActivationTask

/**
 * Returns the currently active license to the license server.
 * Example:
 * <pre>
 * {@code
 *    task activateUnity(type:wooga.gradle.unity.tasks.ReturnLicense) {
 *        licenseDirectory = new File("path/to/license/directory")
 *    }
 * }
 * </pre>
 */
class ReturnLicense extends AbstractUnityActivationTask {

    ReturnLicense() {
        super(ReturnLicense.class)
    }

    private File dir

    /**
     * A file pointing to the unity license directory.
     * Defaults to "/Library/Application Support/Unity/" on macOS and "C:\ProgramData\Unity" on windows.
     * @return file to unity license directory
     */
    @SkipWhenEmpty
    @InputDirectory
    File getLicenseDirectory() {
        return dir
    }

    //TODO check if we actually need to set the path.
    /**
     * Sets the path to the unity license directory
     * @param value File object pointing to a valid unity license directory.
     */
    void setLicenseDirectory(File value) {
        dir = value
    }

    //TODO check if we actually need to set the path.
    /**
     * Sets the path to the unity license directory
     * @param value File object pointing to a valid unity license directory.
     */
    ReturnLicense licenseDirectory(File value) {
        setLicenseDirectory(value)
        this
    }

    @TaskAction
    void returnLicense() {
        activationAction.returnLicense()
    }
}
