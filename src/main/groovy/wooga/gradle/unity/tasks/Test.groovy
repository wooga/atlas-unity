package wooga.gradle.unity.tasks


import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.StopExecutionException
import org.gradle.internal.reflect.Instantiator
import org.gradle.process.ExecResult
import wooga.gradle.unity.UnityTask
import wooga.gradle.unity.models.TestPlatform
import wooga.gradle.unity.models.UnityCommandLineOption
import wooga.gradle.unity.traits.UnityTestSpec
import wooga.gradle.unity.utils.NUnitReportNormalizer
import wooga.gradle.unity.utils.UnityTestTaskReport
import wooga.gradle.unity.utils.UnityTestTaskReportsImpl

import javax.inject.Inject

abstract class Test extends UnityTask implements UnityTestSpec {
    @Inject
    protected Instantiator getInstantiator() {
        throw new UnsupportedOperationException()
    }

    // Failed to use Property<>
    private UnityTestTaskReport reports

    @Internal
    UnityTestTaskReport getReports() {
        reports
    }

    void setReports(Provider<UnityTestTaskReport> value) {
        reports = value.get()
    }

    @Inject
    Test() {
        description = "Executes Unity in batch mode and executes specified method"
        reports = instantiator.newInstance(UnityTestTaskReportsImpl.class, this)
        reports.xml.enabled = true
    }

    Test(TestPlatform testPlatform) {
        this()
        setTestPlatform(testPlatform)
    }

    @Override
    protected void setCommandLineOptionDefaults() {
        super.setCommandLineOptionDefaults()
        quit = false
        runTests = true
    }

    @Override
    protected void preExecute() {
        super.preExecute()

        if ((unityVersion.majorVersion == 5 && unityVersion.minorVersion == 6)
                || unityVersion.majorVersion <= 2020) {

            // BatchMode for tests was broken for unity 2017 and below
            if (unityVersion.majorVersion < 2018) {
                batchMode.convention(false)
            }

            if (reports.xml.enabled) {
                getCommandLineOption(UnityCommandLineOption.testResults).arguments.convention(reports.xml.destination.path)
                //setTestResults(reports.xml.destination.path)
            }

            if (testPlatform.getOrNull() == TestPlatform.playmode.toString() &&
                    !projectSettings.get().playModeTestRunnerEnabled) {
                throw new StopExecutionException("PlayMode tests not activated for this project. Please activate PlayMode tests first")
            }

        } else {
            throw new StopExecutionException("Unit test feature not supported with unity version: ${unityVersion.toString()}")
        }
    }

    @Override
    protected void postExecute(ExecResult result) {
        normalizeTestResult()
    }

    //https://issues.jenkins-ci.org/browse/JENKINS-44072
    protected void normalizeTestResult() {
        File report = this.reports.xml.destination
        if (report != null && report.exists()) {
            def result = NUnitReportNormalizer.normalize(report)
            logger.info("NUnitReportNormalizer result ${result}")
        }
    }
}


