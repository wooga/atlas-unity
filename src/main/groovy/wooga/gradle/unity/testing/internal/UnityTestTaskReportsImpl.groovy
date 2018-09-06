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

package wooga.gradle.unity.testing.internal

import org.gradle.api.Task
import org.gradle.api.reporting.ConfigurableReport
import org.gradle.api.reporting.Report
import org.gradle.api.reporting.internal.TaskGeneratedSingleFileReport
import org.gradle.api.reporting.internal.TaskReportContainer
import wooga.gradle.unity.testing.UnityTestTaskReport

class UnityTestTaskReportsImpl extends TaskReportContainer<Report> implements UnityTestTaskReport {

    UnityTestTaskReportsImpl(Task task) {
        super(ConfigurableReport.class, task)
        add(TaskGeneratedSingleFileReport.class, "xml", task)
    }

    @Override
    TaskGeneratedSingleFileReport getXml() {
        return (TaskGeneratedSingleFileReport) getByName("xml")
    }
}
