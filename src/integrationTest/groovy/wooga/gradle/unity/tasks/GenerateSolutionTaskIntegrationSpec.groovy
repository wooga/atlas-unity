package wooga.gradle.unity.tasks

import com.wooga.spock.extensions.unity.UnityPathResolution
import com.wooga.spock.extensions.unity.UnityPluginTestOptions
import com.wooga.spock.extensions.uvm.UnityInstallation
import net.wooga.uvm.Installation
import spock.lang.Requires
import wooga.gradle.unity.UnityTaskIntegrationSpec

class GenerateSolutionTaskIntegrationSpec extends UnityTaskIntegrationSpec<GenerateSolution> {

    @UnityPluginTestOptions(addMockTask = false, disableAutoActivateAndLicense = false)
    def "calls unity SyncSolution method from command line"() {
        given: "applied atlas-unity plugin"
        when: "generateSolution task is called"
        def res = runTasksSuccessfully("generateSolution")
        then: "unity sync solution entrypoint is called in batchmode"
        res.standardOutput.contains("-batchmode")
        res.standardOutput.contains("-quit")
        res.standardOutput.contains("-executeMethod UnityEditor.SyncVS.SyncSolution")
    }


    @UnityPluginTestOptions(addMockTask = false, disableAutoActivateAndLicense = false)
    def "generateSolution task is never up-to-date"() {
        given: "applied atlas-unity plugin"

        when: "generateSolution task is called"
        def firstRes = runTasksSuccessfully("generateSolution")
        and: "generateSolution task is called again"
        def secondRes = runTasksSuccessfully("generateSolution")

        then: "neither of ran tasks were up-to-date"
        !firstRes.wasUpToDate(":generateSolution")
        !secondRes.wasUpToDate(":generateSolution")
    }

    @UnityInstallation(version = "2019.4.24f1", cleanup = false)
    Installation unity

    @Requires({ os.macOs })
    @UnityPluginTestOptions(unityPath = UnityPathResolution.Default)

    def "generates .sln file when running generateSolution task"() {
        given: "an unity3D project"
        def project_path = "build/test_project"
        environmentVariables.set("UNITY_PATH", unity.getExecutable().getPath())
        appendToSubjectTask("""createProject = "${project_path}" """,
                                  """buildTarget = "Android" """)

        when:"generateSolution task is called"
        def result = runTasksSuccessfully(subjectUnderTestName)

        then:"solution file is generated"
        result.standardOutput.contains("Starting process 'command '${unity.getExecutable().getPath()}'")
        fileExists(project_path)
        fileExists(project_path, "test_project.sln")
    }
}
