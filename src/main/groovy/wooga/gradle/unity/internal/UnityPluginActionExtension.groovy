/*
 * Copyright 2018 Wooga GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
