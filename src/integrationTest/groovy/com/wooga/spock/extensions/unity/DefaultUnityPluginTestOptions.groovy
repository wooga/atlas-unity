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

import java.lang.annotation.Annotation

class DefaultUnityPluginTestOptions implements UnityPluginTestOptions {
    Boolean applyPlugin = true
    UnityPathResolution unityPath = UnityPathResolution.Mock
    Boolean addPluginTestDefaults = true
    Boolean disableAutoActivateAndLicense = true

    Boolean addMockTask = true
    Boolean forceMockTaskRun = true
    Boolean clearMockTaskActions = false
    Boolean writeMockExecutable = true

    boolean applyPlugin() {
        applyPlugin
    }

    UnityPathResolution unityPath() {
        unityPath
    }

    @Override
    boolean writeMockExecutable() {
        writeMockExecutable
    }

    boolean addPluginTestDefaults() {
        addPluginTestDefaults
    }

    @Override
    boolean disableAutoActivateAndLicense() {
        disableAutoActivateAndLicense
    }

    boolean addMockTask() {
        addMockTask
    }

    boolean forceMockTaskRun() {
        forceMockTaskRun
    }

    boolean clearMockTaskActions() {
        clearMockTaskActions
    }

    @Override
    Class<? extends Annotation> annotationType() {
        return null
    }
}
