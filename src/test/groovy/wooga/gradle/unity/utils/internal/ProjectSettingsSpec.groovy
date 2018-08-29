package wooga.gradle.unity.utils.internal

import spock.lang.Unroll

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
}
