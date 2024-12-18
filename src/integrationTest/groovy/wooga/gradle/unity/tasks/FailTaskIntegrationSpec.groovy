package wooga.gradle.unity.tasks


import com.wooga.spock.extensions.unity.UnityPluginTestOptions
import groovy.json.StringEscapeUtils
import wooga.gradle.unity.UnityTaskIntegrationSpec

import java.util.regex.Matcher
import java.util.regex.Pattern

class FailingTask extends Unity {

    static Pattern pattern = Pattern.compile("<BUILD ERRORS>(?<summary>(.*\\s*)*)</BUILD ERRORS>")

    // Escape for ^<, ^>
    public static String expectedErrorMessage = """[BuildEngine] Collected errors during build process:
<BUILD ERRORS>
[Error] FOO Wooga.UnifiedBuildSystem.Tests.Editor.BuildEngineTest+FailingBuildSteps:FailInElaborateWays (at Packages/com.wooga.unified-build-system/Tests/Editor/BuildEngine/BuildRequestOutputTest.cs:32)
[Error] BAR Wooga.UnifiedBuildSystem.Tests.Editor.BuildEngineTest+FailingBuildSteps:FailInElaborateWays (at Packages/com.wooga.unified-build-system/Tests/Editor/BuildEngine/BuildRequestOutputTest.cs:32)
[Exception] Exception: Boom! Wooga.UnifiedBuildSystem.Editor.BuildTask`1[[Wooga.UnifiedBuildSystem.Editor.BuildTaskArguments, Wooga.Build.Editor, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null]]:Execute (at Packages/com.wooga.build/Editor/Tasks/BuildTask.cs:73)
[Error] Failed to execute task 'FailInElaborateWays' Wooga.UnifiedBuildSystem.Editor.BuildEngine+State:SetBuildFailure (at Packages/com.wooga.unified-build-system/Editor/BuildEngine/BuildEngine.cs:89)
</BUILD ERRORS>""".stripIndent()

    static String buildErrorStartMarker = "<BUILD ERRORS>"
    static String buildErrorEndMarker = "</BUILD ERRORS>"

    @Override
    protected String composeExceptionMessage(String stdout, String stderr) {
        Matcher matcher = pattern.matcher(stderr)
        if (matcher.find()){
            var summary = matcher.group(0)
            return summary
        }
        return ""
    }
}

class FailTaskIntegrationSpec extends UnityTaskIntegrationSpec<FailingTask>{

    static String echoError(String message) {
        """echo "${StringEscapeUtils.escapeJava(message).replace("`", "\\`") }" 1>&2 """
    }

    @UnityPluginTestOptions(writeMockExecutable = false)
    def "throws composited exception message"() {

        given:
        writeMockExecutable({
            it.text += "\n${FailingTask.expectedErrorMessage.readLines().collect{echoError(it) }.join("\n")}"
            it.printEnvironment = false 
            it.exitValue = 666
        })

        when:
        def result = runTasksWithFailure(subjectUnderTestName)

        then:
        result.wasExecuted(subjectUnderTestName)
        result.standardError.contains(FailingTask.buildErrorStartMarker)
        result.standardError.contains(FailingTask.buildErrorEndMarker)
    }
}
