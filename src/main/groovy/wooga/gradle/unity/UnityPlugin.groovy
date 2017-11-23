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

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.CopySpec
import org.gradle.api.internal.ConventionMapping
import org.gradle.api.internal.IConventionAware
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.plugins.BasePluginConvention
import org.gradle.api.plugins.ReportingBasePlugin
import org.gradle.api.reporting.Report
import org.gradle.api.reporting.ReportingExtension
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.Delete
import org.gradle.internal.reflect.Instantiator
import org.gradle.language.base.plugins.LifecycleBasePlugin
import wooga.gradle.unity.batchMode.BuildTarget
import wooga.gradle.unity.batchMode.TestPlatform
import wooga.gradle.unity.tasks.*

import javax.inject.Inject
import java.util.concurrent.Callable

class UnityPlugin implements Plugin<Project> {

    static String TEST_TASK_NAME = "test"
    static String TEST_EDITOMODE_TASK_NAME = "testEditMode"
    static String TEST_PLAYMODE_TASK_NAME = "testPlayMode"
    static String ACTIVATE_TASK_NAME = "activateUnity"
    static String RETURN_LICENSE_TASK_NAME = "returnUnityLicense"
    static String EXPORT_PACKAGE_TASK_NAME = "exportUnityPackage"
    static String ASSEMBLE_RESOURCES_TASK_NAME = "assembleResources"
    static String SETUP_TASK_NAME = "setup"
    static String EXTENSION_NAME = "unity"
    static String UNITY_PACKAGE_CONFIGURATION_NAME = "unitypackage"
    static String ANDROID_RESOURCES_CONFIGURATION_NAME = "android"
    static String IOS_RESOURCES_CONFIGURATION_NAME = "ios"
    static String RUNTIME_CONFIGURATION_NAME = "runtime"
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
        UnityPluginExtension extension = project.extensions.create(EXTENSION_NAME, DefaultUnityPluginExtension, project, fileResolver, instantiator)

        final ReportingExtension reportingExtension = (ReportingExtension) project.getExtensions().getByName(ReportingExtension.NAME)
        ConventionMapping unityExtensionMapping = ((IConventionAware) extension).getConventionMapping()
        unityExtensionMapping.map("reportsDir", new Callable<Object>() {
            @Override
            Object call() {
                return reportingExtension.file("unity")
            }
        })

        unityExtensionMapping.map("assetsDir", new Callable<Object>() {
            @Override
            Object call() {
                return new File(extension.getProjectPath().path, "Assets")
            }
        })

        unityExtensionMapping.map("pluginsDir", new Callable<Object>() {
            @Override
            Object call() {
                return new File(extension.getAssetsDir(), "Plugins")
            }
        })

        unityExtensionMapping.map("androidResourceCopyMethod", new Callable<AndroidResourceCopyMethod>() {
            @Override
            AndroidResourceCopyMethod call() {
                return AndroidResourceCopyMethod.sync
            }
        })

        BasePluginConvention convention = new BasePluginConvention(project)

        addLifecycleTasks()
        addTestTasks(extension)
        addPackageTask()
        addActivateAndReturnLicenseTasks(extension)

        createUnityPackageConfiguration()
        createExternalResourcesConfigurations()

        addResourceCopyTasks()

        addDefaultReportTasks(extension)
        configureArchiveDefaults(convention)
        configureUnityTaskDependencies()
        configureCleanObjects()
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

    private void addLifecycleTasks() {
        def assembleResourcesTask = project.tasks.create(name: ASSEMBLE_RESOURCES_TASK_NAME, group: GROUP)
        assembleResourcesTask.description = "gathers all iOS and Android resources into Plugins/ directory of the unity project"
        project.tasks.create(name: SETUP_TASK_NAME, group: GROUP, dependsOn: assembleResourcesTask)
        project.tasks[BasePlugin.ASSEMBLE_TASK_NAME].dependsOn assembleResourcesTask
    }

