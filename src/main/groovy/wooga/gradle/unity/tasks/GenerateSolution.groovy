package wooga.gradle.unity.tasks

import wooga.gradle.unity.UnityTask

class GenerateSolution extends UnityTask {

    GenerateSolution() {
        outputs.upToDateWhen { false }
        executeMethod = "UnityEditor.SyncVS.SyncSolution"
    }
}
