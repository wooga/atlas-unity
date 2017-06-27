atlas-unity
===========

[![Gradle Plugin ID](https://img.shields.io/badge/gradle-net.wooga.unity-brightgreen.svg?style=flat-square)](https://plugins.gradle.org/plugin/net.wooga.unity)
[![Build Status](https://img.shields.io/travis/wooga/atlas-unity/master.svg?style=flat-square)](https://travis-ci.org/wooga/atlas-unity)
[![Coveralls Status](https://img.shields.io/coveralls/wooga/atlas-unity/master.svg?style=flat-square)](https://coveralls.io/github/wooga/atlas-unity?branch=master)
[![Apache 2.0](https://img.shields.io/badge/license-Apache%202-blue.svg?style=flat-square)](https://raw.githubusercontent.com/wooga/atlas-unity/master/LICENSE)
[![GitHub tag](https://img.shields.io/github/tag/wooga/atlas-unity.svg?style=flat-square)]()
[![GitHub release](https://img.shields.io/github/release/wooga/atlas-unity.svg?style=flat-square)]()

This plugin provides tasks to run unity batchmode commands in [gradle][gradle]. It runs and reports unity unit-tests and is able to export `.unitypackage` files.

# Applying the plugin

**build.gradle**
```groovy
plugins {
    id 'net.wooga.unity' version '0.10.0'
}
```

#System Requirements

[Unity3D][unity] > 5.5

#Usage

**build.gradle**

```groovy
plugins {
    id "net.wooga.unity" version "0.10.0"
}

unity {
    authentication {
        username = "username@company.com"
        password = "password"
        serial = "unityserial"
    }

    // projectPath = ""
    // reportsDir = ""
    // unityPath = ""

    //autoActivateUnity = true
    //autoReturnLicense = true
    
    //assetsDir = "Assets"
    //pluginsDir = "Assets/Plugins"
    //androidResourceCopyMethod = "arrUnpack" | "sync"
}

exportUnityPackage {
    inputFiles file('Assets')
}

test {
    // unity 5.5
    // categories = [...,]
    // filter = [...,]
    // verbose = true
    // teamcity = false
    
    // unity 5.6
    // testPlatform 
}

task(performBuild, type:wooga.gradle.unity.tasks.Unity) {
    args "-executeMethod", "MyEditorScript.PerformBuild"

    // projectPath
    // buildTarget
    // logFile
    // quit = true || quit true
    // batchMode = true batchMode true
    // noGraphics = false noGraphics false
}

task(performMultipleBuilds) {
    doLast {
        unity.batchMode {
            unityPath = project.file("/Applications/Unity-5.5.3f1/Unity.app/Contents/MacOS/Unity")
            args "-executeMethod", "MyEditorScript.PerformBuild"
        }

        unity.batchMode {
            unityPath = project.file("/Applications/Unity-5.6.0f3/Unity.app/Contents/MacOS/Unity")
            args "-executeMethod", "MyEditorScript.PerformBuild"
        }
    }
}

//hook up the tasks into the lifecycle
tasks.assemble.dependsOn performBuild
tasks.assemble.dependsOn performMultipleBuilds
tasks.returnUnityLicense.mustRunAfter performMultipleBuilds

```

## Tasks
The plugin will add a number of tasks you can use

| Task name          | Depends on            | Type                                           | Description |
| ------------------ | --------------------- | ---------------------------------------------- | ----------- |
| activateUnity      |                       | `wooga.gradle.unity.tasks.Activate`            | Activates unity with provides credentials. Gets skipped when credentials are missing |
| returnUnityLicense |                       | `wooga.gradle.unity.tasks.ReturnLicense`       | Returns the current licesne to license server. Gets skipped when license directory is empty |
| exportUnityPackage | activationTask, setup | `wooga.gradle.unity.tasks.UnityPackage`        | exports configured assets into an `.unitypackage` file |
| test               | activationTask, setup | `wooga.gradle.unity.tasks.Test`                | runs unity editor tests and writes reports to `reportsDir` |
| setup              |                       | `DefaultTask`                                  | lifecycle task to initialize unity project and all dependencies |
| assembleResources  | setup                 | `DefaultTask`                                  | copies all android/ios dependencies to the unity `Plugins` folder |

### Custom Batchmode task
You can call any abitary unity batchmode command either with the `BatchModeSpec` or the `wooga.gradle.unity.tasks.Unity` task type:

**build.gradle**
```
task(performBatchmodeTask, type:wooga.gradle.unity.tasks.Unity) {
    args "-executeMethod", "MyEditorScript.PerformBuild"
}
```
-or-
```
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
```
task(performBatchmodeSpec) {
    ...
}

tasks.returnUnityLicense.mustRunAfter performBatchmodeSpec
```

### Export unity package

By default the `exportUnityPackage` will be skipped because of missing sources. You need to tell the task what sources you want to include into the package. The produced archive will be added to the `unitypackage` configuration.

**build.gradle**

```
exportUnityPackage {
    inputFiles file('Assets')
}
```

### Editor tests

The editor tests comes in two flavors: Unity 5.5 and Unity 5.6. These versions have different commandline parameters and options. The `wooga.gradle.unity.tasks.Test` will adjust the settings based on the unity version. It will figure out at runtime if your version is 5.5 or 5.6. It will always fallback to 5.5 logic if it couldn't figure out the version.

**build.gradle unity 5.5**
```
test {
    categories = ["Category1",..]
    filter = ["filter",..]
    verbose = true
    teamcity = false
}
```

**build.gradle unity 5.6**
```
test {
    testPlatform = "editmode" || "playmode"
}
```

### iOS/Android dependencies

You can set local or project dependencies to external `*.jar`, `*.aar`, iOS source files (`*.m`, `*.mm`, `*.h`, etc) or `zipped` `framework files`.
Every task which extends `AbstractUnityTask` will execute the `assembleResources` task. This task copies the dependencies into the `Plugins` directory in the `Assets` directory. You can set the `Plugins` directory destination with `unity.pluginsDir`.

**build.gradle**
```groovy
plugins {
    id "net.wooga.unity" version "0.10.0"
}

dependencies {
    android fileTree(dir: "libs", include: "*.jar")
    android project(':android:SubProject')
    
    ios fileTree(dir: "ios/classes")
    ios project(':android:SubProject') //which bundles an .framework.zip
}
```

`.framework` artifacts are a little tricky since gradle defines that an artifact is one file. A `.framework` is a directory. So to make framework files work with gradle's dependency management we need to zip it. The unity plugin will unzip all files `*.framework.zip`


Gradle and Java Compatibility
=============================

Built with Oracle JDK7
Tested with Oracle JDK8

| Gradle Version | Works       |
| :------------- | :---------: |
| <= 2.13        | ![no]       |
| 2.14           | ![yes]      |
| 3.0            | ![yes]      |
| 3.1            | ![yes]      |
| 3.2            | ![yes]      |
| 3.4            | ![yes]      |
| 3.4.1          | ![yes]      |
| 3.5            | ![yes]      |
| 3.5.1          | ![yes]      |
| 4.0            | ![yes]      |


LICENSE
=======

Copyright 2017 Wooga GmbH

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

<http://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

<!-- Links -->
[unity]:                https://unity3d.com/ "Unity 3D"
[unity_cmd]:            https://docs.unity3d.com/Manual/CommandLineArguments.html
[gradle]:               https://gradle.org/ "Gradle"
[gradle_finalizedBy]:   https://docs.gradle.org/3.5/dsl/org.gradle.api.Task.html#org.gradle.api.Task:finalizedBy
[gradle_dependsOn]:     https://docs.gradle.org/3.5/dsl/org.gradle.api.Task.html#org.gradle.api.Task:dependsOn

[yes]:                  http://atlas-resources.wooga.com/icons/icon_check.svg "yes"
[no]:                   http://atlas-resources.wooga.com/icons/icon_uncheck.svg "no"
