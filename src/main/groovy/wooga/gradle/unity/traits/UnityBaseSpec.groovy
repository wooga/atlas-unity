/*
 * Copyright 2021 Wooga GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package wooga.gradle.unity.traits

import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ProviderFactory
import org.gradle.internal.impldep.org.eclipse.jgit.errors.NotSupportedException

import javax.inject.Inject

trait UnityBaseSpec {

    @Inject
    ProjectLayout getLayout() {
        throw new NotSupportedException("ProjectLayout is supposed to be injected here by gradle")
    }
    @Inject
    ProviderFactory getProviderFactory() {
        throw new NotSupportedException("ProviderFactory is supposed to be injected here by gradle")
    }

    @Inject
    ObjectFactory getObjects() {
        throw new NotSupportedException("ObjectFactory is supposed to be injected here by gradle")
    }

}
