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

import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import wooga.gradle.unity.models.APICompatibilityLevel

trait APICompatibilityLevelSpec extends UnityBaseSpec {

    MapProperty<String, APICompatibilityLevel> apiCompatibilityLevel = objects.mapProperty(String, APICompatibilityLevel)

    @Input
    @Optional
    MapProperty<String, APICompatibilityLevel> getApiCompatibilityLevel() {
        apiCompatibilityLevel
    }

    void setApiCompatibilityLevel(Provider<?> value) {
        apiCompatibilityLevel.set(value.map({
            Map<String, APICompatibilityLevel> map = null
            if (it instanceof APICompatibilityLevel){
                map = APICompatibilityLevel.toMap(it)
            }
            else if (it instanceof String){
                map = APICompatibilityLevel.toMap(APICompatibilityLevel.parse(it))
            }
            else if (it instanceof Map<String, APICompatibilityLevel>){
                map = it
            }
            map
        }))
    }

    void setApiCompatibilityLevel(Map<String, APICompatibilityLevel> value) {
        apiCompatibilityLevel.set(value)
    }

    void setApiCompatibilityLevel(APICompatibilityLevel value) {
        apiCompatibilityLevel.set(APICompatibilityLevel.toMap(value))
    }

    void setApiCompatibilityLevel(String value) {
        setApiCompatibilityLevel(APICompatibilityLevel.parse(value))
    }

    private RegularFileProperty settingsFile = objects.fileProperty()
    @InputFile
    RegularFileProperty getSettingsFile() {
        settingsFile
    }
    void setSettingsFile(Provider<RegularFile> value) {
        this.settingsFile.set(value)
    }
}
