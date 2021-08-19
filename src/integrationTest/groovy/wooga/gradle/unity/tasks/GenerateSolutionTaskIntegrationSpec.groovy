package wooga.gradle.unity.tasks

import com.wooga.spock.extensions.unity.UnityPluginTestOptions
import wooga.gradle.unity.UnityTaskIntegrationSpec

class GenerateSolutionTaskIntegrationSpec extends UnityTaskIntegrationSpec<Unity> {

    @UnityPluginTestOptions(addMockTask = false, disableAutoActivateAndLicense = false)
    def "calls unity generateSolution method from command line"() {
        given: "applied atlas-unity plugin"
        when: "generateSolution task is called"
        def res = runTasksSuccessfully("generateSolution")
        then: "unity sync solution entrypoint is called in batchmode"
        res.standardOutput.contains("-batchmode")
        res.standardOutput.contains("-quit")
        res.standardOutput.contains("-executeMethod UnityEditor.SyncVS.SyncSolution")
    }
}
