package wooga.gradle.unity.tasks

import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.process.ExecResult
import wooga.gradle.unity.UnityTask
import wooga.gradle.unity.traits.UnityBaseSpec

class AddUPMPackages extends UnityTask implements UnityBaseSpec {

    final RegularFileProperty manifestPath = objects.fileProperty()
    final MapProperty<String, String> upmPackages = objects.mapProperty(String, String)

    AddUPMPackages() {
        description = "Adds UPM packages to Unity project"
    }

    @Override
    protected void postExecute(ExecResult result) {
        if(unityVersion.majorVersion > 2018 ||
            (unityVersion.majorVersion == 2018 && unityVersion.minorVersion > 3)) {
            def manifestFile = manifestPath.asFile.getOrNull()
            if(manifestFile && manifestFile.exists()) {
                def data = new JsonSlurper().parse(manifestFile)
                upmPackages.get().each {
                    data.dependencies[it.key] = it.value
                }
                def json = JsonOutput.prettyPrint(JsonOutput.toJson(data))
                manifestFile.write(json)
            } else {
                project.logger.warn("manifest.json not found, skipping UPM packages install: ${upmPackages.get()}")
            }
        }
    }

    @Internal
    RegularFileProperty getManifestPath() {
        return manifestPath
    }

    void setManifestPath(File manifestPath) {
        this.manifestPath.set(manifestPath)
    }

    void setManifestPath(Provider<RegularFile> manifestPath) {
        this.manifestPath.set(manifestPath)
    }

    @Input
    @Optional
    MapProperty<String, String> getUpmPackages() {
        return upmPackages
    }

    void setUpmPackages(MapProperty<String, String> upmPackages) {
        this.upmPackages.set(upmPackages)
    }

    void setUpmPackages(Map<String, String> upmPackages) {
        this.upmPackages.set(upmPackages)
    }
}
