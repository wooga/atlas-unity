/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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

/**
 * An Activation action provides methods to activate a Unity instance and return
 * a license to the license server.
 */
interface ActivationAction extends ActivationSpec, BatchModeSpec {

    /**
     * Executes a Unity license activation with the configured credentials.
     *
     * @return the result of the execution
     * @throws ExecException
     */
    ExecResult activate() throws ExecException

    /**
     * Executes a Unity return license action.
     *
     * @return the result of the execution
     * @throws ExecException
     */
    ExecResult returnLicense() throws ExecException
}