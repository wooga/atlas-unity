/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
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
import org.gradle.api.internal.ConventionMapping
import org.gradle.api.internal.IConventionAware
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.plugins.ReportingBasePlugin
import org.gradle.api.reporting.Report
import org.gradle.api.reporting.ReportingExtension
import org.gradle.internal.reflect.Instantiator
import wooga.gradle.unity.tasks.Test

import javax.inject.Inject
import java.util.concurrent.Callable

class UnityPlugin implements Plugin<Project> {

    static String TEST_TASK_NAME = "test"
    static String EXTENSION_NAME = "unity"
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

        addTestTask()
        addDefaultReportTasks(extension)
    }

    private void addTestTask() {
        def task = project.tasks.create(name: TEST_TASK_NAME, type: Test, group: GROUP)
        project.tasks[BasePlugin.ASSEMBLE_TASK_NAME].dependsOn task
    }

    private void addDefaultReportTasks(final UnityPluginExtension extension) {
        project.getTasks().withType(Test.class, new Action<Test>() {
            @Override
            void execute(Test task) {
                configureUnityReportDefaults(extension, task)
            }
        })
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
