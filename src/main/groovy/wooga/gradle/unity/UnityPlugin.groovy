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


import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.plugins.ReportingBasePlugin
import org.gradle.api.provider.Provider
import org.gradle.api.reporting.ReportingExtension
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.TaskProvider
import org.gradle.language.base.plugins.LifecycleBasePlugin
import wooga.gradle.unity.models.APICompatibilityLevel
import wooga.gradle.unity.models.DefaultUnityAuthentication
import wooga.gradle.unity.internal.DefaultUnityPluginExtension
import wooga.gradle.unity.models.TestPlatform
import wooga.gradle.unity.tasks.Activate
import wooga.gradle.unity.tasks.GenerateSolution
import wooga.gradle.unity.tasks.ReturnLicense
import wooga.gradle.unity.tasks.SetAPICompatibilityLevel
import wooga.gradle.unity.tasks.Test
import wooga.gradle.unity.utils.ProjectSettingsFile

/**
 * A {@link org.gradle.api.Plugin} which provides tasks to run unity batch-mode commands.
 * It runs and reports unity edit and play-mode test and is able to export `.unitypackage` files.
 * <p>
 * Example:
 * <pre>
 * {@code
 *     plugins {*         id "net.wooga.unity" version "0.16.0"
 *}*
 *     unity {*         authentication {*             username = "username@company.com"
 *             password = "password"
 *             serial = "unityserial"
 *}*
 *         exportUnityPackage {*             inputFiles file('Assets')
 *}*
 *         task(performBuild, type:wooga.gradle.unity.tasks.Unity) {*             args "-executeMethod", "MyEditorScript.PerformBuild"
 *}*
 *         task(performMultipleBuilds) {*             doLast {*                 unity.batchMode {*                     unityPath = project.file("/Applications/Unity-5.5.3f1/Unity.app/Contents/MacOS/Unity")
 *                     args "-executeMethod", "MyEditorScript.PerformBuild"
 *}*
 *                 unity.batchMode {*                     unityPath = project.file("/Applications/Unity-5.6.0f3/Unity.app/Contents/MacOS/Unity")
 *                     args "-executeMethod", "MyEditorScript.PerformBuild"
 *}*}*}*}*}
 * </pre>
 */
class UnityPlugin implements Plugin<Project> {

    enum Tasks {
        test(Test),
        testEditMode(Test),
        testPlayMode(Test),
        activateUnity(Activate),
        returnUnityLicense(ReturnLicense),
        setAPICompatibilityLevel(APICompatibilityLevel),
        unsetAPICompatibilityLevel(APICompatibilityLevel),
        generateSolution(GenerateSolution)

        private final Class taskClass

        Class getTaskClass() {
            taskClass
        }

        Tasks(Class _class) {
            this.taskClass = _class
        }
    }

    static final String EXTENSION_NAME = "unity"
    static final String GROUP = EXTENSION_NAME
    static final String PLUGIN_NAME = 'net.wooga.unity'

    @Override
    void apply(Project project) {

        project.pluginManager.apply(BasePlugin.class)
        project.pluginManager.apply(ReportingBasePlugin.class)

        def extension = project.extensions.create(UnityPluginExtension, EXTENSION_NAME, DefaultUnityPluginExtension)
        configureExtension(extension, project)
        addTasks(extension, project)
        configureUnityTasks(extension, project)
    }

    private static void configureExtension(UnityPluginExtension extension, Project project) {
        extension.projectDirectory.convention(project.layout.projectDirectory)
        extension.assetsDir.convention(extension.projectDirectory.dir("Assets"))
        extension.pluginsDir.convention(extension.assetsDir.dir("Plugins"))
        extension.logsDir.convention(UnityPluginConventions.logDirectory.getDirectoryValueProvider(project))

        extension.logCategory.convention(UnityPluginConventions.logCategory.getStringValueProvider(project))
        final ReportingExtension reportingExtension = (ReportingExtension) project.extensions.getByName(ReportingExtension.NAME)
        extension.reportsDir.convention(project.layout.buildDirectory.dir(project.provider({ reportingExtension.file("unity").path })))

        extension.licenseDirectory.convention(project.layout.buildDirectory.dir(project.provider({ UnityPluginConventions.licenseDirectory.path })))
        extension.autoActivateUnity.convention(true)
        extension.autoReturnLicense.convention(extension.autoActivateUnity)

        extension.unityPath.convention(UnityPluginConventions.unityPath.getFileValueProvider(project))

        extension.authentication = new DefaultUnityAuthentication(project.objects)
        extension.authentication.username.set(UnityPluginConventions.user.getStringValueProvider(project))
        extension.authentication.password.set(UnityPluginConventions.password.getStringValueProvider(project))
        extension.authentication.serial.set(UnityPluginConventions.serial.getStringValueProvider(project))

        extension.batchModeForEditModeTest.convention(UnityPluginConventions.batchModeForEditModeTest.getBooleanValueProvider(project))
        extension.batchModeForPlayModeTest.convention(UnityPluginConventions.batchModeForPlayModeTest.getBooleanValueProvider(project))

        extension.projectSettings.set(project.providers.provider({
            def file = new File(extension.projectDirectory.get().asFile.path, "ProjectSettings/ProjectSettings.asset")
            return new ProjectSettingsFile(file)
        }))
        extension.enableTestCodeCoverage.convention(UnityPluginConventions.enableTestCodeCoverage.getBooleanValueProvider(project))
    }

