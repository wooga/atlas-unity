package wooga.gradle.unity.tasks

import com.wooga.spock.extensions.unity.UnityPathResolution
import com.wooga.spock.extensions.unity.UnityPluginTestOptions
import com.wooga.spock.extensions.uvm.UnityInstallation
import net.wooga.uvm.Installation
import org.gradle.api.file.Directory
import org.gradle.api.tasks.Copy
import spock.lang.Requires
import spock.lang.Unroll
import wooga.gradle.unity.UnityIntegrationSpec
import wooga.gradle.unity.testutils.SetupProjectLayoutTestTask
import wooga.gradle.unity.utils.PackageManifestBuilder
import wooga.gradle.utils.DirectoryComparer

class GenerateUpmPackageTaskIntegrationSpec extends UnityIntegrationSpec {

    @Requires({  os.macOs })
    @UnityPluginTestOptions(unityPath = UnityPathResolution.Default)
    @UnityInstallation(version = "2019.4.38f1", cleanup = false)
    def "generates unity package"(Installation unity) {

        given: "a pre installed unity editor"
        environmentVariables.set("UNITY_PATH", unity.getExecutable().getPath())

        and: "future directory expectations"
        def distributionsDirName = "build/distributions"
        def distributionsDir = new File(projectDir, distributionsDirName)
        assert !distributionsDir.exists()

        and: "configuration of the extension"
        def unityProjectPath = SetupProjectLayoutTestTask.unityProjectDirectoryName
        def packageDirRel = unityProjectPath + "/Assets/Wooga/Foobar"
        def expectedProjectDir = new File(projectDir, unityProjectPath)
        buildFile << """
        unity {
        projectDirectory.set(${wrapValueBasedOnType(unityProjectPath, Directory)})
        }
        """.stripIndent()

        and: "a set of values for the package"
        def packageName = "com.wooga.foobar"
        def packageVersion = "0.0.1"
        def expectedPackageFileName = "${packageName}-${packageVersion}.tgz"

        and: "a task to create the project"
        def createProjectTaskName = "createProject"
        def createProjectTask = addTask("createProject", CreateProject.class.name, false, "")

        and: "a task to add additional files to the project 2"
        def addFilesTaskName = "addPackageFiles"
        def addFilesTask = addTask(addFilesTaskName, SetupProjectLayoutTestTask.class.name, false, """
        dependsOn ${createProjectTask}
        """.stripIndent())

        and: "a task to generate meta files"
        def generateMetaFilesTaskName = "metatron"
        def generateMetaFilesTask = addTask(generateMetaFilesTaskName, Unity.class.name, false, """
        dependsOn ${addFilesTask}
        """.stripIndent())

        and: "a task to generate the upm package"
        def generateUpmPackageTaskName = "upmPack"
        addTask(generateUpmPackageTaskName, GenerateUpmPackage.class.name, false, """
        packageDirectory.set(${wrapValueBasedOnType(packageDirRel, Directory)})
        packageName = ${wrapValueBasedOnType(packageName, String)}
        archiveVersion.set(${wrapValueBasedOnType(packageVersion, String)})
        dependsOn ${generateMetaFilesTask}
        """.stripIndent())

        and: "a task to extract the upm package so we can compare, okay?"
        def extractUpmPackageName = "upmUnpack"
        def packageFileRelPath = "${distributionsDirName}/${expectedPackageFileName}"
        addTask(extractUpmPackageName, Copy.class.name, false, """
            from tarTree(\"${packageFileRelPath}\")
            into layout.buildDirectory.dir(${wrapValueBasedOnType("unpack", String)})
            """.stripIndent())

        when:
        def result = runTasksSuccessfully(createProjectTaskName, generateUpmPackageTaskName, addFilesTaskName, extractUpmPackageName)

        then:
        result.success
        distributionsDir.exists()

        def packageManifestUnpackDir = new File(projectDir, "build/unpack")
        packageManifestUnpackDir.exists()

        def packageFile = new File(distributionsDir, expectedPackageFileName)
        packageFile.exists()

        def unpackedPackageDir = new File(packageManifestUnpackDir, "package")
        unpackedPackageDir.exists()

        // Check the contents of the package manifest
        def packageManifestFile = new File(unpackedPackageDir, GenerateUpmPackage.packageManifestFileName)
        packageManifestFile.exists()

        def packageJson = packageManifestFile.text
        packageJson.contains("\"name\" : \"${packageName}\"")
        packageJson.contains("\"version\" : \"${packageVersion}\"")

        // Compare the contents of both unpacked and package source directories
        // NOTE: We don't compare the manifests since we are patching it during the packaging
        def packageSourceDir = new File(expectedProjectDir, "Assets/Wooga/Foobar")
        def comparer = new DirectoryComparer(packageSourceDir, unpackedPackageDir)
        comparer.ignoreFile(GenerateUpmPackage.packageManifestFileName)
        comparer.ignoreTimestamps()
        def comparison = comparer.compare()
        assert comparison.valid: comparison.toString()
    }

    @Unroll
    def "fails to generate package if '#reason'"() {

        given: "configuration of the extension"
        def projectPath = "Wooga.Foobar"
        directory(projectPath)
        buildFile << """
        unity {
        projectDirectory.set(${wrapValueBasedOnType(projectPath, Directory)})
        }
        """.stripIndent()

        // Add package files
        file(projectPath, "README.MD")
        def manifestFile = file(projectPath, GenerateUpmPackage.packageManifestFileName)
        manifestFile.write(new PackageManifestBuilder().build())

        and:
        def generateUpmPackageTaskName = "upmPack"
        List<String> taskStatements = new ArrayList<String>()
        if (predicate != GenerateUpmPackage.Message.packageDirectoryNotSet) {
            taskStatements.add("packageDirectory.set(${wrapValueBasedOnType(projectPath, Directory)})")
        }
        if (predicate != GenerateUpmPackage.Message.versionNotSet) {
            taskStatements.add("archiveVersion.set(${wrapValueBasedOnType(packageVersion, String)})")
        }
        if (predicate != GenerateUpmPackage.Message.packageNameNotSet) {
            taskStatements.add("packageName = ${wrapValueBasedOnType(packageName, String)}")
        }

        addTask(generateUpmPackageTaskName, GenerateUpmPackage.class.name, false, taskStatements.join("\n"))

        when:
        def result = runTasks(generateUpmPackageTaskName)

        then:
        // TODO: Utility method to check either skip reason (NO-SOURCES, etc)
        result.wasSkipped(generateUpmPackageTaskName) || outputContains(result, "${generateUpmPackageTaskName} NO-SOURCE")
        outputContains(result, reason)

        where:
        packageName        | packageVersion | predicate
        "com.wooga.foobar" | "0.0.1"        | GenerateUpmPackage.Message.packageDirectoryNotSet
        "com.wooga.foobar" | "0.0.1"        | GenerateUpmPackage.Message.versionNotSet
        "com.wooga.foobar" | "0.0.1"        | GenerateUpmPackage.Message.packageNameNotSet
        reason = predicate.message
    }


}