    private void addResourceCopyTasks() {
        Configuration androidResources = project.configurations[ANDROID_RESOURCES_CONFIGURATION_NAME]
        Configuration iOSResources = project.configurations[IOS_RESOURCES_CONFIGURATION_NAME]
        UnityPluginExtension extension = project.extensions.getByName(EXTENSION_NAME)

        def assembleTask = project.tasks[ASSEMBLE_RESOURCES_TASK_NAME]

        Task iOSResourceCopy = project.tasks.create(name: "assembleIOSResources", group: GROUP)
        iOSResourceCopy.description = "gathers all additional iOS files into the Plugins/iOS directory of the unity project"
        iOSResourceCopy.dependsOn(iOSResources)
        iOSResourceCopy.doLast(new Action<Task>() {
            @Override
            void execute(Task task) {
                String collectDir = "${extension.getPluginsDir()}/iOS"
                def artifacts = iOSResources.resolve()
                def zipFrameworkArtifacts = artifacts.findAll { it.path =~ /\.framework.zip$/ }

                zipFrameworkArtifacts.each { artifact ->
                    def artifactName = artifact.name.replace(".zip", "")
                    project.sync(new Action<CopySpec>() {
                        @Override
                        void execute(CopySpec copySpec) {
                            copySpec.from project.zipTree(artifact)
                            copySpec.into "$collectDir/$artifactName"
                        }
                    })
                }

                project.copy(new Action<CopySpec>() {
                    @Override
                    void execute(CopySpec copySpec) {
                        copySpec.from iOSResources
                        copySpec.into "$collectDir"
                        copySpec.exclude "*.framework"
                        copySpec.exclude "*.framework.zip"
                    }
                })
            }
        })

        Task androidResourceCopy = project.tasks.create(name: "assembleAndroidResources", group: GROUP)
        androidResourceCopy.description = "gathers all *.jar and AndroidManifest.xml files into the Plugins/Android directory of the unity project"
        androidResourceCopy.dependsOn(androidResources)
        androidResourceCopy.doLast(new Action<Task>() {
            @Override
            void execute(Task task) {
                String collectDir = "${extension.pluginsDir}/Android"
                if (extension.androidResourceCopyMethod == AndroidResourceCopyMethod.sync) {
                    project.sync(new Action<CopySpec>() {
                        @Override
                        void execute(CopySpec copySpec) {
                            copySpec.from(androidResources)
                            copySpec.include '**/*.jar'
                            copySpec.include '**/*.aar'
                            copySpec.into collectDir
                        }
                    })
                } else if (extension.androidResourceCopyMethod == AndroidResourceCopyMethod.arrUnpack) {
                    def artifacts = androidResources.resolve()
                    def aarArtifacts = artifacts.findAll { it.path =~ /\.aar$/ }

                    aarArtifacts.each { artifact ->
                        def artifactName = artifact.name.replace(".aar", "")
                        project.sync(new Action<CopySpec>() {
                            @Override
                            void execute(CopySpec copySpec) {
                                copySpec.from project.zipTree(artifact)
                                copySpec.into "$collectDir/$artifactName"
                                copySpec.include 'AndroidManifest.xml'
                                copySpec.include '**/*.jar'
                                copySpec.rename(/classes\.jar/, "${artifactName}.jar")
                            }
                        })
                    }

                    project.sync(new Action<CopySpec>() {
                        @Override
                        void execute(CopySpec copySpec) {
                            copySpec.from androidResources
                            copySpec.into "$collectDir/libs"
                            copySpec.include '*.jar'
                        }
                    })
                }
            }
        })

        assembleTask.dependsOn androidResourceCopy
        assembleTask.dependsOn iOSResourceCopy
    }

    private void addPackageTask() {
        def task = project.tasks.create(name: EXPORT_PACKAGE_TASK_NAME, type: UnityPackage, group: GROUP)
        project.tasks[BasePlugin.ASSEMBLE_TASK_NAME].dependsOn task
    }

    private void addTestTasks(final UnityPluginExtension extension) {
        def testTask = project.tasks.create(name: TEST_TASK_NAME, group: GROUP)
        def testEditModeTask = project.tasks.create(name: TEST_EDITOMODE_TASK_NAME, group: GROUP)
        def testPlayModeTask = project.tasks.create(name: TEST_PLAYMODE_TASK_NAME, group: GROUP)
        testTask.dependsOn testEditModeTask, testPlayModeTask

        project.afterEvaluate {
            extension.testBuildTargets.each { target ->
                def suffix = target.toString().capitalize()
                testEditModeTask.dependsOn createTestTask(TEST_EDITOMODE_TASK_NAME + suffix, TestPlatform.editmode, target)
                testPlayModeTask.dependsOn createTestTask(TEST_PLAYMODE_TASK_NAME + suffix, TestPlatform.playmode, target)
            }
        }

        project.tasks[LifecycleBasePlugin.CHECK_TASK_NAME].dependsOn testTask
    }

    private Test createTestTask(String name, TestPlatform testPlatform, BuildTarget testBuildTarget) {
        def task = project.tasks.create(name: name, type: Test, group: GROUP) as Test
        task.testPlatform = testPlatform
        task.buildTarget = testBuildTarget
        task
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

    private void createExternalResourcesConfigurations() {
        Configuration androidConfiguration = project.configurations.maybeCreate(ANDROID_RESOURCES_CONFIGURATION_NAME)
        androidConfiguration.description = "android application resources"
        androidConfiguration.transitive = false

        Configuration iosConfiguration = project.configurations.maybeCreate(IOS_RESOURCES_CONFIGURATION_NAME)
        iosConfiguration.description = "ios application resources"
        iosConfiguration.transitive = false

        Configuration runtimeConfiguration = project.configurations.maybeCreate(RUNTIME_CONFIGURATION_NAME)
        runtimeConfiguration.transitive = true
        runtimeConfiguration.extendsFrom(androidConfiguration, iosConfiguration)
    }

    private void configureCleanObjects() {
        UnityPluginExtension extension = (UnityPluginExtension) project.extensions.getByName(EXTENSION_NAME)
        Delete cleanTask = (Delete) project.tasks[BasePlugin.CLEAN_TASK_NAME]

        cleanTask.delete({ new File(extension.getPluginsDir(), "iOS") })
        cleanTask.delete({ new File(extension.getPluginsDir(), "Android") })
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

    private void configureUnityTaskDependencies() {
        project.tasks.withType(AbstractUnityTask, new Action<AbstractUnityTask>() {
            @Override
            void execute(AbstractUnityTask task) {
                task.dependsOn project.tasks[SETUP_TASK_NAME]
            }
        })
    }
}
