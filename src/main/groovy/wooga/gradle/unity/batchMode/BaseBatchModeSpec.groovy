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

import org.gradle.api.internal.IConventionAware
import wooga.gradle.unity.UnityActionConvention

/**
 * Base Unity batchmode settings.
 */
trait BaseBatchModeSpec<T extends BaseBatchModeSpec> extends UnityActionConvention implements IConventionAware {

    /**
     * Returns a {@code File} path to a log file location.
     * @return path to logfile
     */
    File logFile

    /**
     * Sets custom path to Unity logfile location.
     * @param file the log file
     * @return this
     */
    abstract T logFile(Object file)
}
