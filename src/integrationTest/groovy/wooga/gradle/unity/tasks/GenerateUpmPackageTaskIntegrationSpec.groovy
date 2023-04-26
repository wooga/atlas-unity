package wooga.gradle.unity.tasks


import com.wooga.gradle.test.writers.PropertyGetterTaskWriter
import com.wooga.gradle.test.writers.PropertySetterWriter
import com.wooga.spock.extensions.unity.UnityPathResolution
import com.wooga.spock.extensions.unity.UnityPluginTestOptions
import com.wooga.spock.extensions.uvm.UnityInstallation
import groovy.json.JsonSlurper
import net.wooga.uvm.Installation
import org.gradle.api.file.Directory
import spock.lang.Requires
import spock.lang.Unroll
import wooga.gradle.unity.UnityIntegrationSpec
import wooga.gradle.unity.testutils.SetupProjectLayoutTestTask
import wooga.gradle.unity.utils.PackageManifestBuilder
import wooga.gradle.utils.DirectoryComparer

class GenerateUpmPackageTaskIntegrationSpec extends UnityIntegrationSpec {

    @Override
    String getSubjectUnderTestName() {
        "generateUpmPackage"
    }

    String subjectUnderTestTypeName = GenerateUpmPackage.class.name

    @Unroll
    def "can set property #propertyName with #type"() {
        expect:
        runPropertyQuery(getter, setter).matches(value)

        where:
        propertyName          | type                  | value
        "packageDirectory"    | "File"                | osPath("/path/to/package/dir")
        "packageDirectory"    | "Provider<Directory>" | osPath("/path/to/package/dir")
        "packageName"         | "String"              | "testPackageA"
        "packageName"         | "Provider<String>"    | "testPackageB"

        setter = new PropertySetterWriter(subjectUnderTestName, propertyName)
                .set(value, type)
        getter = new PropertyGetterTaskWriter(setter)
    }

    @Requires({ os.macOs })
    @UnityPluginTestOptions(unityPath = UnityPathResolution.Default)
    @UnityInstallation(version = "2022.1.15f1", cleanup = false)
    def "generates unity package"(
            String packageDisplayName,
            String unityProjectPath,
            String distributionsDirName,
            String packageDirectory,
            String packageName,
            String packageVersion,
            String expectedPackageFileName,
            Installation unity
    ) {

        given: "a pre installed unity editor"
        environmentVariables.set("UNITY_PATH", unity.getExecutable().getPath())

        and: "future directory expectations"
        def distributionsDir = new File(projectDir, distributionsDirName)
        assert !distributionsDir.exists()

        and: "future package file"
        def packageFile = new File(distributionsDir, expectedPackageFileName)
        assert !packageFile.exists()

        and: "configuration of the extension"
        buildFile << """\
            unity {
                projectDirectory.set(${wrapValueBasedOnType(unityProjectPath, Directory)})
            }
        """.stripIndent()

        def setupProjectTask = setupUpmTestProject(unityProjectPath, packageDisplayName, packageName)

        and: "configure task to generate the upm package"
        appendToSubjectTask("""\
            packageDirectory.set(${wrapValueBasedOnType(packageDirectory, Directory)})
            packageName = ${wrapValueBasedOnType(packageName, String)}
            archiveVersion.set(${wrapValueBasedOnType(packageVersion, String)})
            dependsOn ${setupProjectTask}
        """.stripIndent())

        when:
        def result = runTasksSuccessfully(subjectUnderTestName)

        then:
        result.success
        distributionsDir.exists()
        packageFile.exists()

        def packageManifestUnpackDir = unpackPackage(packageFile)
        def unpackedPackageDir = new File(packageManifestUnpackDir, "package")
        unpackedPackageDir.exists()

        // Check the contents of the package manifest
        def packageManifestFile = new File(unpackedPackageDir, GenerateUpmPackage.packageManifestFileName)
        packageManifestFile.exists()

        def json = new JsonSlurper().parse(packageManifestFile)
        json["name"] == packageName
        json["version"] == packageVersion

        // Compare the contents of both unpacked and package source directories
        // NOTE: We don't compare the manifests since we are patching it during the packaging
        def packageSourceDir = new File(projectDir, packageDirectory)
        def comparer = new DirectoryComparer(packageSourceDir, unpackedPackageDir)
        comparer.ignoreFile(GenerateUpmPackage.packageManifestFileName)
        comparer.ignoreTimestamps()
        def comparison = comparer.compare()
        assert comparison.valid: comparison.toString()

        where:
        packageDisplayName = "Foobar"
        unityProjectPath = "Wooga.${packageDisplayName}"
        distributionsDirName = "build/distributions"
        packageDirectory = unityProjectPath + "/Assets/Wooga/Foobar"
        packageName = "com.wooga.foobar"
        packageVersion = "0.0.1"
        expectedPackageFileName = "${packageName}-${packageVersion}.tgz"

        and: "the injected unity installation"
        unity = null
    }

