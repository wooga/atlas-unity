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

import org.gradle.process.BaseExecSpec

interface BatchModeSpec extends BaseBatchModeSpec, BaseExecSpec{
    BatchModeSpec args(Object... var1)

    BatchModeSpec args(Iterable<?> var1)

    BatchModeSpec setArgs(Iterable<?> var1)

    List<String> getArgs()

    BuildTarget getBuildTarget()

    BatchModeSpec buildTarget(BuildTarget target)
    void setBuildTarget(BuildTarget target)

    Boolean getQuit()

    BatchModeSpec quit(Boolean value)
    void setQuit(Boolean value)

    Boolean getBatchMode()

    BatchModeSpec batchMode(Boolean value)
    void setBatchMode(Boolean value)

    Boolean getNoGraphics()

    BatchModeSpec noGraphics(Boolean value)
    void setNoGraphics(Boolean value)
}