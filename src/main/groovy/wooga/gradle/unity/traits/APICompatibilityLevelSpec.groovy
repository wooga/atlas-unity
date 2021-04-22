package wooga.gradle.unity.traits


import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import wooga.gradle.unity.models.APICompatibilityLevel

trait APICompatibilityLevelSpec extends UnityBaseSpec {

    MapProperty<String, APICompatibilityLevel> apiCompatibilityLevel = objects.mapProperty(String, APICompatibilityLevel)

    @Input
    @Optional
    MapProperty<String, APICompatibilityLevel> getApiCompatibilityLevel() {
        apiCompatibilityLevel
    }

    void setApiCompatibilityLevel(Provider<?> value) {
        apiCompatibilityLevel.set(value.map({
            Map<String, APICompatibilityLevel> map = null
            if (it instanceof APICompatibilityLevel){
                map = APICompatibilityLevel.toMap(it)
            }
            else if (it instanceof String){
                map = APICompatibilityLevel.toMap(APICompatibilityLevel.parse(it))
            }
            else if (it instanceof Map<String, APICompatibilityLevel>){
                map = it
            }
            map
        }))
    }

    void setApiCompatibilityLevel(Map<String, APICompatibilityLevel> value) {
        apiCompatibilityLevel.set(value)
    }

    void setApiCompatibilityLevel(APICompatibilityLevel value) {
        apiCompatibilityLevel.set(APICompatibilityLevel.toMap(value))
    }

    void setApiCompatibilityLevel(String value) {
        setApiCompatibilityLevel(APICompatibilityLevel.parse(value))
    }

    private RegularFileProperty settingsFile = objects.fileProperty()
    @InputFile
    RegularFileProperty getSettingsFile() {
        settingsFile
    }
    void setSettingsFile(Provider<RegularFile> value) {
        this.settingsFile.set(value)
    }
}
