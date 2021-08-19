package wooga.gradle.unity.tasks

import wooga.gradle.unity.UnityTask

class GenerateSolution extends UnityTask {

    GenerateSolution() {
        executeMethod = "UnityEditor.SyncVS.SyncSolution"
        quit = true
    }
}
