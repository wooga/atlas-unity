/*
 * Copyright 2017 the original author or authors.
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

package wooga.gradle.unity

import nebula.test.IntegrationSpec
import org.apache.commons.lang.StringEscapeUtils

abstract class UnityIntegrationSpec extends IntegrationSpec {

    File unityTestLocation
    File unityMainDirectory

    def escapedPath(String path) {
        String osName = System.getProperty("os.name").toLowerCase()
        if (osName.contains("windows")) {
            return StringEscapeUtils.escapeJava(path)
        }
        path
    }

    def setup() {
        String osName = System.getProperty("os.name").toLowerCase()
        unityMainDirectory = new File(projectDir, "Unity/SomeLevel/SecondLevel")
        unityMainDirectory.mkdirs()
        unityTestLocation = createFile("fakeUnity.bat", unityMainDirectory)
        unityTestLocation.executable = true
        if (osName.contains("windows")) {
            unityTestLocation << """
                @echo off
                echo %*
            """.stripIndent()
        }
        else
        {
            unityTestLocation << """
                #!/usr/bin/env bash
                echo \$@
            """.stripIndent()
        }

        buildFile << """
            group = 'test'
            ${applyPlugin(wooga.gradle.unity.UnityPlugin)}
         
            unity.unityPath("Unity/SomeLevel/SecondLevel/fakeUnity.bat")
        """.stripIndent()
    }
}
