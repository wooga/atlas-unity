/*
 * Copyright 2018 Wooga GmbH
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

package wooga.gradle.unity.tasks

import org.apache.maven.artifact.versioning.ArtifactVersion
import org.apache.maven.artifact.versioning.DefaultArtifactVersion
import org.gradle.api.Action
import org.gradle.api.internal.ClosureBackedAction
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.reporting.Reporting
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.StopExecutionException
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.reflect.Instantiator
import wooga.gradle.unity.batchMode.BatchModeFlags
import wooga.gradle.unity.batchMode.TestPlatform
import wooga.gradle.unity.tasks.internal.AbstractUnityProjectTask
import wooga.gradle.unity.testing.UnityTestTaskReport
import wooga.gradle.unity.testing.internal.UnityTestTaskReportsImpl
import wooga.gradle.unity.utils.internal.NUnitReportNormalizer
import wooga.gradle.unity.utils.internal.ProjectSettings
import wooga.gradle.unity.utils.internal.UnityVersionManager

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
        ArtifactVersion unityVersion = getUnityVersion(getUnityPath())
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

    protected List<String> buildTestArguments(ArtifactVersion unityVersion) {
        def testArgs = []
        if ((unityVersion.majorVersion == 5 && unityVersion.minorVersion == 6)
                || unityVersion.majorVersion <= 2020) {
            logger.info("activate unittests with ${BatchModeFlags.RUN_TESTS} switch")

            // BatchMode for tests was broken for unity 2017 and below
            if(unityVersion.majorVersion < 2018) {
                batchMode = false
            }
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

    protected ArtifactVersion getUnityVersion(File pathToUnity) {
        if (unityVersion != null) {
            return unityVersion
        }

        String versionString = project.properties.get("defaultUnityTestVersion", "5.6.0")
        UnityVersionManager.retrieveUnityVersion(pathToUnity, versionString)
    }

    protected boolean getPlayModeTestsEnabled() {
        def file = new File(projectPath, "ProjectSettings/ProjectSettings.asset")
        def settings = new ProjectSettings(file)
        return settings.playModeTestRunnerEnabled
    }
}
