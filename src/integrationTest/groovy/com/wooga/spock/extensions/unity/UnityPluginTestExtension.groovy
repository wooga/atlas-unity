/*
 * Copyright 2018-2020 Wooga GmbH
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

package com.wooga.spock.extensions.unity

import org.spockframework.runtime.extension.IAnnotationDrivenExtension
import org.spockframework.runtime.model.FeatureInfo
import org.spockframework.runtime.model.FieldInfo
import org.spockframework.runtime.model.MethodInfo
import org.spockframework.runtime.model.SpecInfo
import wooga.gradle.unity.UnityIntegrationSpec

class UnityPluginTestExtension implements IAnnotationDrivenExtension<UnityPluginTestOptions> {

    @Override
    void visitSpecAnnotation(UnityPluginTestOptions annotation, SpecInfo spec) {
        spec.addSetupInterceptor({ invocation ->
            def unitySpec = invocation.instance as UnityIntegrationSpec
            if (unitySpec) {
                applyAnnotation(annotation, unitySpec)
            }
            invocation.proceed()
        })
    }

    @Override
    void visitFeatureAnnotation(UnityPluginTestOptions annotation, FeatureInfo feature) {
        feature.spec.addSetupInterceptor({ invocation ->
            def unitySpec = invocation.instance as UnityIntegrationSpec
            if (invocation.feature == feature) {
                if (unitySpec) {
                    applyAnnotation(annotation, unitySpec)
                }
            }
            invocation.proceed()
        })
    }

    @Override
    void visitFixtureAnnotation(UnityPluginTestOptions annotation, MethodInfo fixtureMethod) {
    }

    @Override
    void visitFieldAnnotation(UnityPluginTestOptions annotation, FieldInfo field) {

    }

    @Override
    void visitSpec(SpecInfo spec) {
    }

    static void applyAnnotation(UnityPluginTestOptions annotation, UnityIntegrationSpec spec) {
        spec.options = annotation

    }
}
