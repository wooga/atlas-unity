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
