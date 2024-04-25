package wooga.gradle.unity.tasks

import wooga.gradle.unity.UnityTask

class GenerateSolution extends UnityTask {

    GenerateSolution() {
        outputs.upToDateWhen { false }

        this.executeMethod.set(project.provider {
            unityVersion.majorVersion >= 2022?
                    "Packages.Rider.Editor.RiderScriptEditor.SyncSolution" :
                    "UnityEditor.SyncVS.SyncSolution"
        })

    }
}
