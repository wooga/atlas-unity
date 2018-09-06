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

package wooga.gradle.unity.tasks

import wooga.gradle.unity.tasks.internal.AbstractUnityProjectTask

/**
 * Run Unity and execute arbitrary tasks.
 * This task type wraps the <code>ExecSpec</code> and sets defaults to invoke Unity.
 * Example:
 * <pre>
 * {@code
 *      task activateUnity(type:wooga.gradle.unity.tasks.Unity) {
 *         args "-executeMethod", "Custom.UnityScript"
 *     }
 * }
 * </pre>
 */
class Unity extends AbstractUnityProjectTask {

    Unity() {
        super(Unity.class)
    }
}
