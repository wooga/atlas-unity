package wooga.gradle.unity.tasks

import com.wooga.spock.extensions.unity.UnityPathResolution
import com.wooga.spock.extensions.unity.UnityPluginTestOptions
import com.wooga.spock.extensions.uvm.UnityInstallation
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import net.wooga.uvm.Installation
import spock.lang.Requires
import wooga.gradle.unity.UnityTaskIntegrationSpec

class AddUPMPackagesTaskIntegrationSpec extends UnityTaskIntegrationSpec<AddUPMPackages> {


    @Requires({ os.macOs })
    @UnityPluginTestOptions(unityPath = UnityPathResolution.Default)
    @UnityInstallation(version = "2019.4.19f1", cleanup = false)
    def "creates unity manifest and adds package to it when running AddUPMTask task"(Installation unity) {
        given: "an unity3D project"
        def projectPath = "build/test_project"
        environmentVariables.set("UNITY_PATH", unity.getExecutable().getPath())


        //Test using the extension
        and: "a setup AddUPMPackageTask"
        appendToSubjectTask("""
                createProject = "${projectPath}"
                manifestPath = new File("${projectPath}/Packages/manifest.json")
                buildTarget = "Android"
                upmPackages.put("com.unity.testtools.codecoverage", "1.1.0") 
                upmPackages.put("com.unity.package", "anyString")
            """.stripIndent()
        )

        when: "add UPM packages"
        runTasksSuccessfully(subjectUnderTestName)

        then: "manifest file is generated"
        def manifestFile = new File(new File(projectDir, projectPath), "Packages/manifest.json")
        manifestFile.exists()
        and: "manifest file contains added packages"
        def dependencies = new JsonSlurper().parse(manifestFile)["dependencies"]
        dependencies["com.unity.testtools.codecoverage"] == "1.1.0"
        dependencies["com.unity.package"] == "anyString"
    }

    def "adds package to existing unity manifest file when running AddUPMTask task"() {
        given: "an unity3D project"
        def manifestPath = "build/test_project/Packages/custom/manifest.json"
        def manifestFile = new File(projectDir, manifestPath)
        manifestFile.parentFile.mkdirs()
        manifestFile.createNewFile()

        and: "an existing manifest file"
        def packages = ["com.unity.testtools.codecoverage": "1.1.0"]
        manifestFile << JsonOutput.toJson(["dependencies": packages])

        and: "a setup AddUPMPackageTask"
        appendToSubjectTask("""
                buildTarget = "Android"
                manifestPath = new File("build/test_project/Packages/custom/manifest.json")
                upmPackages.put("com.unity.package", "anyString")
            """.stripIndent()
        )

        when: "add UPM packages"
        runTasksSuccessfully(subjectUnderTestName)

        then: "manifest file contains added packages"
        def dependencies = new JsonSlurper().parse(manifestFile)["dependencies"]
        dependencies["com.unity.testtools.codecoverage"] == "1.1.0"
        dependencies["com.unity.package"] == "anyString"
    }
}

