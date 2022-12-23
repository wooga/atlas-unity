/*
 * Copyright 2021 Wooga GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package wooga.gradle.unity

import com.wooga.gradle.ArgumentsSpec
import com.wooga.gradle.io.FileUtils
import com.wooga.gradle.io.OutputStreamSpec
import com.wooga.gradle.io.ProcessExecutor
import com.wooga.gradle.io.TextStream
import groovy.transform.InheritConstructors
import org.apache.maven.artifact.versioning.ArtifactVersion
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec
import wooga.gradle.unity.models.UnityCommandLineOption
import wooga.gradle.unity.traits.UnityCommandLineSpec
import wooga.gradle.unity.traits.UnitySpec
import wooga.gradle.unity.utils.UnityVersionManager

import javax.annotation.Nullable

@InheritConstructors
class UnityExecutionException extends Exception {
}

abstract class UnityTask extends DefaultTask
    implements UnitySpec,
        UnityCommandLineSpec,
        ArgumentsSpec,
        OutputStreamSpec {

    UnityTask() {
        // When this task is executed, we query the arguments to pass
        // onto the Unity process here. We generate a sequence of Unity's command line options
        // and also an additional one for our custom use
        internalArguments = project.provider({ getUnityCommandLineOptions() })
    }

    @TaskAction
    void exec() {

        preExecute()

        def logFile = unityLogFile.asFile.get()
        def _arguments = arguments.get()
        def stdoutStream = getOutputStream(logFile)
        def stderrStream = new ByteArrayOutputStream()

        def executor = ProcessExecutor.from(project)
            .withExecutable(unityPath.get().asFile)
            .withArguments(_arguments)
            .withEnvironment(environment.get())
            .withStandardOutput(stdoutStream)
            .withStandardError(stderrStream)
            .ignoreExitValue()

        ExecResult execResult = executor.execute()

        if (execResult.exitValue != 0) {
            String message = ""
            // Only write out the message if not already set to --info
            if (!logger.infoEnabled) {
                def stdout = logFile.text
                def stderr = new String(stderrStream.toByteArray())
                message = stderr ? stderr : stdout
            }
            throw new UnityExecutionException("Failed during execution of the Unity process with arguments:\n${_arguments}\n${message}")
        }

        postExecute(execResult)
    }

    /**
     * Invoked before the task executes the Unity process
     */
    protected void preExecute() {
        if (projectDirectory.present && !isSet(UnityCommandLineOption.projectPath)) {
            projectPath = projectDirectory.get().asFile.path
        }
    }

    /**
     * Invoked after the task executes the Unity process
     * @param result The result of the Unity execution
     */
    protected void postExecute(ExecResult result) {
    }

    @Internal
    protected ArtifactVersion getUnityVersion() {
        File file = unityPath.present ? unityPath.get().asFile : null
        UnityVersionManager.retrieveUnityVersion(file, UnityPluginConventions.defaultUnityTestVersion.getValue(project).toString())
    }

    @Override
    String resolveLogFilePath() {
        def unityVersion = getUnityVersion()

        // In Unity 2019 or greater, we need to pass this to properly redirect stdout
        // "-logFile -" In POSIX, - refers to stdout by convention
        if (logToStdout.get() && unityVersion.majorVersion >= 2019) {
            return "-"
        }

        // If there's a provided log file path
        // AND we don't want to log to std out
        if (unityLogFile.present
            && !(logToStdout.present && logToStdout.get())) {
            FileUtils.ensureFile(unityLogFile)
            return unityLogFile.get().asFile.path
        }

        // If no file was found
        return null
    }
}
