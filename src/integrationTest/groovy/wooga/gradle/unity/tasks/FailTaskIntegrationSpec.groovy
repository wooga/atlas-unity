package wooga.gradle.unity.tasks

import com.wooga.gradle.test.mock.MockExecutable
import com.wooga.spock.extensions.unity.UnityPluginTestOptions
import wooga.gradle.unity.UnityTaskIntegrationSpec

class FailingTask extends Unity {

    @Override
    protected String composeExceptionMessage(String stdout, String stderr) {
        return stderr ?: "Build failed because: ${stdout}"
    }
}

class FailTaskIntegrationSpec extends UnityTaskIntegrationSpec<FailingTask>{

    String expectedErrorMessage = """[BuildEngine] Collected errors during build process:
<BUILD ERRORS>
[Error] FOO Wooga.UnifiedBuildSystem.Tests.Editor.BuildEngineTest+FailingBuildSteps:FailInElaborateWays (at Packages/com.wooga.unified-build-system/Tests/Editor/BuildEngine/BuildRequestOutputTest.cs:32)
[Error] BAR Wooga.UnifiedBuildSystem.Tests.Editor.BuildEngineTest+FailingBuildSteps:FailInElaborateWays (at Packages/com.wooga.unified-build-system/Tests/Editor/BuildEngine/BuildRequestOutputTest.cs:32)
[Exception] Exception: Boom! Wooga.UnifiedBuildSystem.Editor.BuildTask`1[[Wooga.UnifiedBuildSystem.Editor.BuildTaskArguments, Wooga.Build.Editor, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null]]:Execute (at Packages/com.wooga.build/Editor/Tasks/BuildTask.cs:73)
[Error] Failed to execute task 'FailInElaborateWays' Wooga.UnifiedBuildSystem.Editor.BuildEngine+State:SetBuildFailure (at Packages/com.wooga.unified-build-system/Editor/BuildEngine/BuildEngine.cs:89)
</BUILD ERRORS>
    """.stripIndent()

    @UnityPluginTestOptions(forceMockTaskRun = true)
    def "throws composited exception message"() {

        given:


        MockExecutable executable = new MockExecutable("fail.bat")
            .withEnvironment(false)
            .withText("BOO!\n${expectedErrorMessage}")
            .withExitValue(666)

        def execFile = executable.toDirectory(projectDir)

        buildFile << """
        unity {
            unityPath.set(${wrapFile(execFile)})            
        }
        """.stripIndent()

        when:
        def result = runTasksWithFailure(subjectUnderTestName)

        then:
        result.wasExecuted(subjectUnderTestName)
        result.standardOutput.contains(expectedErrorMessage)
    }
}
