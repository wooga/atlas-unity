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
import org.gradle.api.tasks.*
import org.gradle.internal.reflect.Instantiator
import org.gradle.process.ExecSpec
import org.gradle.util.GUtil
import wooga.gradle.unity.batchMode.BatchModeFlags
import wooga.gradle.unity.batchMode.TestPlatform
import wooga.gradle.unity.testing.UnityTestTaskReport
import wooga.gradle.unity.testing.UnityTestTaskReportsImpl
import wooga.gradle.unity.utils.NUnitReportNormalizer
import wooga.gradle.unity.utils.ProjectSettings

import javax.inject.Inject

class Test extends AbstractUnityTask implements Reporting<UnityTestTaskReport> {

    static Logger logger = Logging.getLogger(Test)

    private final List<Object> filter = new ArrayList()
    private final List<Object> categories = new ArrayList()
    private final FileResolver fileResolver
    private TestPlatform testPlatform = TestPlatform.editmode
    private DefaultArtifactVersion unityVersion

    private final UnityTestTaskReport reports

    @Override
    AbstractUnityTask unityPath(File path) {
        unityVersion = null
        super.unityPath(path)
        return this
    }

    @Override
    void setUnityPath(File path) {
        unityVersion = null
        super.setUnityPath(path)
    }

    @Input
    @Optional
    List<String> getCategories() {
        List<String> args = new ArrayList<String>()
        for (Object argument : categories) {
            args.add(argument.toString())
        }
        return args
    }

    Test categories(Object... var1) {
        if (var1 == null) {
            throw new IllegalArgumentException("categories == null!")
        } else {
            this.categories.addAll(Arrays.asList(var1))
            return this
        }
    }

    Test categories(Iterable<?> var1) {
        GUtil.addToCollection(categories, var1)
        return this
    }

    Test setCategories(Iterable<?> var1) {
        this.categories.clear()
        GUtil.addToCollection(categories, var1)
        return this
    }

    @Input
    @Optional
    List<String> getFilter() {
        List<String> args = new ArrayList<String>()
        for (Object argument : filter) {
            args.add(argument.toString())
        }
        return args
    }

    Test filter(Object... var1) {
        if (var1 == null) {
            throw new IllegalArgumentException("filter == null!")
        } else {
            this.filter.addAll(Arrays.asList(var1))
            return this
        }
    }

    Test filter(Iterable<?> var1) {
        GUtil.addToCollection(filter, var1)
        return this
    }

    Test setFilter(Iterable<?> var1) {
        this.filter.clear()
        GUtil.addToCollection(filter, var1)
        return this
    }

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

    @Console
    boolean verbose = true

    @Console
    boolean teamcity = false

    @Inject
    protected Instantiator getInstantiator() {
        throw new UnsupportedOperationException()
    }

    @Inject
    Test(FileResolver fileResolver) {
        super(Test.class)
        this.fileResolver = fileResolver
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
        if (unityVersion.majorVersion == 5 && unityVersion.minorVersion == 5) {
            logger.info("activate unittests with ${BatchModeFlags.RUN_EDITOR_TESTS} switch")

            testArgs << BatchModeFlags.RUN_EDITOR_TESTS

            if (reports.getXml().enabled) {
                testArgs << BatchModeFlags.EDITOR_TEST_RESULTS_FILE << reports.getXml().destination
            }

            if (verbose) {
                testArgs << BatchModeFlags.EDITOR_TEST_VERBOSE_LOG
                if (teamcity) {
                    testArgs << 'teamcity'
                }
            }

            if (filter.size() > 0) {
                testArgs << BatchModeFlags.EDITOR_TEST_FILTER << filter.join(",")
            }

            if (categories.size() > 0) {
                testArgs << BatchModeFlags.EDITOR_TEST_CATEGORIES << categories.join(",")
            }
        } else if ((unityVersion.majorVersion == 5 && unityVersion.minorVersion == 6)
                || (unityVersion.majorVersion == 2017 && unityVersion.minorVersion == 1)) {
            logger.info("activate unittests with ${BatchModeFlags.RUN_TESTS} switch")

            //new unit test runner does not work in batchmode
            batchMode = false
            quit = false

            if (verbose) {
                logger.info("Option [verbose] not supported with unity version: ${unityVersion.toString()}")
            }

            if (filter.size() > 0) {
                logger.info("Option [filter] not supported with unity version: ${unityVersion.toString()}")
            }

            if (categories.size() > 0) {
                logger.info("Option [categories] not supported with unity version: ${unityVersion.toString()}")
            }

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

    DefaultArtifactVersion getUnityVersion(File pathToUnity) {
        if (unityVersion != null) {
            return unityVersion
        }

        String versionString = project.properties.get("defaultUnityTestVersion", "5.5.0")
        unityVersion = retrieveUnityVersion(project, pathToUnity, versionString)
        unityVersion
    }

    static DefaultArtifactVersion retrieveUnityVersion(Project project, File pathToUnity, String defaultVersion) {
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
                    String winPath = pathToUnity.path.replace('\\',"\\\\")
                    execSpec.commandLine "wmic", "datafile", "where", "Name=\"${winPath}\"", "get", "Version"
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


    boolean getPlayModeTestsEnabled() {
        def file = new File(projectPath, "ProjectSettings/ProjectSettings.asset")
        def settings = new ProjectSettings(file)
        return settings.playModeTestRunnerEnabled
    }
}
