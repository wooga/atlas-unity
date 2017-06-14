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
import org.gradle.api.internal.ClosureBackedAction
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.logging.Logging
import org.gradle.api.reporting.Reporting
import org.gradle.api.tasks.Console
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.StopExecutionException
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.reflect.Instantiator
import org.gradle.process.ExecSpec
import org.gradle.util.GUtil
import wooga.gradle.unity.batchMode.BatchModeFlags
import wooga.gradle.unity.batchMode.TestPlatform
import wooga.gradle.unity.testing.UnityTestTaskReport
import wooga.gradle.unity.testing.UnityTestTaskReportsImpl

import javax.inject.Inject

class Test extends AbstractUnityTask implements Reporting<UnityTestTaskReport> {

    static org.gradle.api.logging.Logger logger = Logging.getLogger(Test)

    private final List<Object> filter = new ArrayList()
    private final List<Object> categories = new ArrayList()
    private final FileResolver fileResolver
    private TestPlatform testPlatform = TestPlatform.editmode

    private final UnityTestTaskReport reports

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
        this.filter.clear()
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
        def testArgs = []
        DefaultArtifactVersion unityVersion = getUnityVersion(getUnityPath())

        logger.info("unity version major:${unityVersion.majorVersion} minor: ${unityVersion.minorVersion}")

        if(unityVersion.majorVersion == 5 && unityVersion.minorVersion == 5) {
            logger.info("execute unittests with ${BatchModeFlags.RUN_EDITOR_TESTS} switch")

            testArgs << BatchModeFlags.RUN_EDITOR_TESTS

            if(reports.getXml().enabled) {
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
        }
        else if(unityVersion.majorVersion == 5 && unityVersion.minorVersion == 6) {
            logger.info("execute unittests with ${BatchModeFlags.RUN_TESTS} switch")

            //new unit test runner does not work in batchmode
            batchMode = false
            quit = false

            if (verbose) {
                logger.warn("Option [verbose] not supported with unity version: ${unityVersion.toString()}")
            }

            if(filter.size() > 0) {
                logger.warn("Option [filter] not supported with unity version: ${unityVersion.toString()}")
            }

            if (categories.size() > 0) {
                logger.warn("Option [categories] not supported with unity version: ${unityVersion.toString()}")
            }

            testArgs << BatchModeFlags.RUN_TESTS

            if(reports.getXml().enabled) {
                testArgs << BatchModeFlags.TEST_RESULTS << reports.getXml().destination
            }

            testArgs << BatchModeFlags.TEST_PLATFORM << testPlatform
        }
        else
        {
            throw new StopExecutionException("Unit test feature not supported with unity version: ${unityVersion.toString()}")
        }

        args(testArgs)
        super.exec()
    }

    DefaultArtifactVersion getUnityVersion(File path) {
        String osName = System.getProperty("os.name").toLowerCase()
        def versionString = "5.5.0"
        if(osName.contains("mac os x")) {
            File infoPlist = new File(path.parentFile.parentFile, "Info.plist")
            def standardOutput = new ByteArrayOutputStream()
            if(infoPlist.exists()) {
                def readResult = project.exec(new Action<ExecSpec>() {
                    @Override
                    void execute(ExecSpec execSpec) {
                        execSpec.standardOutput = standardOutput
                        execSpec.ignoreExitValue = true
                        execSpec.commandLine "defaults", "read", infoPlist.path, "CFBundleVersion"
                    }
                })
                if(readResult.exitValue == 0) {
                    versionString = standardOutput.toString().trim()
                    logger.info("Found unity version $versionString")
                }
            }
        }

        return new DefaultArtifactVersion(versionString.split(/f|p/).first())
    }
}
