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

package wooga.gradle.unity

import com.sun.org.apache.xpath.internal.operations.Bool
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
import wooga.gradle.unity.tasks.*

import javax.inject.Inject
import java.util.concurrent.Callable

class UnityPlugin implements Plugin<Project> {

    static String TEST_TASK_NAME = "test"
    static String ACTIVATE_TASK_NAME = "activateUnity"
    static String RETURN_LICENSE_TASK_NAME = "returnUnityLicense"
    static String EXPORT_PACKAGE_TASK_NAME = "exportUnityPackage"
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
        UnityPluginExtension extension = project.extensions.create("unity", DefaultUnityPluginExtension, project, fileResolver, instantiator)

        final ReportingExtension reportingExtension = (ReportingExtension) project.getExtensions().getByName(ReportingExtension.NAME)
        ((IConventionAware) extension).getConventionMapping().map("reportsDir", new Callable<Object>() {
            @Override
            Object call() {
                return reportingExtension.file("unity")
            }
        })

        BasePluginConvention convention = new BasePluginConvention(project)

        addTestTask()
        addPackageTask()
        addActivateAndReturnLicenseTasks(extension)

        createUnityPackageConfiguration()

        addDefaultReportTasks(extension)
        configureArchiveDefaults(convention)
        project.afterEvaluate(new Action<Project>() {
            @Override
            void execute(Project p) {
                configureAutoActivationDeactivation(p, extension)
            }
        })
    }

    private void configureAutoActivationDeactivation(final Project project, final UnityPluginExtension extension) {
        Task activationTask = project.tasks[ACTIVATE_TASK_NAME]
        Task returnLicenseTask = project.tasks[RETURN_LICENSE_TASK_NAME]

        project.getTasks().withType(AbstractUnityTask, new Action<AbstractUnityTask>() {
            @Override
            void execute(AbstractUnityTask task) {
                if (extension.autoActivateUnity) {
                    task.dependsOn activationTask
                }

                if (extension.autoReturnLicense) {
                    returnLicenseTask.mustRunAfter task
                    activationTask.finalizedBy returnLicenseTask
                }
            }
        })
    }

    private void addActivateAndReturnLicenseTasks(final UnityPluginExtension extension) {
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

    private void addPackageTask() {
        def task = project.tasks.create(name: EXPORT_PACKAGE_TASK_NAME, type: UnityPackage, group: GROUP)
        project.tasks[BasePlugin.ASSEMBLE_TASK_NAME].dependsOn task
    }

    private void addTestTask() {
        def task = project.tasks.create(name: TEST_TASK_NAME, type: Test, group: GROUP)
        project.tasks[LifecycleBasePlugin.CHECK_TASK_NAME].dependsOn task
    }

    private void addDefaultReportTasks(final UnityPluginExtension extension) {
        project.getTasks().withType(Test.class, new Action<Test>() {
            @Override
            void execute(Test task) {
                configureUnityReportDefaults(extension, task)
            }
        })
    }

    private void configureArchiveDefaults(final BasePluginConvention pluginConvention) {
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

                project.artifacts.add(UNITY_PACKAGE_CONFIGURATION_NAME, [file: task.archivePath, builtBy: task])
            }
        })
    }

    private void createUnityPackageConfiguration() {
        Configuration unityPackage = project.configurations.maybeCreate(UNITY_PACKAGE_CONFIGURATION_NAME)
        unityPackage.description = "unity package resource"
        unityPackage.transitive = false
    }

    private void configureUnityReportDefaults(final UnityPluginExtension extension, final Test task) {
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
