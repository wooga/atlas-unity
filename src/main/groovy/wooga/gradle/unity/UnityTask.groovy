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

import org.apache.maven.artifact.versioning.ArtifactVersion
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.io.LineBufferingOutputStream
import org.gradle.internal.io.TextStream
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec
import sun.reflect.misc.FieldUtil
import wooga.gradle.unity.models.UnityCommandLineOption
import wooga.gradle.unity.traits.ArgumentsSpec
import wooga.gradle.unity.traits.UnityCommandLineSpec
import wooga.gradle.unity.traits.UnitySpec
import wooga.gradle.unity.utils.FileUtils
import wooga.gradle.unity.utils.ForkTextStream
import wooga.gradle.unity.utils.UnityVersionManager

abstract class UnityTask extends DefaultTask
        implements UnitySpec,
                UnityCommandLineSpec,
                ArgumentsSpec {

    UnityTask() {
        // When this task is executed, we query the arguments to pass
        // onto the Unity process here. We generate a sequence of Unity's command line options
        // and also an additional one for our custom use
        wooga_gradle_unity_traits_ArgumentsSpec__arguments = project.provider({ getUnityCommandLineOptions() })
        wooga_gradle_unity_traits_ArgumentsSpec__additionalArguments = project.objects.listProperty(String)
        wooga_gradle_unity_traits_ArgumentsSpec__environment =  project.objects.mapProperty(String, String)
    }

    @TaskAction
    void exec() {
        ExecResult execResult = project.exec(new Action<ExecSpec>() {
            @Override
            void execute(ExecSpec exec) {

                if (!unityPath.present) {
                    throw new GradleException("Unity path is not set")
                }

                preExecute()

                def unityPath = unityPath.get().asFile.absolutePath
                def unityArgs = getAllArguments()
                def unityEnvironment = environment.get()
                def outputStream = getOutputStream()

                exec.with {
                    executable unityPath
                    args = unityArgs
                    standardOutput = outputStream
                    ignoreExitValue = true
                    environment = unityEnvironment
                }
            }
        })

        execResult.assertNormalExitValue()
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
    String fetchLogFilePath() {
        def unityVersion = getUnityVersion()

        // In Unity 2019 or greater, we need to pass this to properly redirect stdout
        // "-logFile -" In POSIX, - refers to stdout by convention
        if (logToStdout.get() && unityVersion.majorVersion >= 2019) {
            return "-"
        }

        if (unityLogFile.present) {
            FileUtils.ensureFile(unityLogFile)
            return unityLogFile.get().asFile.path
        }

        // If no file was found
        return null
    }

    private OutputStream getOutputStream() {
        OutputStream outputStream
        if (logToStdout.get()) {
            TextStream handler = new ForkTextStream()
            outputStream = new LineBufferingOutputStream(handler)

            if (unityLogFile.present) {
                handler.addWriter(unityLogFile.get().asFile.newPrintWriter())
            }
            handler.addWriter(System.out.newPrintWriter())
        } else {
            outputStream = new ByteArrayOutputStream()
        }
        return outputStream
    }


}
