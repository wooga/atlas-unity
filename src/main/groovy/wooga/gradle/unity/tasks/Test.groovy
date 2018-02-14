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

import org.apache.maven.artifact.versioning.DefaultArtifactVersion
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.internal.ClosureBackedAction
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.reporting.Reporting
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.StopExecutionException
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.reflect.Instantiator
import org.gradle.process.ExecSpec
import wooga.gradle.unity.UnityActionConvention
import wooga.gradle.unity.batchMode.BatchModeFlags
import wooga.gradle.unity.batchMode.TestPlatform
import wooga.gradle.unity.tasks.internal.AbstractUnityProjectTask
import wooga.gradle.unity.tasks.internal.AbstractUnityTask
import wooga.gradle.unity.testing.UnityTestTaskReport
import wooga.gradle.unity.testing.internal.UnityTestTaskReportsImpl
import wooga.gradle.unity.utils.internal.NUnitReportNormalizer
import wooga.gradle.unity.utils.internal.ProjectSettings

import javax.inject.Inject

/**
 * Executes Unity edit or play-mode test runner.
 * Example:
 * <pre>
 *     task activateUnity(type:wooga.gradle.unity.tasks.Test) {
 *         testPlatform = 'editMode'
 *         reports.xml.enabled = true
 *         reports.xml.destination = file('out/reports/NUnitReport.xml')
 *     }
 * </pre>
 */
class Test extends AbstractUnityProjectTask implements Reporting<UnityTestTaskReport> {

    private static Logger logger = Logging.getLogger(Test)

    private TestPlatform testPlatform = TestPlatform.editmode
    private DefaultArtifactVersion unityVersion

    private final UnityTestTaskReport reports

    @Override
    Test unityPath(File path) {
        unityVersion = null
        super.unityPath(path)
        return this
    }

    @Override
    void setUnityPath(File path) {
        unityVersion = null
        super.setUnityPath(path)
    }

    /**
     * The unity test-platform to invoke defaults to 'editmode'
     * @see TestPlatform
     * @return the test-platform
     */
    @Input
    @Optional
    TestPlatform getTestPlatform() {
        return testPlatform
    }

    void setTestPlatform(TestPlatform value) {
        testPlatform = value
    }

    void setTestPlatform(String value) {
        testPlatform = TestPlatform.valueOf(value)
    }

    Test testPlatform(TestPlatform value) {
        setTestPlatform(value)
        return this
    }

    Test testPlatform(String value) {
        setTestPlatform(value)
        return this
    }

    @Inject
    protected Instantiator getInstantiator() {
        throw new UnsupportedOperationException()
    }

    @Inject
    Test() {
        super(Test.class)
        description = "Executes Unity in batch mode and executes specified method"

        reports = instantiator.newInstance(UnityTestTaskReportsImpl.class, this)
        reports.xml.enabled = true
    }

    @Override
    UnityTestTaskReport getReports() {
        return reports
    }

    @Override
    UnityTestTaskReport reports(Closure closure) {
        return reports(new ClosureBackedAction<UnityTestTaskReport>(closure));
    }

    @Override
    UnityTestTaskReport reports(Action<? super UnityTestTaskReport> configureAction) {
        configureAction.execute(reports)
        return reports
    }

    @TaskAction
    @Override
    protected void exec() {
        DefaultArtifactVersion unityVersion = getUnityVersion(getUnityPath())
        logger.info("unity version major:${unityVersion.majorVersion} minor: ${unityVersion.minorVersion}")

        args(buildTestArguments(unityVersion))

        try {
            super.exec()
        }
        finally {
            //normalize test result file
            normalizeTestResult()
        }
    }

    //https://issues.jenkins-ci.org/browse/JENKINS-44072
    protected void normalizeTestResult() {
        File report = this.getReports().getXml().getDestination()
        if (report.exists()) {
            def result = NUnitReportNormalizer.normalize(report)
            logger.info("NUnitReportNormalizer result ${result}")
        }
    }

    protected List<String> buildTestArguments(DefaultArtifactVersion unityVersion) {
        def testArgs = []
        if ((unityVersion.majorVersion == 5 && unityVersion.minorVersion == 6)
                || (unityVersion.majorVersion == 2017 && unityVersion.minorVersion == 1)) {
            logger.info("activate unittests with ${BatchModeFlags.RUN_TESTS} switch")

            //new unit test runner does not work in batchmode
            batchMode = false
            quit = false

            testArgs << BatchModeFlags.RUN_TESTS

            if (reports.getXml().enabled) {
                testArgs << BatchModeFlags.TEST_RESULTS << reports.getXml().destination
            }

            testArgs << BatchModeFlags.TEST_PLATFORM << testPlatform

            if (testPlatform == TestPlatform.playmode && !getPlayModeTestsEnabled()) {
                throw new StopExecutionException("PlayMode tests not activated for this project. please activate playMode tests first")
            }

        } else {
            throw new StopExecutionException("Unit test feature not supported with unity version: ${unityVersion.toString()}")
        }
        testArgs
    }

    protected DefaultArtifactVersion getUnityVersion(File pathToUnity) {
        if (unityVersion != null) {
            return unityVersion
        }

        String versionString = project.properties.get("defaultUnityTestVersion", "5.6.0")
        unityVersion = retrieveUnityVersion(project, pathToUnity, versionString)
        unityVersion
    }

    private static DefaultArtifactVersion retrieveUnityVersion(Project project, File pathToUnity, String defaultVersion) {
        def versionString = defaultVersion
        String osName = System.getProperty("os.name").toLowerCase()
        if (osName.contains("mac os x")) {
            File infoPlist = new File(pathToUnity.parentFile.parentFile, "Info.plist")
            def standardOutput = new ByteArrayOutputStream()
            if (infoPlist.exists()) {
                def readResult = project.exec(new Action<ExecSpec>() {
                    @Override
                    void execute(ExecSpec execSpec) {
                        execSpec.standardOutput = standardOutput
                        execSpec.ignoreExitValue = true
                        execSpec.commandLine "defaults", "read", infoPlist.path, "CFBundleVersion"
                    }
                })
                if (readResult.exitValue == 0) {
                    versionString = standardOutput.toString().trim()
                    logger.info("Found unity version $versionString")
                }
            }
        }

        if (osName.contains("windows")) {
            def standardOutput = new ByteArrayOutputStream()
            def readResult = project.exec(new Action<ExecSpec>() {
                @Override
                void execute(ExecSpec execSpec) {
                    execSpec.standardOutput = standardOutput
                    execSpec.ignoreExitValue = true
                    String winPath = pathToUnity.path.replace('\\', "\\\\")

                    //Todo find a better solution to test this
                    String wmicPath = System.getenv("WMIC_PATH") ? System.getenv("WMIC_PATH") : "wmic"
                    execSpec.commandLine wmicPath, "datafile", "where", "Name=\"${winPath}\"", "get", "Version"
                }
            })
            if (readResult.exitValue == 0) {
                def wmicOut = standardOutput.toString().trim()
                def versionMatch = wmicOut =~ /(\d+)\.(\d+)\.(\d+)/
                if (versionMatch) {
                    versionString = versionMatch[0][0]
                    logger.info("Found unity version $versionString")
                }
            }
        }

        new DefaultArtifactVersion(versionString.split(/f|p/).first().toString())
    }


    protected boolean getPlayModeTestsEnabled() {
        def file = new File(projectPath, "ProjectSettings/ProjectSettings.asset")
        def settings = new ProjectSettings(file)
        return settings.playModeTestRunnerEnabled
    }
}
