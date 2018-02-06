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
import wooga.gradle.unity.UnityAuthentication

class DefaultActivationActionFactory implements Factory<ActivationAction> {

    private final Instantiator instantiator
    private final FileResolver fileResolver
    private final Project project
    private final UnityAuthentication authentication

    DefaultActivationActionFactory(Project project, Instantiator instantiator, FileResolver fileResolver, UnityAuthentication authentication) {
        this.instantiator = instantiator
        this.fileResolver = fileResolver
        this.project = project
        this.authentication = authentication
    }

    @Override
    ActivationAction create() {
        instantiator.newInstance(DefaultActivationAction.class, project, fileResolver, authentication)
    }
}
