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
import groovy.transform.InheritConstructors
import org.apache.maven.artifact.versioning.ArtifactVersion
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecResult
import wooga.gradle.unity.internal.ForkedOutputStream
import wooga.gradle.unity.models.UnityCommandLineOption
import wooga.gradle.unity.traits.RetrySpec
import wooga.gradle.unity.traits.UnityCommandLineSpec
import wooga.gradle.unity.traits.UnitySpec
import wooga.gradle.unity.utils.UnityVersionManager

import java.time.Duration
import java.util.function.Function
import java.util.function.Supplier

@InheritConstructors
class UnityExecutionException extends Exception {
}


abstract class UnityTask extends DefaultTask
    implements UnitySpec,
            RetrySpec,
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
        def retryCount = maxRetries.get();
        def retryWait = retryWait.get();
        def retryPatterns = retryRegexes.get()
        ByteArrayOutputStream lastStdoutStream = null
        ByteArrayOutputStream lastStderrStream = null

        def execResult = retryOn(retryCount, retryWait, { ->
            lastStdoutStream = new ByteArrayOutputStream()
            lastStderrStream = new ByteArrayOutputStream()
            def sout = new ForkedOutputStream(lastStdoutStream, getOutputStream(logFile))
            def serr = lastStderrStream
            sout.withStream { stdoutStream -> serr.withStream { stderrStream ->
                return ProcessExecutor.from(project)
                            .withExecutable(unityPath.get().asFile)
                            .withArguments(_arguments)
                            .withEnvironment(environment.get())
                            .withStandardOutput(stdoutStream)
                            .withStandardError(stderrStream)
                            .ignoreExitValue()
                       .execute()
            }}
        }, {retries ->
            def stdout = new String(lastStdoutStream.toByteArray(), "UTF-8")
            def licenseFailure = retryPatterns.any {pattern ->
                return pattern.asPredicate().test(stdout)
            }
            if(licenseFailure) {
                logger.info("Unity Editor failed to acquire license, will try again in ${retryWait}. " +
                        "There are ${retries-1} attempts left.")
            }
            return licenseFailure
        })

        if (execResult.exitValue != 0) {
            String message = ""
            // Only write out the message if not already set to --info
            if (!logger.infoEnabled) {
                def stdout = logFile.text
                def stderr = lastStderrStream? new String(lastStderrStream.toByteArray()) : ""
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

    static <T extends ExecResult> T retryOn(int maxRetries, Duration wait, Supplier<T> operation, Function<Integer, Boolean> condition) {
        def remainingRetries = maxRetries
        def shouldRetry = false
        ExecResult result
        do {
            result = operation()
            if(result.exitValue != 0) {
                if(condition.apply(remainingRetries)) {
                    shouldRetry = true
                    Thread.yield()
                    Thread.sleep(wait.toMillis())
                } else {
                    shouldRetry = false
                }
            }
            remainingRetries--
        } while(remainingRetries > 0 && shouldRetry)
        return result
    }
}


