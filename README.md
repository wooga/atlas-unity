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
    id 'net.wooga.unity' version '0.16.0'
}
```

# Note breaking changes with `0.16.0`

With `0.16.0` the dependency setup for iOS and android will be removed from the plugin. The logic never really fitted into this plugin. It is moved to [`net.wooga.wdk-unity`](https://github.com/wooga/atlas-wdk-unity).

# System Requirements

[Unity3D][unity] > 5.5

# Usage

**build.gradle**

```groovy
plugins {
    id "net.wooga.unity" version "0.16.0"
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

    //defaultBuildTarget = "android"
}

exportUnityPackage {
    inputFiles file('Assets')
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

## Documentation

- [API docs](https://wooga.github.io/atlas-unity/docs/groovydoc/)
- [Tasks](docs/Tasks.md)

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

[yes]:                  https://atlas-resources.wooga.com/icons/icon_check.svg "yes"
[no]:                   https://atlas-resources.wooga.com/icons/icon_uncheck.svg "no"

