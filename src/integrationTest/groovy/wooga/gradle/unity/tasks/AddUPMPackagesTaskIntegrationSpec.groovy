package wooga.gradle.unity.tasks

import com.wooga.spock.extensions.unity.UnityPathResolution
import com.wooga.spock.extensions.unity.UnityPluginTestOptions
import com.wooga.spock.extensions.uvm.UnityInstallation
import net.wooga.uvm.Installation
import spock.lang.Requires
import wooga.gradle.unity.UnityTaskIntegrationSpec

class AddUPMPackagesTaskIntegrationSpec extends UnityTaskIntegrationSpec<AddUPMPackages> {

    @Requires({ os.macOs })
    @UnityPluginTestOptions(unityPath = UnityPathResolution.Default)
    @UnityInstallation(version = "2019.4.24f1", cleanup = false, basePath = "/Applications/Unity/Hub/Editor")
    def "adds Packages"(Installation unity) {
        given: "an unity3D project"
        def project_path = "build/test_project"
        environmentVariables.set("UNITY_PATH", unity.getExecutable().getPath())
        appendToSubjectTask("""
                createProject = "${project_path}"
                buildTarget = "Android"
                manifestPath.set("${project_path}/Packages/manifest.json")
                upmPackages.put("com.unity.testtools.codecoverage", "1.1.0")
            """.stripIndent())

        when:"add UPM packages"
        def result = runTasksSuccessfully(subjectUnderTestName)

        then:"solution file is generated"
        result.standardOutput.contains("Starting process 'command '${unity.getExecutable().getPath()}'")
        fileExists(project_path)
        fileExists(project_path, "Packages/manifest.json")
    }
}