    @Requires({ os.macOs })
    @UnityPluginTestOptions(unityPath = UnityPathResolution.Default)
    @UnityInstallation(version = "2019.4.38f1", cleanup = false)
    def "uses package name and version from package.json when not specified"(
            String packageDisplayName,
            String unityProjectPath,
            String distributionsDirName,
            String packageDirectory,
            String packageName,
            String packageVersion,
            String expectedPackageFileName,
            Installation unity
    ) {

        given: "a pre installed unity editor"
        environmentVariables.set("UNITY_PATH", unity.getExecutable().getPath())

        and: "future directory expectations"
        def distributionsDir = new File(projectDir, distributionsDirName)
        assert !distributionsDir.exists()

        and: "future package file"
        def packageFile = new File(distributionsDir, expectedPackageFileName)
        assert !packageFile.exists()

        and: "configuration of the extension"
        buildFile << """\
            unity {
                projectDirectory.set(${wrapValueBasedOnType(unityProjectPath, Directory)})
            }
        """.stripIndent()

        def setupProjectTask = setupUpmTestProject(unityProjectPath, packageDisplayName, packageName, packageVersion)

        and: "configure task to generate the upm package"
        appendToSubjectTask("""\
            packageDirectory.set(${wrapValueBasedOnType(packageDirectory, Directory)})
            dependsOn ${setupProjectTask}
        """.stripIndent())

        when:
        def result = runTasksSuccessfully(subjectUnderTestName)

        then:
        result.success
        distributionsDir.exists()
        packageFile.exists()

        def packageManifestUnpackDir = unpackPackage(packageFile)
        def unpackedPackageDir = new File(packageManifestUnpackDir, "package")
        unpackedPackageDir.exists()

        // Check the contents of the package manifest
        def packageManifestFile = new File(unpackedPackageDir, GenerateUpmPackage.packageManifestFileName)
        packageManifestFile.exists()

        def json = new JsonSlurper().parse(packageManifestFile)
        json["name"] == packageName
        json["version"] == packageVersion

        // Compare the contents of both unpacked and package source directories
        // NOTE: We don't compare the manifests since we are patching it during the packaging
        def packageSourceDir = new File(projectDir, packageDirectory)
        def comparer = new DirectoryComparer(packageSourceDir, unpackedPackageDir)
        comparer.ignoreFile(GenerateUpmPackage.packageManifestFileName)
        comparer.ignoreTimestamps()
        def comparison = comparer.compare()
        assert comparison.valid: comparison.toString()

        where:
        packageDisplayName = "Foobar"
        unityProjectPath = "Wooga.${packageDisplayName}"
        distributionsDirName = "build/distributions"
        packageDirectory = unityProjectPath + "/Assets/Wooga/Foobar"
        packageName = "com.wooga.foobar-baz"
        packageVersion = "1.1.1"
        expectedPackageFileName = "${packageName}-${packageVersion}.tgz"

        and: "the injected unity installation"
        unity = null
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
        projectFile(projectPath, "README.MD")
        def manifestFile = projectFile(projectPath, GenerateUpmPackage.packageManifestFileName)
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
        "com.wooga.foobar" | "0.0.1"        | GenerateUpmPackage.Message.packageNameNotSet
        reason = predicate.message
    }

    private static File unpackPackage(File packageFile) {
        def packageManifestUnpackDir = File.createTempDir(packageFile.name, "_unpack")
        def ant = new AntBuilder()
        ant.untar(src: packageFile.path, dest: packageManifestUnpackDir.path, compression: "gzip")
        packageManifestUnpackDir
    }

    private String setupUpmTestProject(String unityProjectPath, String packageDisplayName, String packageName, String packageVersion = "0.0.0") {
        def createProjectTaskName = "createProject"
        def createProjectTask = addTask(createProjectTaskName, CreateProject.class.name, false, "")

        def addFilesTaskName = "addPackageFiles"
        def addFilesTask = addTask(addFilesTaskName, SetupProjectLayoutTestTask.class.name, false, """\
            unityProjectDirectoryName = ${wrapValueBasedOnType(unityProjectPath, "String")}
            packageDisplayName = ${wrapValueBasedOnType(packageDisplayName, "String")}
            packageName = ${wrapValueBasedOnType(packageName, "String")}
            initialPackageVersion = ${wrapValueBasedOnType(packageVersion, "String")}
            dependsOn ${createProjectTask}
        """.stripIndent())

        def generateMetaFilesTaskName = "generateMetafiles"
        def generateMetaFilesTask = addTask(generateMetaFilesTaskName, Unity.class.name, false, """\
            dependsOn ${addFilesTask}
        """.stripIndent())

        def setupProjectTask = addTask("setupUnityProject", "org.gradle.api.DefaultTask", false, """\
            dependsOn ${createProjectTask}, ${addFilesTask}, ${generateMetaFilesTask}
        """)
        setupProjectTask
    }
}
