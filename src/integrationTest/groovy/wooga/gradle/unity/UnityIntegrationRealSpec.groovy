package wooga.gradle.unity

import nebula.test.IntegrationSpec
import org.apache.commons.lang.StringEscapeUtils
import spock.lang.Ignore

@Ignore
class UnityIntegrationRealSpec extends IntegrationSpec {

    def escapedPath(String path) {
        String osName = System.getProperty("os.name").toLowerCase()
        if (osName.contains("windows")) {
            return StringEscapeUtils.escapeJava(path)
        }
        path
    }

    def "runs batchmode action"() {
        given: "path to future project"
        def project_path = new File( projectDir,"build/test")

        and: "a build script"
        buildFile << """
            group = 'test'
            ${applyPlugin(UnityPlugin)}
         
            task mUnity {
                doLast {
                    unity.batchMode {
                        args "-createProject", "test"
                    }
                }
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("mUnity")

        then:
        result.standardOutput.contains("Unity.exe")
        fileExists("build/test")
    }

    def "runs batchmode task"() {
        given: "path to future project"
        def project_path = new File( projectDir,"build/test")

        and: "a build script"
        buildFile << """
            group = 'test'
            ${applyPlugin(UnityPlugin)}
         
            task (mUnity, type: wooga.gradle.unity.tasks.Unity) {
                args "-createProject", "${escapedPath(project_path.path)}"
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("mUnity")

        then:
        result.standardOutput.contains("Unity.exe")
        fileExists("build/test")
    }
}
