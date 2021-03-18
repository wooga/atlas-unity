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

package wooga.gradle.unity

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.internal.ConventionMapping
import org.gradle.api.internal.IConventionAware
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.plugins.BasePluginConvention
import org.gradle.api.plugins.ReportingBasePlugin
import org.gradle.api.reporting.Report
import org.gradle.api.reporting.ReportingExtension
import org.gradle.api.specs.Spec
import org.gradle.internal.reflect.Instantiator
import org.gradle.language.base.plugins.LifecycleBasePlugin
import wooga.gradle.unity.batchMode.BuildTarget
import wooga.gradle.unity.batchMode.TestPlatform
import wooga.gradle.unity.internal.DefaultUnityPluginExtension
import wooga.gradle.unity.tasks.Activate
import wooga.gradle.unity.tasks.ReturnLicense
import wooga.gradle.unity.tasks.SetAPICompatibilityLevel
import wooga.gradle.unity.tasks.Test
import wooga.gradle.unity.tasks.UnityPackage
import wooga.gradle.unity.tasks.UnityPackageArtifact
import wooga.gradle.unity.tasks.internal.AbstractUnityActivationTask
import wooga.gradle.unity.tasks.internal.AbstractUnityProjectTask
import wooga.gradle.unity.tasks.internal.AbstractUnityTask
import wooga.gradle.unity.utils.GenericUnityAsset

import javax.inject.Inject
import java.util.concurrent.Callable

/**
 * A {@link org.gradle.api.Plugin} which provides tasks to run unity batch-mode commands.
 * It runs and reports unity edit and play-mode test and is able to export `.unitypackage` files.
 * <p>
 * Example:
 * <pre>
 * {@code
 *     plugins {
 *         id "net.wooga.unity" version "0.16.0"
 *     }
 *
 *     unity {
 *         authentication {
 *             username = "username@company.com"
 *             password = "password"
 *             serial = "unityserial"
 *         }
 *
 *         exportUnityPackage {
 *             inputFiles file('Assets')
 *         }
 *
 *         task(performBuild, type:wooga.gradle.unity.tasks.Unity) {
 *             args "-executeMethod", "MyEditorScript.PerformBuild"
 *         }
 *
 *         task(performMultipleBuilds) {
 *             doLast {
 *                 unity.batchMode {
 *                     unityPath = project.file("/Applications/Unity-5.5.3f1/Unity.app/Contents/MacOS/Unity")
 *                     args "-executeMethod", "MyEditorScript.PerformBuild"
 *                 }
 *
 *                 unity.batchMode {
 *                     unityPath = project.file("/Applications/Unity-5.6.0f3/Unity.app/Contents/MacOS/Unity")
 *                     args "-executeMethod", "MyEditorScript.PerformBuild"
 *                 }
 *             }
 *         }
 *     }
 * }
 * </pre>
 */
class UnityPlugin implements Plugin<Project> {

    static String TEST_TASK_NAME = "test"
    static String TEST_EDITMODE_TASK_NAME = "testEditMode"
    static String TEST_PLAYMODE_TASK_NAME = "testPlayMode"
    static String ACTIVATE_TASK_NAME = "activateUnity"
    static String RETURN_LICENSE_TASK_NAME = "returnUnityLicense"
    static String EXPORT_PACKAGE_TASK_NAME = "exportUnityPackage"
    static String SET_API_COMPATIBILITY_LEVEL_TASK_NAME = "setAPICompatibilityLevel"
    static String UNSET_API_COMPATIBILITY_LEVEL_TASK_NAME = "unsetAPICompatibilityLevel"
    static String EXTENSION_NAME = "unity"
    static String UNITY_PACKAGE_CONFIGURATION_NAME = "unitypackage"
    static String GROUP = "unity"

    private Project project
    private final FileResolver fileResolver
    private final Instantiator instantiator

    @Inject
    UnityPlugin(FileResolver fileResolver, Instantiator instantiator) {
        this.fileResolver = fileResolver
        this.instantiator = instantiator
    }

    @Override
    void apply(Project project) {
        this.project = project
        project.pluginManager.apply(BasePlugin.class)
        project.pluginManager.apply(ReportingBasePlugin.class)

        final UnityPluginExtension unityExtension = project.extensions.create(EXTENSION_NAME, DefaultUnityPluginExtension, project, fileResolver, instantiator)
        final BasePluginConvention basePluginConvention = new BasePluginConvention(project)

        configureUnityExtensionConvention(project, unityExtension)
        createUnityPackageConfiguration(project)
        configureUnityTasks(project, unityExtension)
        addTestTasks(project, unityExtension)
        addPackageTask(project)
        addSetAPICompatibilityLevelTasks(project, unityExtension)
        configureSetAPICompatibilityLevelTasks(project)
        addActivateAndReturnLicenseTasks(project, unityExtension)
        addDefaultReportTasks(project, unityExtension)
        configureArchiveDefaults(project, basePluginConvention)

        project.afterEvaluate(new Action<Project>() {
            @Override
            void execute(Project p) {
                configureAutoActivationDeactivation(p, unityExtension)
            }
        })
    }

