package wooga.gradle.unity.utils

import spock.lang.Specification
import spock.lang.Shared
import spock.lang.Unroll

class ProjectSettingsSpec extends Specification {

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
    def "initialize with #objectType"() {
        expect:
        new ProjectSettings(content)

        where:
        objectType | content
        "String"   | TEMPLATE_CONTENT
        "File"     | File.createTempFile("ProjectSettings", ".asset") << TEMPLATE_CONTENT
    }

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

    def "strip unity instructions from content string"() {
        given: "a test content"
        def content = """      
        %YAML 1.1
        %TAG !u! tag:unity3d.com,2011:
        --- !u!129 &1
        PlayerSettings:
            wiiUDrcBufferDisabled: 0
            wiiUProfilerLibPath: 
            playModeTestRunnerEnabled: 0
            actionOnDotNetUnhandledException: 1
    
        """.stripIndent()

        when:
        def result = ProjectSettings.stripUnityInstructions(content)

        then:
        result.readLines().every { !it.matches(/%TAG !u! tag:unity3d.com,.*:/) }
        result.readLines().every { !it.matches(/(--- )!u!\d+( &\d+)/) }
    }
}
