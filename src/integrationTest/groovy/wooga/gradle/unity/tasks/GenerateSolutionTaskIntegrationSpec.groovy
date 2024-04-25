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

    @Requires({ os.macOs })
    @UnityPluginTestOptions(unityPath = UnityPathResolution.Default)
    @UnityInstallation(version = "2019.4.24f1", cleanup = false)
    def "generates .sln file when running generateSolution task for unity 2019.4"(Installation unity) {
        given: "an unity3D project"
        buildFile << """
            unity {
                unityPath = ${wrapValueBasedOnType(unity.executable, File)}
                projectDirectory = file("${projectDir.absolutePath}")
            }
            tasks.withType(wooga.gradle.unity.UnityTask).configureEach {
                createProject = "${projectDir.absolutePath}"
                buildTarget = "Android" 
            }
        """
        assert !projectDir.list().any{ it.endsWith(".sln") }


        when:"generateSolution task is called"
        def result = runTasks(subjectUnderTestName)

        then:"solution file is generated"
        projectDir.list().any{ it.endsWith(".sln") }
        result.standardOutput.contains("Starting process 'command '${unity.getExecutable().getPath()}'")
        result.standardOutput.contains("-executeMethod")
        result.standardOutput.contains("UnityEditor.SyncVS.SyncSolution")
    }


    @Requires({ os.macOs })
    @UnityPluginTestOptions(unityPath = UnityPathResolution.Default)
    @UnityInstallation(version = "2022.3.18f1", cleanup = false)
    def "generates .sln file when running generateSolution task for unity 2022.3"(Installation unity) {
        given: "an unity3D project"
        buildFile << """
            unity {
                unityPath = ${wrapValueBasedOnType(unity.executable, File)}
                projectDirectory = file("${projectDir.absolutePath}")
            }
            tasks.withType(wooga.gradle.unity.UnityTask).configureEach {
                createProject = "${projectDir.absolutePath}"
                buildTarget = "Android" 
            }
        """
        assert !projectDir.list().any{ it.endsWith(".sln") }

        when:"generateSolution task is called"
        def result = runTasks(subjectUnderTestName)

        then:"solution file is generated"
        projectDir.list().any{ it.endsWith(".sln") }
        result.standardOutput.contains("Starting process 'command '${unity.getExecutable().getPath()}'")
        result.standardOutput.contains("-executeMethod")
        result.standardOutput.contains("Packages.Rider.Editor.RiderScriptEditor.SyncSolution")
    }
}
