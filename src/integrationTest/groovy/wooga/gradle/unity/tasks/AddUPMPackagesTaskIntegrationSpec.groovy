package wooga.gradle.unity.tasks


import com.wooga.gradle.test.queries.TestValue
import com.wooga.gradle.test.writers.PropertyGetterTaskWriter
import com.wooga.gradle.test.writers.PropertySetterWriter
import com.wooga.spock.extensions.unity.UnityPathResolution
import com.wooga.spock.extensions.unity.UnityPluginTestOptions
import com.wooga.spock.extensions.uvm.UnityInstallation
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import net.wooga.uvm.Installation
import org.gradle.api.file.Directory
import spock.lang.Requires
import spock.lang.Unroll

class AddUPMPackagesTaskIntegrationSpec extends ProjectManifestTaskSpec<AddUPMPackages> {

    @Unroll
    def "can set property #propertyName with #type"() {
        expect:
        runPropertyQuery(getter, setter).matches(value)

        where:
        propertyName   | type                    | value
        // TODO: This property will be deprecated. Remove when possible.
        "manifestPath" | File                    | TestValue.projectFile("foobar")

        setter = new PropertySetterWriter(subjectUnderTestName, propertyName)
            .set(value, type)
        getter = new PropertyGetterTaskWriter(setter)
    }

    @UnityInstallation(version = "2019.4.19f1", cleanup = false)
    Installation unity

    @Requires({ os.macOs })
    @UnityPluginTestOptions(unityPath = UnityPathResolution.Default)
    def "creates unity manifest and adds package to it when running AddUPMTask task"() {
        given: "an unity3D project"
        def projectPath = "test_project"
        environmentVariables.set("UNITY_PATH", unity.getExecutable().getPath())
        buildFile << """
        unity {
            projectDirectory.set(${wrapValueBasedOnType(projectPath, Directory)})
        }
        """.stripIndent()


        and: "a task to create the project"
        def createProjectTask = "projectMachen"
        addTask(createProjectTask, CreateProject, true, """            
            buildTarget = "Android" 
        """)

        when: "creating the project"
        runTasksSuccessfully(createProjectTask)

        and: "a task to add the packages"
        def addPackagesTask = "langsamerHund"
        writeTask(addPackagesTask, AddUPMPackages, {
            it.withLines("""     
                upmPackages.put("com.unity.testtools.codecoverage", "1.1.0") 
                upmPackages.put("com.unity.package", "anyString")
            """.stripIndent())
        })

        then: "running the tasks"
        runTasksSuccessfully(addPackagesTask)

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

        and: "configuration of the AddUPMPackageTask"
        addTask(taskName, AddUPMPackages, true, """
                manifestPath = new File(${wrapValueBasedOnType(manifestPath, String)})
                upmPackages.put("com.unity.package", "anyString")
            """.stripIndent()
        )

        when: "add UPM packages"
        runTasksSuccessfully(taskName)

        then: "manifest file contains added packages"
        def dependencies = new JsonSlurper().parse(manifestFile)["dependencies"]
        dependencies["com.unity.testtools.codecoverage"] == "1.1.0"
        dependencies["com.unity.package"] == "anyString"

        where:
        taskName = "addPackagesTask"
    }
}


