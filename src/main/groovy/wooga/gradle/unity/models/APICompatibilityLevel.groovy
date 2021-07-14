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

import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional

import java.security.InvalidKeyException

/**
 * .NET profile support
 *
 * Unity supports a number of .NET profiles. Each profile provides a different API surface for C# code
 * which interacts with the .NET class libraries.
 *
 * The legacy scripting runtime supports two different profiles:
 * NET 2.0 Subset and .NET 2.0. Both of these are closely aligned with the .NET 2.0 profile from Microsoft.
 * The .NET 2.0 Subset profile is smaller than the .NET 4.x profile, and it allows access to the class library
 * APIs that most Unity projects use. It is the ideal choice for size-constrained platforms, such as mobile,
 * and it provides a set of portable APIs for multiplatform support. By default, most Unity projects should
 * use the .NET Standard 2.0 profile.
 *
 * The stable scripting runtime supports two different profiles: .NET Standard 2.0 and .NET 4.x.
 * The name of the .NET Standard 2.0 profile can be a bit misleading because it is not related to the .NET 2.0 and
 * .NET 2.0 Subset profile from the legacy scripting runtime. Instead, Unity’s support for the .NET Standard 2.0 profile
 * matches the profile of the same name published by the .NET Foundation. The .NET 4.x profile in Unity matches the
 * .NET 4 series (.NET 4.5, .NET 4.6, .NET 4.7, and so on) of profiles from the .NET Framework.
 *
 * Only use the .NET 4.x profile for compatibility with external libraries,
 * or when you require functionality that is not available in .NET Standard 2.0.
 *
 * Unity aims to support the vast majority of the APIs in the .NET Standard 2.0 profile on all platforms.
 * While not all platforms fully support the .NET Standard, libraries which aim for cross-platform compatibility should
 * target the .NET Standard 2.0 profile.
 * The .NET 4.x profile includes a much larger API surface, including parts which may work on few or no platforms.
 **/
enum APICompatibilityLevel {

    net_2_0(1),
    net_2_0_subset(2),
    net_4_6(3),
    net_web(4),
    net_micro(5),
    net_standard_2_0(6) // DEFAULT

    /**
     * The key in the Unity project's ProjectSettings.asset file.
     * apiCompatibilityLevelPerPlatform:
     *   Android: 3
     *   Standalone: 6
     *   iPhone: 3
     */
    static final String unityProjectSettingsPropertyKey = "apiCompatibilityLevelPerPlatform"
    /**
     The default API compatibility level used by Unity
     */
    static final APICompatibilityLevel defaultLevel = net_standard_2_0

    private static Map intToValue = new HashMap<>();
    private static Map fallbacks = new HashMap<>()

    static {
        for (APICompatibilityLevel apiLevel : values()) {
            intToValue.put(apiLevel.value, apiLevel);
        }

        fallbacks.put("net2_0", APICompatibilityLevel.net_2_0)
        fallbacks.put("net2_0_subset", APICompatibilityLevel.net_2_0_subset)
        fallbacks.put("net4_6", APICompatibilityLevel.net_4_6)
    }

    APICompatibilityLevel(Integer value) {
        this.value = value
    }

    private final Integer value

    Integer getValue() {
        value
    }

    static APICompatibilityLevel valueOfInt(Integer value) {
        if (!intToValue.containsKey(value)) {
            throw new InvalidKeyException("There is no API compatibility level for the value ${value}")
        }
        return (APICompatibilityLevel) intToValue.get(value);
    }

    static Map<String, APICompatibilityLevel> toMap(APICompatibilityLevel level) {
        Map<String, APICompatibilityLevel> map = [:]
        for (group in SupportedBuildTargetGroup.values()) {
            map.put(group.toString(), level)
        }
        return map
    }

    static APICompatibilityLevel parse(String value) {
        if (fallbacks.containsKey(value)) {
            return fallbacks[value]
        }
        return value as APICompatibilityLevel
    }

}

enum SupportedBuildTargetGroup {
    Standalone,
    Android,
    iPhone,
}
