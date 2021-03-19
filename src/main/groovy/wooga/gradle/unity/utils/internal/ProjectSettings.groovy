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


package wooga.gradle.unity.utils.internal

import groovy.transform.InheritConstructors
import wooga.gradle.unity.APICompatibilityLevel
import wooga.gradle.unity.SupportedBuildTargetGroup
import wooga.gradle.unity.utils.GenericUnityAsset

@InheritConstructors
class ProjectSettings extends UnityAssetFile {

    boolean getPlayModeTestRunnerEnabled() {
        content['playModeTestRunnerEnabled'] && content['playModeTestRunnerEnabled'] == 1
    }

    Map<String, APICompatibilityLevel> getAPICompatibilityLevelPerPlatform() {
        Map value = getSerializedAPICompatibilityLevelPerPlatform()
        Map<String, APICompatibilityLevel> conversion = value.collectEntries
                { [ it.key, APICompatibilityLevel.valueOfInt(it.value) ]
        }
        return conversion
    }

    boolean setAPICompatibilityLevelForSupportedPlatforms(APICompatibilityLevel level) {
        Map map = getSerializedAPICompatibilityLevelPerPlatform()

        if (map == null) {
            return false
        }
        for (group in SupportedBuildTargetGroup.values()) {
            map.put(group.toString(), level.value)
        }
    }

    private Map<String, Integer> getSerializedAPICompatibilityLevelPerPlatform() {
        content[APICompatibilityLevel.unityProjectSettingsPropertyKey]
    }

    void setAPICompatibilityLevelForSupportedPlatforms(Map value) {
        content[APICompatibilityLevel.unityProjectSettingsPropertyKey] = value.collectEntries
                { [ it.key,
                   it.value instanceof APICompatibilityLevel
                           ? ((APICompatibilityLevel)it.value).value : it.value ]}
    }
}
