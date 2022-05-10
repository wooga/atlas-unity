package wooga.gradle.unity.tasks


import wooga.gradle.unity.UnityTask
/***
 * Creates an empty Unity project
 */
class CreateProject extends UnityTask {
    CreateProject() {
        createProject.set(providers.provider({ projectDirectory.get().asFile.path }))
        buildTarget.set("Android")
    }
}