    private static void configureUnityTasks(UnityPluginExtension extension, final Project project) {
        project.tasks.withType(UnityTask).configureEach { t ->
            // Command-line options
            t.batchMode.convention(true)
            t.quit.convention(true)
            t.logToStdout.convention(t.logger.infoEnabled || t.logger.debugEnabled)
            t.toggleLogFile(true)
            t.buildTarget.convention(extension.defaultBuildTarget)

            // Properties used by tasks
            t.unityPath.convention(extension.unityPath)
            t.projectDirectory.convention(extension.projectDirectory)
            t.projectSettings.convention(extension.projectSettings)
            t.logCategory.convention(extension.logCategory)

            t.unityLogFile.convention(extension.logsDir.file(project.provider {
                t.logCategory.get().isEmpty() ? "${t.name}.log" : "${t.logCategory.get()}/${t.name}.log"
            }))
            t.environment.putAll(project.provider({System.getenv()}))
        }
    }

    private static void addTasks(UnityPluginExtension extension, Project project) {
        addTestTasks(project, extension)
        addSetAPICompatibilityLevelTasks(project, extension)
        addActivateAndReturnLicenseTasks(project, extension)
        addGenerateSolutionTask(project)
    }

    private static void addTestTasks(final Project project, final UnityPluginExtension extension) {

        // Create "container" tasks which will depend on any tasks of type Test with the given platforms
        def testEditModeTask = project.tasks.register(Tasks.testEditMode.toString(), { t ->
            t.group = GROUP
            t.dependsOn(project.tasks.withType(Test).matching {
                Test tt -> tt.name.startsWith(Tasks.testEditMode.toString())
            })
        })
        def testPlayModeTask = project.tasks.register(Tasks.testPlayMode.toString(), { t ->
            t.group = GROUP
            t.dependsOn(project.tasks.withType(Test).matching {
                Test tt -> tt.name.startsWith(Tasks.testPlayMode.toString())
            })
        })
        // Create the top-level task which depends on every other test task (due to using the containers above)
        def testTask = project.tasks.register(Tasks.test.toString(), { t ->
            t.group = GROUP
            t.dependsOn(testEditModeTask, testPlayModeTask)
        })

        // Generate playmode/editmode test tasks for test build targets
        project.afterEvaluate {
            extension.getTestBuildTargets(project).each { target ->
                def suffix = target.toString().capitalize()
                createTestTask(TestPlatform.editmode, project, Tasks.testEditMode.toString() + suffix, target)
                createTestTask(TestPlatform.editmode, project, Tasks.testPlayMode.toString() + suffix, target)
            }
        }

        // Override the batchmode property for edit/playmode test tasks by that of the extension
        project.tasks.withType(Test.class).configureEach({ Test t ->

            Provider<Boolean> testBatchModeProvider = project.provider {
                def tp = t.testPlatform.getOrNull()
                try {
                    if (tp) {
                        switch (tp as TestPlatform) {
                            case TestPlatform.editmode:
                                return extension.batchModeForEditModeTest.get()
                            case TestPlatform.playmode:
                                return extension.batchModeForPlayModeTest.get()
                        }
                    }
                }
                catch (e) {
                    logger.warn(e.toString())
                }
                true
            }
            t.batchMode.set(testBatchModeProvider)
            t.reports.xml.outputLocation.convention(extension.reportsDir.file(t.name + "/" + t.name + "." + reports.xml.name))
            t.enableCodeCoverage.convention(extension.enableTestCodeCoverage)
            t.coverageResultsPath.convention(extension.enableTestCodeCoverage.map {
                return it? extension.reportsDir.getOrElse(null)?.asFile?.absolutePath: null
            })
            t.coverageOptions.convention(extension.enableTestCodeCoverage.map {it? "generateAdditionalMetrics" : null})
            t.debugCodeOptimization.convention(extension.enableTestCodeCoverage) //needed from 2020.1 and on for coverage
        })

        // Make sure the lifecycle check task depends on our test task
        project.tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME).configure({ t ->
            t.dependsOn(testTask)
        })
    }

    private static TaskProvider<Test> createTestTask(TestPlatform platform, final Project project, String name, String buildTarget) {
        def task = project.tasks.register(name, Test,
                { t ->
                    t.testPlatform.set(platform.toString().toLowerCase())
                    t.group = GROUP
                    t.buildTarget.set(buildTarget)
                })
        task
    }

    private static void addSetAPICompatibilityLevelTasks(final Project project, final UnityPluginExtension extension) {

        // Read old value
        def projectSettings = extension.projectDirectory.map({ it.file(ProjectSettingsFile.filePath) })

        // Configure tasks
        def setTask = project.tasks.register(Tasks.setAPICompatibilityLevel.toString(), SetAPICompatibilityLevel.class)
        def unsetTask = project.tasks.register(Tasks.unsetAPICompatibilityLevel.toString(), SetAPICompatibilityLevel.class)
        setTask.configure(
                { t ->
                    t.settingsFile.set(projectSettings)
                    t.apiCompatibilityLevel.set(extension.apiCompatibilityLevel)
                    t.finalizedBy(unsetTask)
                })

        unsetTask.configure(
                { t ->
                    t.settingsFile.set(projectSettings)
                    t.apiCompatibilityLevel.set(setTask.get().previousAPICompatibilityLevel)
                    t.mustRunAfter(project.tasks.withType(UnityTask))
                })

        // Make other Unity tasks depend on these
        project.tasks.withType(UnityTask).configureEach({ ut ->
            ut.dependsOn setTask
        })
    }

    private static void addActivateAndReturnLicenseTasks(final Project project, final UnityPluginExtension extension) {

        def activateTask = project.tasks.register(Tasks.activateUnity.toString(), Activate)
                { t ->
                    t.group = GROUP
                    t.onlyIf(new Spec<Task>() {
                        @Override
                        boolean isSatisfiedBy(Task task) {
                            def cliTasks = project.gradle.startParameter.taskNames
                            Boolean cliActivateLicense = cliTasks.contains(Tasks.returnUnityLicense.toString())
                            Boolean autoActivate = extension.autoActivateUnity.get()
                            return cliActivateLicense || autoActivate
                        }
                    })
                }

        def returnLicenseTask = project.tasks.register(Tasks.returnUnityLicense.toString(), ReturnLicense)
                { t ->
                    t.group = GROUP
                    t.onlyIf(new Spec<Task>() {
                        @Override
                        boolean isSatisfiedBy(Task task) {
                            def cliTasks = project.gradle.startParameter.taskNames
                            Boolean cliReturnLicense = cliTasks.contains(Tasks.returnUnityLicense.toString())
                            Boolean activateDidWork = activateTask.get().didWork
                            Boolean didRunUnityTasks = project.gradle.taskGraph.allTasks.any { UnityTask.isInstance(it) }
                            Boolean autoReturn = extension.autoReturnLicense.get()
                            return cliReturnLicense || (activateDidWork && didRunUnityTasks && autoReturn)
                        }
                    })
                }

        activateTask.configure({ t ->
            t.finalizedBy(returnLicenseTask)
        })

        // Assign authentication to ALL tasks of type Activate
        project.tasks.withType(Activate).configureEach({ t ->
            t.authentication = extension.authentication
        })

        // Assign license to ALL tasks of type ReturnLicense
        project.tasks.withType(ReturnLicense).configureEach({ t ->
            t.licenseDirectory.convention(extension.licenseDirectory)
            t.onlyIf { extension.autoReturnLicense.get() }
        })

        project.tasks.withType(UnityTask).configureEach({ t ->
            if (!Activate.isInstance(t) && !ReturnLicense.isInstance(t)) {
                t.dependsOn(activateTask)
                t.finalizedBy(returnLicenseTask)
            }
        })
    }

    private static void addGenerateSolutionTask(Project project) {
        project.tasks.register(Tasks.generateSolution.toString(), GenerateSolution) {task ->
            task.description = "Generates a synchronized solution file for the unity project"
            task.group = GROUP
        }
    }
}
