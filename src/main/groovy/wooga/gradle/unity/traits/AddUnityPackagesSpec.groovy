package wooga.gradle.unity.traits

import com.wooga.gradle.BaseSpec
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import wooga.gradle.unity.models.ResolutionStrategy

trait AddUnityPackagesSpec extends BaseSpec {

    /**
     * @return The UPM packages to be added onto the project manifest
     */
    @Input
    @Optional
    MapProperty<String, String> getUpmPackages() {
        upmPackages
    }

    private final MapProperty<String, String> upmPackages = objects.mapProperty(String, String)

    void setUpmPackages(MapProperty<String, String> values) {
        upmPackages.set(values)
    }

    void setUpmPackages(Map<String, String> values) {
        upmPackages.set(values)
    }
}

