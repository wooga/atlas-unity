package wooga.gradle.unity.utils

import spock.lang.Specification
import spock.lang.Shared
import spock.lang.Unroll

class ProjectSettingsSpec extends Specification {

    static String TEMPLATE_CONTENT = """      

PlayerSettings:
    wiiUDrcBufferDisabled: 0
    wiiUProfilerLibPath: 
    playModeTestRunnerEnabled: 0
    actionOnDotNetUnhandledException: 1
    
    """

    static String TEMPLATE_CONTENT_ENABLED = """      

PlayerSettings:
    wiiUDrcBufferDisabled: 0
    wiiUProfilerLibPath: 
    playModeTestRunnerEnabled: 1
    actionOnDotNetUnhandledException: 1
    
    """

    @Shared
    File templateFile = File.createTempFile("ProjectSettings", ".asset")

    @Unroll
    def "initialize with #objectType"() {
        expect:
        new ProjectSettings(content)

        where:
        objectType | content
        "String"   | TEMPLATE_CONTENT
        "File"     | templateFile << TEMPLATE_CONTENT
    }

    @Unroll
    def "parse playModeTestRunnerDisabled with #objectType"() {
        when:
        def settings =new ProjectSettings(content)

        then:
        !settings.playModeTestRunnerEnabled

        where:
        objectType | content
        "String"   | TEMPLATE_CONTENT
        "File"     | templateFile << TEMPLATE_CONTENT
    }

    @Unroll
    def "parse playModeTestRunnerEnabled with #objectType"() {
        when:
        def settings =new ProjectSettings(content)

        then:
        settings.playModeTestRunnerEnabled

        where:
        objectType | content
        "String"   | TEMPLATE_CONTENT_ENABLED
        "File"     | templateFile << TEMPLATE_CONTENT_ENABLED
    }

}
