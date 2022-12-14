package wooga.gradle.unity.tasks

import wooga.gradle.unity.UnityTask

/**
 * A task that will set up the Unity project by just invoking it
 * from batchmode (opening then closing the project).
 * This, for example, ensures the packages are resolved.
 */
class SetupProject extends UnityTask {

    static String manifestFileName = "manifest.json"
    static String lockFileName = "packages-lock.json"

    SetupProject() {
        batchMode.set(true)
        description = "Resolves packages through UPM"
    }
}
