package wooga.gradle.unity.internal

import org.gradle.internal.Factory
import wooga.gradle.unity.batchMode.ActivationAction
import wooga.gradle.unity.batchMode.BatchModeAction

/**
 * This type provides methods to create Unity batchmode actions.
 */
interface UnityPluginActionExtension {

    /**
     * Returns a {@code Factory} to create a batchmode action from.
     * <p>
     * The action can be further configured.
     *
     * @return a batchmode action factory
     * @see wooga.gradle.unity.batchMode.BatchModeAction
     */
    Factory<BatchModeAction> getBatchModeActionFactory()

    /**
     * Returns a {@code Factory} to create a activation action from.
     * <p>
     * The action can be further configured.
     *
     * @return a activation action factory
     * @see wooga.gradle.unity.batchMode.ActivationAction
     */
    Factory<ActivationAction> getActivationActionFactory()
}
