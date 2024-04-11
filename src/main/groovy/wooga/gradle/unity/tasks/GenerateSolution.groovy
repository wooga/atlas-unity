package wooga.gradle.unity.tasks

import wooga.gradle.unity.UnityTask

class GenerateSolution extends UnityTask {

    GenerateSolution() {
        outputs.upToDateWhen { false }
        if(unityVersion.majorVersion < 2022) {
            executeMethod = "UnityEditor.SyncVS.SyncSolution"
        } else {
            //The rider package ships with unity >= 2019.2
            executeMethod = "Packages.Rider.Editor.RiderScriptEditor.SyncSolution"
        }
    }
}
