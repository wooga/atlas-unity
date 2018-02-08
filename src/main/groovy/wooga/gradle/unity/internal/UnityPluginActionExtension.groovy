package wooga.gradle.unity.internal

import org.gradle.internal.Factory
import wooga.gradle.unity.batchMode.ActivationAction
import wooga.gradle.unity.batchMode.BatchModeAction

interface UnityPluginActionExtension {

    Factory<BatchModeAction> getBatchModeActionFactory()

    Factory<ActivationAction> getActivationActionFactory()
}

