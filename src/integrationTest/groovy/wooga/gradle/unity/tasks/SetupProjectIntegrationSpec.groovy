package wooga.gradle.unity.tasks

import com.wooga.spock.extensions.unity.UnityPathResolution
import com.wooga.spock.extensions.unity.UnityPluginTestOptions
import com.wooga.spock.extensions.uvm.UnityInstallation
import net.wooga.uvm.Installation
import org.gradle.api.file.Directory
import spock.lang.Requires
import wooga.gradle.unity.UnityTaskIntegrationSpec

import static org.apache.commons.io.FileUtils.getFile

class SetupProjectIntegrationSpec extends UnityTaskIntegrationSpec<SetupProject> {

    @Requires({ os.macOs })
    @UnityPluginTestOptions(unityPath = UnityPathResolution.Default)
    @UnityInstallation(version = "2020.3.19f1", cleanup = false)
    def "resolves packages"(Installation unity) {

        given: "a pre installed unity editor"
        environmentVariables.set("UNITY_PATH", unity.getExecutable().getPath())

        and: "configuration of the new project"
        def projectPath = "Wooga.Foobar"
        def createProjectTaskName = "createProject"

        buildFile << """
        unity {
        projectDirectory.set(${wrapValueBasedOnType(projectPath, Directory)})
        }        
        """.stripIndent()
        addTask(createProjectTaskName, CreateProject, true)

        when: "creating the project"
        def createProject = runTasksSuccessfully(createProjectTaskName)

        then:
        createProject.success
        getFile(projectDir, projectPath, "Assets").exists()

        def packageDirectory = getFile(projectDir, projectPath, "Packages")
        def packageLockFile = new File(packageDirectory, SetupProject.lockFileName)
        packageLockFile.delete()
        !packageLockFile.exists()

        when: "resolving the project packages when there's no lock file"
        def setupProject = runTasksSuccessfully("setupProject")

        then:
        setupProject.success
        packageLockFile.exists()
    }
}
