/*
 * Copyright 2017 Wooga GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */



package wooga.gradle.unity.utils

import org.yaml.snakeyaml.Yaml

class ProjectSettings {

    private def content

    ProjectSettings(File projectSettingsFile) {
        this(projectSettingsFile.text)
    }

    ProjectSettings(String templateContent) {

        Yaml parser = new Yaml()
        content = parser.load(stripUnityInstructions(templateContent))
    }

    boolean getPlayModeTestRunnerEnabled() {
        content['PlayerSettings'] && content['PlayerSettings']['playModeTestRunnerEnabled'] && content['PlayerSettings']['playModeTestRunnerEnabled'] == 1
    }

    static String stripUnityInstructions(String content) {
        def lines = content.readLines()
        lines.collect {
            if(it.matches(/%TAG !u! tag:unity3d.com,.*:/)) {
                return ""
            }

            def m = it =~ /(--- )!u!\d+( &\d+)/
            if(m) {
                return "${m[0][1]}${m[0][2]}"
            }

            return it
        }
        .join("\n")
    }
}
