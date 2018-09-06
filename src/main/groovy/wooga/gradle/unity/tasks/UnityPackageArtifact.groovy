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

import org.gradle.api.artifacts.PublishArtifact
import org.gradle.api.internal.tasks.DefaultTaskDependency
import org.gradle.api.internal.tasks.TaskResolver
import org.gradle.api.tasks.TaskDependency

class UnityPackageArtifact implements PublishArtifact {


    @Override
    String getName() {
        return task.getArchiveName().split(".${UnityPackage.UNITY_PACKAGE_EXTENSION}")[0]
    }

    @Override
    String getExtension() {
        return UnityPackage.UNITY_PACKAGE_EXTENSION
    }

    @Override
    String getType() {
        return "zip"
    }

    @Override
    String getClassifier() {
        return null
    }

    @Override
    File getFile() {
        return this.task.getArchivePath()
    }

    @Override
    Date getDate() {
        return null
    }

    def taskDependency

    @Override
    TaskDependency getBuildDependencies() {
        taskDependency
    }
    private UnityPackage task

    static PublishArtifact fromTask(UnityPackage task) {
        new UnityPackageArtifact(task)
    }

    UnityPackageArtifact(UnityPackage task) {
        this.task = task
        taskDependency = new DefaultTaskDependency(task.project.tasks as TaskResolver)
        taskDependency.add(task)
    }
}
