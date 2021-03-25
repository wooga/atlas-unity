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


import spock.lang.Unroll
import wooga.gradle.unity.APICompatibilityLevel
import wooga.gradle.unity.SupportedBuildTargetGroup

class ProjectSettingsSpec extends UnityAssetFileSpec {

    @Override
    Class<UnityAssetFile> getClassImp() {
        return ProjectSettings.class
    }

    static String TEMPLATE_CONTENT = """      
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

    static String TEMPLATE_CONTENT_FULL = """
    %YAML 1.1
    %TAG !u! tag:unity3d.com,2011:
    --- !u!129 &1
    PlayerSettings:
      m_ObjectHideFlags: 0
      serializedVersion: 15
      productGUID: 8da4962602f6644a0ba02ecd2fc41073
      AndroidProfiler: 0
      AndroidFilterTouchesWhenObscured: 0
      AndroidEnableSustainedPerformanceMode: 0
      defaultScreenOrientation: 0
      targetDevice: 2
      useOnDemandResources: 0
      accelerometerFrequency: 60
      companyName: Wooga GmbH
      productName: Tropicats D
      defaultCursor: {fileID: 0}
      cursorHotspot: {x: 0, y: 0}
      m_SplashScreenBackgroundColor: {r: 0.13725491, g: 0.12156863, b: 0.1254902, a: 1}
      m_ShowUnitySplashScreen: 0
      m_ShowUnitySplashLogo: 1
      m_SplashScreenOverlayOpacity: 1
      m_SplashScreenAnimation: 1
      m_SplashScreenLogoStyle: 1
      m_SplashScreenDrawMode: 0
      m_SplashScreenBackgroundAnimationZoom: 1
      m_SplashScreenLogoAnimationZoom: 1
      m_SplashScreenBackgroundLandscapeAspect: 1
      m_SplashScreenBackgroundPortraitAspect: 1
      m_SplashScreenBackgroundLandscapeUvs:
        serializedVersion: 2
        x: 0
        y: 0
        width: 1
        height: 1
    """.stripIndent()

    @Unroll
    def "parse playModeTestRunnerDisabled with #objectType"() {
        when:
        def settings = new ProjectSettings(content)

        then:
        !settings.playModeTestRunnerEnabled

        where:
        objectType | content
        "String"   | TEMPLATE_CONTENT
        "File"     | File.createTempFile("ProjectSettings", ".asset") << TEMPLATE_CONTENT
    }

    @Unroll
    def "parse playModeTestRunnerEnabled with #objectType"() {
        when:
        def settings = new ProjectSettings(content)

        then:
        settings.playModeTestRunnerEnabled

        where:
        objectType | content
        "String"   | TEMPLATE_CONTENT_ENABLED
        "File"     | File.createTempFile("ProjectSettings", ".asset") << TEMPLATE_CONTENT_ENABLED
    }

    def "playModeTestRunnerEnabled returns false with invalid project settings file"() {
        when: "initialize projectSettings with binary content"
        ProjectSettings settings = new ProjectSettings(content)

        then:
        !settings.playModeTestRunnerEnabled

        where:
        content << [INVALID_CONTENT]
    }

    @Unroll
    def "serializes file correctly"() {
        given: "a valid project settings file serialization"
        def settings = new ProjectSettings(file)

        and:
        assert settings.isValid()
        assert settings.isSerialized()

        when:
        settings.write()

        then:
        def updatedSettings = new ProjectSettings(file)
        assert updatedSettings.isValid()
        assert updatedSettings.isSerialized()
        assert settings ==  updatedSettings

        where:
        objectType | file
        "File"     | File.createTempFile("ProjectSettings", ".asset") << TEMPLATE_CONTENT_FULL
    }

    @Unroll
    def "sets api compatibility level #level"() {
        given:
        def settings = new ProjectSettings(content)

        and:
        assert settings.isValid()

        when:
        settings.setAPICompatibilityLevelForSupportedPlatforms(level)

        then:
        def apiCompLevel = settings.getAPICompatibilityLevelPerPlatform()
        assert apiCompLevel != null
        !apiCompLevel.isEmpty()
        assert apiCompLevel.size() == SupportedBuildTargetGroup.values().size()
        for(target in apiCompLevel.keySet()){
            assert apiCompLevel[target] == level
        }

        where:
        objectType | content | level
        "String"   | TEMPLATE_CONTENT | APICompatibilityLevel.net_4_6
        "String"   | TEMPLATE_CONTENT | APICompatibilityLevel.net_standard_2_0
    }

    @Unroll
    def "writes api compatibility level '#level' to file"() {
        given:
        def settings = new ProjectSettings(file)

        and:
        assert settings.isValid()
        assert settings.isSerialized()

        when:
        settings.setAPICompatibilityLevelForSupportedPlatforms(level)

        then:
        def apiCompLevel = settings.getAPICompatibilityLevelPerPlatform()
        !apiCompLevel.isEmpty()
        for(target in apiCompLevel.keySet()) {
            assert apiCompLevel[target] == level
        }
        settings.write()

        def updatedSettings = new ProjectSettings(file)
        assert settings == updatedSettings

        where:
        objectType | file | level
        "File"     | File.createTempFile("ProjectSettings", ".asset") << TEMPLATE_CONTENT | APICompatibilityLevel.net_standard_2_0
        "File"     | File.createTempFile("ProjectSettings", ".asset") << TEMPLATE_CONTENT | APICompatibilityLevel.net_4_6
    }
}