    private static void configureUnityExtensionConvention(final Project project, UnityPluginExtension unityExtension) {
        final ReportingExtension reportingExtension = (ReportingExtension) project.getExtensions().getByName(ReportingExtension.NAME)
        final ConventionMapping unityExtensionConvention = ((IConventionAware) unityExtension).getConventionMapping()
        unityExtensionConvention.map("reportsDir", { reportingExtension.file("unity") })
        unityExtensionConvention.map("assetsDir", { new File(unityExtension.getProjectPath().path, "Assets") })
        unityExtensionConvention.map("pluginsDir", { new File(unityExtension.getAssetsDir(), "Plugins") })
    }

    private static void configureUnityTasks(final Project project, UnityPluginExtension extension) {
        project.getTasks().withType(AbstractUnityTask, new Action<AbstractUnityTask>() {
            @Override
            void execute(AbstractUnityTask task) {
                ConventionMapping taskConventionMapping = task.conventionMapping
                applyBaseConvention(taskConventionMapping, extension)
                taskConventionMapping.map('logFile', {
                    project.file("${project.buildDir}/logs/${task.getLogCategory()}/${task.name}.log")
                })
            }
        })
    }

    static void applyBaseConvention(ConventionMapping taskConventionMapping, UnityPluginExtension extension) {
        taskConventionMapping.map "unityPath", { extension.unityPath }
        taskConventionMapping.map "logCategory", { extension.logCategory }
        taskConventionMapping.map "projectPath", { extension.projectPath }
        taskConventionMapping.map "redirectStdOut", { extension.redirectStdOut }
    }

    private static void configureAutoActivationDeactivation(
            final Project project, final UnityPluginExtension extension) {
        Task activationTask = project.tasks[ACTIVATE_TASK_NAME]
        Task returnLicenseTask = project.tasks[RETURN_LICENSE_TASK_NAME]

        project.getTasks().withType(AbstractUnityProjectTask, new Action<AbstractUnityProjectTask>() {
            @Override
            void execute(AbstractUnityProjectTask task) {
                if (!AbstractUnityActivationTask.isInstance(task)) {
                    if (extension.autoActivateUnity) {
                        task.dependsOn activationTask
                    }

                    if (extension.autoReturnLicense) {
                        returnLicenseTask.mustRunAfter task
                        activationTask.finalizedBy returnLicenseTask
                    }
                }
            }
        })
    }

    private static void configureSetAPICompatibilityLevelTasks(final Project project) {
        Task setAPICompLevel = project.tasks[SET_API_COMPATIBILITY_LEVEL_TASK_NAME]
        Task unsetAPICompLevel = project.tasks[UNSET_API_COMPATIBILITY_LEVEL_TASK_NAME]

        project.getTasks().withType(AbstractUnityProjectTask, new Action<AbstractUnityProjectTask>() {
            @Override
            void execute(AbstractUnityProjectTask task) {
                task.dependsOn setAPICompLevel
                unsetAPICompLevel.mustRunAfter task
                setAPICompLevel.finalizedBy unsetAPICompLevel
            }
        })
    }

    private static void addActivateAndReturnLicenseTasks(final Project project, final UnityPluginExtension extension) {
        Task activateTask = project.tasks.create(name: ACTIVATE_TASK_NAME, type: Activate, group: GROUP)
        ReturnLicense returnLicense = (ReturnLicense) project.tasks.create(name: RETURN_LICENSE_TASK_NAME, type: ReturnLicense, group: GROUP)
        returnLicense.licenseDirectory = extension.unityLicenseDirectory
        returnLicense.onlyIf(new Spec<Task>() {
            @Override
            boolean isSatisfiedBy(Task task) {
                def cliTasks = project.gradle.startParameter.taskNames
                Boolean cliReturnLicense = cliTasks.contains(RETURN_LICENSE_TASK_NAME)
                Boolean activateDidWork = activateTask.didWork
                Boolean didRunUnityTasks = project.gradle.taskGraph.allTasks.any { AbstractUnityTask.isInstance(it) }
                return cliReturnLicense || (activateDidWork && didRunUnityTasks)
            }
        })
    }

