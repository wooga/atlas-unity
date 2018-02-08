package wooga.gradle.unity.utils

import spock.lang.Specification
import spock.lang.Unroll
import wooga.gradle.unity.utils.internal.ProjectSettings

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

    static String TEMPLATE_CONTENT_INVALID = """      
    0000 be6d 0000 c9e7 0000 0011 0000 be90
    0000 0000 3230 3137 2e31 2e30 6633 00fe
    ffff ff01 0100 0000 8100 0000 00ff ffa0
    fc36 4045 ea85 96ed 5478 598b 4e1b 87d8
    0500 00d9 3100 000c 0000 0000 0000 0037
    0000 80ff ffff ff00 0000 0000 8000 0001
    
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

    def "parse invalid project settings file"() {
        when: "initialize projectSettings with binary content"
        def settings = new ProjectSettings(TEMPLATE_CONTENT_INVALID)

        then:
        !settings.playModeTestRunnerEnabled
    }
}
