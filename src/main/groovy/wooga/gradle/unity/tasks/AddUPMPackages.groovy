package wooga.gradle.unity.tasks

import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.gradle.process.ExecResult
import wooga.gradle.unity.UnityTask
import wooga.gradle.unity.traits.UnityBaseSpec

import javax.inject.Inject

class AddUPMPackages extends UnityTask implements UnityBaseSpec {

    public Property<String> manifestPath = objects.property(String)
    public MapProperty<String, String> upmPackages = objects.mapProperty(String, String)

    @Inject
    AddUPMPackages() {
        description = "Adds UPM packages to Unity project"
        quit = true
    }

    @Override
    protected void postExecute(ExecResult result) {
        if(unityVersion.majorVersion > 2018 && unityVersion.minorVersion > 3) {
            def manifestFile =  new File(project.projectDir, manifestPath.get())
            def data = new JsonSlurper().parse(manifestFile)

            upmPackages.get().each {
                data.dependencies[it.key] = it.value
            }
            def json = JsonOutput.prettyPrint(JsonOutput.toJson(data))
            print(json)
            manifestFile.write(json)
        }
    }
}
