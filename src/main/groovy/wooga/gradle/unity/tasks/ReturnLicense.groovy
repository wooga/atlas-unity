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

package wooga.gradle.unity.tasks

import org.gradle.api.internal.ConventionMapping
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.Factory
import org.gradle.process.ExecResult
import org.gradle.process.internal.ExecException
import wooga.gradle.unity.UnityPluginExtension
import wooga.gradle.unity.batchMode.ActivationAction
import wooga.gradle.unity.batchMode.BaseBatchModeSpec

class ReturnLicense extends AbstractUnityTask {

    private interface ExecuteExclude {
        ExecResult execute() throws ExecException
    }

    @Delegate(excludeTypes = [ExecuteExclude.class], interfaces = false)
    ActivationAction activationAction
    private ExecResult batchModeResult

    protected Factory<ActivationAction> retrieveActivationActionFactory() {
        return project.extensions.getByType(UnityPluginExtension).activationActionFactory
    }

    ReturnLicense() {
        super(ReturnLicense.class)
        activationAction = retrieveActivationActionFactory().create()
    }

    private File dir

    @SkipWhenEmpty
    @InputDirectory
    File getLicenseDirectory() {
        return dir
    }

    void setLicenseDirectory(File value) {
        dir = value
    }

    @TaskAction
    void returnLicense() {
        activationAction.returnLicense()
    }

    @Override
    ReturnLicense unityPath(File path) {
        activationAction.unityPath(path)
        return this
    }

    @Override
    ReturnLicense projectPath(File path) {
        activationAction.projectPath(path)
        return this
    }

    @Override
    ReturnLicense logFile(Object file) {
        activationAction.logFile(file)
        return this
    }

    @Override
    BaseBatchModeSpec retrieveAction() {
        return activationAction
    }

    /**
     * {@inheritDoc}
     */
    @Input
    boolean isIgnoreExitValue() {
        return activationAction.isIgnoreExitValue()
    }
}
