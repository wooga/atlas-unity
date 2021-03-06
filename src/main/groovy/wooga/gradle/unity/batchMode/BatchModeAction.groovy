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

package wooga.gradle.unity.batchMode

import org.gradle.process.ExecResult
import org.gradle.process.internal.ExecException

interface BatchModeAction extends BatchModeSpec {

    /**
     * Executes this with Unity action and returns the result as {@code ExecResult}
     * @return the result of the execution
     * @throws ExecException
     */
    ExecResult execute() throws ExecException
}
