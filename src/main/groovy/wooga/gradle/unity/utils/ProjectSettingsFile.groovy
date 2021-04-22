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


package wooga.gradle.unity.utils

import groovy.transform.InheritConstructors
import wooga.gradle.unity.models.APICompatibilityLevel
import wooga.gradle.unity.models.SupportedBuildTargetGroup

@InheritConstructors
class ProjectSettingsFile extends UnityAssetFile {

    static String DEFAULT_TEMPLATE_CONTENT = """      
    %YAML 1.1
    %TAG !u! tag:unity3d.com,2011:
    --- !u!129 &1
    PlayerSettings:
      wiiUDrcBufferDisabled: 0
      wiiUProfilerLibPath: 
      playModeTestRunnerEnabled: 0
      ${APICompatibilityLevel.unityProjectSettingsPropertyKey}: 
        Standalone: 6
        iPhone: 6
        Android: 6
      actionOnDotNetUnhandledException: 1
    
    """.stripIndent()

    static String TEMPLATE_CONTENT_ENABLED = """      
    %YAML 1.1
    %TAG !u! tag:unity3d.com,2011:
    --- !u!129 &1
    PlayerSettings:
      wiiUDrcBufferDisabled: 0
      wiiUProfilerLibPath: 
      playModeTestRunnerEnabled: 1
      actionOnDotNetUnhandledException: 1
    
    """.stripIndent()

    public static final String filePath = "ProjectSettings/ProjectSettings.asset"

    boolean getPlayModeTestRunnerEnabled() {
        content['playModeTestRunnerEnabled'] && content['playModeTestRunnerEnabled'] == 1
    }

    Map<String, APICompatibilityLevel> getAPICompatibilityLevelPerPlatform() {
        Map value = getSerializedAPICompatibilityLevelPerPlatform()
        if (value){
            Map<String, APICompatibilityLevel> conversion = value.collectEntries
                    { [ it.key, APICompatibilityLevel.valueOfInt(it.value) ]
            }
            return conversion
        }
        return [:]
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
