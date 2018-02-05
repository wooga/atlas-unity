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

import org.gradle.api.Project
import org.gradle.api.internal.file.FileResolver
import org.gradle.internal.Factory
import org.gradle.internal.reflect.Instantiator

class DefaultBatchModeActionFactory implements Factory<BatchModeAction> {

    private final Instantiator instantiator
    private final FileResolver fileResolver
    private final Project project

    DefaultBatchModeActionFactory(Project project, Instantiator instantiator, FileResolver fileResolver) {
        this.instantiator = instantiator
        this.fileResolver = fileResolver
        this.project = project
    }

    @Override
    BatchModeAction create() {
        return instantiator.newInstance(DefaultBatchModeAction.class, project, fileResolver)
    }
}
