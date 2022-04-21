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

package wooga.gradle.unity.traits

import com.wooga.gradle.BaseSpec
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.SkipWhenEmpty

trait UnityLicenseSpec extends BaseSpec {

    private DirectoryProperty licenseDirectory = objects.directoryProperty()
    /**
     * The Unity license directory.
     * Defaults to "/Library/Application Support/Unity/" on macOS and "C:\ProgramData\Unity" on windows.
     * @return Reference to Unity license directory
     */
    @SkipWhenEmpty
    @InputDirectory
    DirectoryProperty getLicenseDirectory() {
        licenseDirectory
    }
    void setLicenseDirectory(Provider<Directory> value){
        licenseDirectory.set(value)
    }

}
