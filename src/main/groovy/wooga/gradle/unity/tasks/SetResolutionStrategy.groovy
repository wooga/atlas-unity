package wooga.gradle.unity.tasks


import wooga.gradle.unity.models.UnityProjectManifest
import wooga.gradle.unity.traits.ResolutionStrategySpec
/**
 * Sets the project's package resolution strategy by modifying the project manifest
 */
class SetResolutionStrategy extends ProjectManifestTask
    implements ResolutionStrategySpec {

    @Override
    void modifyProjectManifest(UnityProjectManifest manifest) {
        manifest.setResolutionStrategy(resolutionStrategy.get())
    }

    SetResolutionStrategy() {
        onlyIf {
            resolutionStrategy.present
        }
    }
}
