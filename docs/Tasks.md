# Tasks
The plugin will add a number of tasks you can use

| Task name          | Depends on            | Type                                           | Description |
| ------------------ | --------------------- | ---------------------------------------------- | ----------- |
| activateUnity      |                       | `wooga.gradle.unity.tasks.Activate`            | Activates unity with provides credentials. Gets skipped when credentials are missing |
| returnUnityLicense |                       | `wooga.gradle.unity.tasks.ReturnLicense`       | Returns the current licesne to license server. Gets skipped when license directory is empty |
| exportUnityPackage | activationTask, setup | `wooga.gradle.unity.tasks.UnityPackage`        | exports configured assets into an `.unitypackage` file |
| test               | testEditMode, testPlayMode | `DefaultTask`                             | runs editMode and playMode tasks |
| testEditMode       | activationTask, setup | `DefaultTask`                                  | runs testEditMode for all testBuildTargets (`testEditModeAndroid`, `testEditModeIos` ...)|
| testPlayMode       | activationTask, setup | `DefaultTask`                                  | runs testPlayMode for all testBuildTargets (`testPlayModeAndroid`, `testPlayModeIos` ...) |
| testEditMode[TestBuildTarget]       | testEditMode | `wooga.gradle.unity.tasks.Task`        | runs unity editor tests on `testBuildTarget` and writes reports to `reportsDir` |
| testPlayMode[TestBuildTarget]       | testPlayMode | `wooga.gradle.unity.tasks.Task`        | runs unity playMode tests on `testBuildTarget` and writes reports to `reportsDir` |

**Example Test Task structure:**

```
:check
\--- :test
     +--- :testEditMode
     |    +--- :testEditModeAndroid
     |    \--- :testEditModeIos
     \--- :testPlayMode
          +--- :testPlayModeAndroid
          \--- :testPlayModeIos
```


### Custom Batchmode task
You can call any abitary unity batchmode command either with the `BatchModeSpec` or the `wooga.gradle.unity.tasks.Unity` task type:

**build.gradle**
```groovy
task(performBatchmodeTask, type:wooga.gradle.unity.tasks.Unity) {
    args "-executeMethod", "MyEditorScript.PerformBuild"
}
```
-or-
```groovy
task(performBatchmodeSpec) {
    doLast {
        unity.batchMode {
            unityPath = project.file("/Applications/Unity-5.5.3f1/Unity.app/Contents/MacOS/Unity")
        }
    }
}
```

The `Unity` task type is the prefered way of calling unity batchmode commands because it works better with `autoActivation/ReturnLicense`
If you want to use this feature along with one or multiple tasks running the `BatchModeSpec` you need to make sure the task `returnUnityLicense` runs after these custom tasks.
See the [unity manual][unity_cmd] for a complete list of possible batchmode arguments.

**build.gradle**
```groovy
task(performBatchmodeSpec) {
    ...
}

tasks.returnUnityLicense.mustRunAfter performBatchmodeSpec
```

### Export unity package

By default the `exportUnityPackage` will be skipped because of missing sources. You need to tell the task what sources you want to include into the package. The produced archive will be added to the `unitypackage` configuration.

**build.gradle**

```groovy
exportUnityPackage {
    inputFiles file('Assets')
}
```

### Editor tests

The editor tests comes in two flavors: Unity 5.5 and Unity 5.6. These versions have different commandline parameters and options. The `wooga.gradle.unity.tasks.Test` will adjust the settings based on the unity version. It will figure out at runtime if your version is 5.5 or 5.6. It will always fallback to 5.5 logic if it couldn't figure out the version.

### PlayMode tests
The plugin automatically detects if `playMode` test are enabled and creates corresponding tasks.

```
:check
\--- :test
     +--- :testEditMode
     |    +--- :testEditModeAndroid
     |    \--- :testEditModeIos
     \--- :testPlayMode
          +--- :testPlayModeAndroid
          \--- :testPlayModeIos
```

### Setting up custom test tasks

**build.gradle unity 5.5**
```groovy
task("myTests", type:wooga.gradle.unity.tasks.Test) {
    categories = ["Category1",..]
    filter = ["filter",..]
    verbose = true
    teamcity = false
}
```

**build.gradle unity 5.6**
```groovy
task("myEditModeTests", type:wooga.gradle.unity.tasks.Test) {
    testPlatform = "editmode"
}

task("myPlayModeTests", type:wooga.gradle.unity.tasks.Test) {
    testPlatform = "playmode"
}
```