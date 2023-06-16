package wooga.gradle.unity.tasks

import com.wooga.spock.extensions.unity.UnityPathResolution
import com.wooga.spock.extensions.unity.UnityPluginTestOptions
import com.wooga.spock.extensions.uvm.UnityInstallation
import net.wooga.uvm.Installation
import org.apache.commons.io.FileUtils
import org.gradle.api.file.Directory
import spock.lang.Requires
import wooga.gradle.unity.UnityTaskIntegrationSpec

import static org.apache.commons.io.FileUtils.getFile

class CreateProjectTaskIntegrationSpec extends UnityTaskIntegrationSpec<CreateProject> {

    @UnityInstallation(version = "2019.4.38f1", cleanup = false)
    Installation unity

    @Requires({  os.macOs })
    @UnityPluginTestOptions(unityPath = UnityPathResolution.Default)
    def "creates unity project"() {

        given: "a pre installed unity editor"
        environmentVariables.set("UNITY_PATH", unity.getExecutable().getPath())

        and: "configuration of the extension"
        def projectPath = "Wooga.Foobar"
        buildFile << """
        unity {
            projectDirectory.set(${wrapValueBasedOnType(projectPath, Directory)})
        }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully(subjectUnderTestName)

        then:
        result.success
        getFile(projectDir, projectPath, "Assets").exists()
        getFile(projectDir, projectPath, "ProjectSettings").exists()
    }
}