    private static void addSetAPICompatibilityLevelTasks(final Project project, final UnityPluginExtension extension) {

        // Read old value
        def projectSettings = new File(extension.getProjectPath(), "ProjectSettings/ProjectSettings.asset")

        // SET
        Task setTask = project.tasks.create(SET_API_COMPATIBILITY_LEVEL_TASK_NAME, SetAPICompatibilityLevel.class)
        setTask.settingsFile = { projectSettings }
        setTask.apiCompatibilityLevel = { extension.getApiCompatibilityLevel() }

        // UNSET
        Task unsetTask = project.tasks.create(UNSET_API_COMPATIBILITY_LEVEL_TASK_NAME, SetAPICompatibilityLevel.class)
        unsetTask.settingsFile = { projectSettings }
        unsetTask.apiCompatibilityLevel = { setTask.previousAPICompatibilityLevel }
    }

    private static void addPackageTask(final Project project) {
        def task = project.tasks.create(name: EXPORT_PACKAGE_TASK_NAME, type: UnityPackage, group: GROUP)
        project.tasks[BasePlugin.ASSEMBLE_TASK_NAME].dependsOn task
    }

    private static void addTestTasks(final Project project, final UnityPluginExtension extension) {
        def testTask = project.tasks.create(name: TEST_TASK_NAME, group: GROUP)
        def testEditModeTask = project.tasks.create(name: TEST_EDITMODE_TASK_NAME, group: GROUP)
        def testPlayModeTask = project.tasks.create(name: TEST_PLAYMODE_TASK_NAME, group: GROUP)
        testTask.dependsOn testEditModeTask, testPlayModeTask

        project.afterEvaluate {
            extension.testBuildTargets.each { target ->
                def suffix = target.toString().capitalize()
                testEditModeTask.dependsOn createTestTask(project, TEST_EDITMODE_TASK_NAME + suffix, TestPlatform.editmode, target)
                testPlayModeTask.dependsOn createTestTask(project, TEST_PLAYMODE_TASK_NAME + suffix, TestPlatform.playmode, target)
            }
        }

        project.tasks[LifecycleBasePlugin.CHECK_TASK_NAME].dependsOn testTask
    }

    private static Test createTestTask(
            final Project project, String name, TestPlatform testPlatform, BuildTarget testBuildTarget) {
        def task = project.tasks.create(name: name, type: Test, group: GROUP) as Test
        task.testPlatform = testPlatform
        task.buildTarget = testBuildTarget
        task
    }

    private static void addDefaultReportTasks(final Project project, final UnityPluginExtension extension) {
        project.getTasks().withType(Test.class, new Action<Test>() {
            @Override
            void execute(Test task) {
                ConventionMapping taskConventionMapping = task.getConventionMapping()
                Callable<Boolean> batchmode = new Callable<Boolean>() {
                    @Override
                    Boolean call() throws Exception {
                        extension.getBatchMode(task.getTestPlatform())
                    }
                }
                taskConventionMapping.map("batchMode", batchmode)
                configureUnityReportDefaults(extension, task)
            }
        })
    }

    private static void configureArchiveDefaults(final Project project, final BasePluginConvention pluginConvention) {
        def configuration = project.configurations.getByName(UNITY_PACKAGE_CONFIGURATION_NAME)
        project.getTasks().withType(UnityPackage.class, new Action<UnityPackage>() {
            void execute(UnityPackage task) {
                ConventionMapping taskConventionMapping = task.getConventionMapping()
                Callable destinationDir

                destinationDir = new Callable<File>() {
                    File call() throws Exception {
                        return pluginConvention.getDistsDir()
                    }
                }

                taskConventionMapping.map("destinationDir", destinationDir)
                taskConventionMapping.map("version", new Callable<String>() {
                    String call() throws Exception {
                        return project.getVersion() == "unspecified" ? null : project.getVersion().toString()
                    }
                })
                taskConventionMapping.map("baseName", new Callable<String>() {
                    String call() throws Exception {
                        return pluginConvention.getArchivesBaseName()
                    }
                })

                UnityPackageArtifact artifact = UnityPackageArtifact.fromTask(task)
                configuration.getArtifacts().add(artifact)
            }
        })
    }

    private static void createUnityPackageConfiguration(final Project project) {
        Configuration unityPackage = project.configurations.maybeCreate(UNITY_PACKAGE_CONFIGURATION_NAME)
        unityPackage.description = "unity package resource"
        unityPackage.transitive = false
    }

    private static void configureUnityReportDefaults(final UnityPluginExtension extension, final Test task) {
        task.getReports().all(new Action<Report>() {
            void execute(final Report report) {
                ConventionMapping mapping = ((IConventionAware) report).conventionMapping
                mapping.map("destination", new Callable<File>() {
                    File call() {
                        new File(extension.reportsDir, task.name + "/" + task.name + "." + report.name)
                    }
                })
            }
        })
    }
}
