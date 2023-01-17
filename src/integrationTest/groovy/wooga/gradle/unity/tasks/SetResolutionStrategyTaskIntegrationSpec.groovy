package wooga.gradle.unity.tasks


import com.wooga.gradle.test.writers.PropertyGetterTaskWriter
import com.wooga.gradle.test.writers.PropertySetterWriter
import com.wooga.spock.extensions.unity.UnityPluginTestOptions
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import spock.lang.Unroll
import wooga.gradle.unity.models.ResolutionStrategy

class SetResolutionStrategyTaskIntegrationSpec extends ProjectManifestTaskSpec<SetResolutionStrategy> {

    @Unroll
    def "can set property #propertyName with #type"() {
        expect:
        runPropertyQuery(getter, setter).matches(value)

        where:
        propertyName         | type   | value
        "resolutionStrategy" | String | ResolutionStrategy.lowest
        "resolutionStrategy" | String | ResolutionStrategy.highest

        setter = new PropertySetterWriter(subjectUnderTestName, propertyName)
            .set(value, type)
        getter = new PropertyGetterTaskWriter(setter)
    }

    @UnityPluginTestOptions(forceMockTaskRun = false)
    def "skips when the resolution strategy is not set"() {

        when:
        def result = runTasks(subjectUnderTestName)

        then:
        result.wasSkipped(subjectUnderTestName)
    }

    @UnityPluginTestOptions(forceMockTaskRun = false)
    @Unroll
    def "#verb the resolution strategy #strategy"() {

        given: "an unity project with the manifest file set"
        def manifestPath = "build/test_project/Packages/manifest.json"
        def manifestFile = new File(projectDir, manifestPath)
        manifestFile.parentFile.mkdirs()
        manifestFile.createNewFile()

        and: "an existing manifest file"
        def packages = ["com.unity.ugui": "1.0.0"]

        Map<String, Object> manifestContents = [
            "dependencies": packages
        ]
        if (originalStrategy != _) {
            manifestContents["resolutionStrategy"] = originalStrategy
        }
        manifestFile << JsonOutput.toJson(manifestContents)

        and: "task configuration"
        appendToSubjectTask("""
                resolutionStrategy = ${wrapValueBasedOnType(strategy, String)}
                projectManifestFile = ${wrapValueBasedOnType(manifestPath, File)}
            """.stripIndent()
        )

        when:
        runTasksSuccessfully(subjectUnderTestName)

        then: "manifest file contains resolution strategy"
        def actual = new JsonSlurper().parse(manifestFile)["resolutionStrategy"]
        actual == strategy

        where:
        originalStrategy | strategy
        "katzen"         | "highestMinor"
        _                | "highestMinor"
        _                | "lowest"
        "highest"        | "highestPatch"

        verb = originalStrategy != _ ? "overrides" : "sets"
    }
}
