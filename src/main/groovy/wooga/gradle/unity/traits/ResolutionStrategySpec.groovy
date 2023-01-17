package wooga.gradle.unity.traits

import com.wooga.gradle.BaseSpec
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import wooga.gradle.unity.models.ResolutionStrategy

trait ResolutionStrategySpec extends BaseSpec {

    private final Property<ResolutionStrategy> resolutionStrategy = objects.property(ResolutionStrategy)

    @Input
    @Optional
    Property<ResolutionStrategy> getResolutionStrategy() {
        resolutionStrategy
    }

    void setResolutionStrategy(Provider<ResolutionStrategy> value) {
        resolutionStrategy.set(value)
    }

    void setResolutionStrategy(String value) {
        resolutionStrategy.set(ResolutionStrategy.valueOf(value))
    }

}
