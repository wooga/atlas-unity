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

import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.internal.file.PathToFileResolver
import org.gradle.process.ExecResult
import org.gradle.process.internal.ExecException
import org.gradle.process.internal.ExecHandle
import wooga.gradle.FileUtils
import wooga.gradle.unity.UnityAuthentication
import wooga.gradle.unity.UnityPluginExtension

import static org.gradle.util.ConfigureUtil.configureUsing

class DefaultActivationAction extends DefaultBatchModeAction implements ActivationAction {

    private final UnityAuthentication authentication

    UnityAuthentication getAuthentication() {
        return authentication
    }

    void setAuthentication(UnityAuthentication authentication) {
        if(authentication == null) {
            return
        }
        this.authentication.username = authentication.username
        this.authentication.password = authentication.password
        this.authentication.serial = authentication.serial
    }

    DefaultActivationAction authentication(Closure closure) {
        return authentication(configureUsing(closure))
    }

    DefaultActivationAction authentication(Action<? super UnityAuthentication> action) {
        action.execute(authentication)
        return this
    }

    DefaultActivationAction(Project project, PathToFileResolver fileResolver, UnityAuthentication authentication) {
        super(project, fileResolver)
        this.authentication = authentication
    }

    @Override
    ExecResult activate() {
        commandLine = setupActivationCommandline()
        return execute()
    }

    @Override
    ExecResult returnLicense() throws ExecException {
        commandLine = setupReturnLicenseCommandline()
        return execute()
    }

    @Override
    ExecResult execute() {
        ignoreExitValue = true
        ExecHandle execHandle = this.build()
        ExecResult execResult = execHandle.start().waitForFinish()

        if (execResult.exitValue != 0) {
            String errorMessage = UnityLogErrorReader.readErrorMessageFromLog(getLogFile())
            throw new ExecException(String.format("Unity authentication finished with non-zero exit value %d and error '%s'", Integer.valueOf(execResult.exitValue), errorMessage))
        }

        return execResult
    }

    List<String> setupReturnLicenseCommandline() {
        def authenticationArgs = []

        if (getUnityPath() == null || !getUnityPath().exists()) {
            throw new GradleException("Unity does not exist")
        }

        authenticationArgs << getUnityPath().path
        authenticationArgs << BatchModeFlags.BATCH_MODE
        authenticationArgs << BatchModeFlags.QUIT
        authenticationArgs << BatchModeFlags.RETURN_LICENSE
    }

    List<String> setupActivationCommandline() {
        def authenticationArgs = []

        if (getUnityPath() == null || !getUnityPath().exists()) {
            throw new GradleException("Unity does not exist")
        }

        if (getAuthentication().username == null) {
            throw new GradleException("Need a unity login username")
        }

        if (getAuthentication().password == null) {
            throw new GradleException("Need a unity login password")
        }

        if (getAuthentication().serial == null) {
            throw new GradleException("Need a unity serial")
        }

        authenticationArgs << getUnityPath().path
        authenticationArgs << BatchModeFlags.BATCH_MODE
        authenticationArgs << BatchModeFlags.QUIT

        authenticationArgs << BatchModeFlags.USER_NAME << getAuthentication().username
        authenticationArgs << BatchModeFlags.PASSWORD << getAuthentication().password
        authenticationArgs << BatchModeFlags.SERIAL << getAuthentication().serial

        if (getLogFile()) {
            FileUtils.ensureFile(getLogFile())
            authenticationArgs << BatchModeFlags.LOG_FILE << getLogFile().path
        }

        return authenticationArgs
    }
}
