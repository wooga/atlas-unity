/*
 * Copyright 2018 Wooga GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package wooga.gradle.unity

import spock.lang.Unroll
import wooga.gradle.unity.batchMode.BatchModeFlags
import wooga.gradle.unity.batchMode.BuildTarget

class UnityBatchModeIntegrationSpec extends UnityIntegrationSpec {

    def setup() {
        buildFile << """
        task (mUnity, type: wooga.gradle.unity.tasks.Unity) {
            quit = false
            batchMode = false
            noGraphics = false
            buildTarget = 'undefined'
        }
        """.stripIndent()
    }

    @Unroll
    def "can set #property with #value and expect commandline switch #expectedCommandlineSwitch"() {
        given:
        buildFile << """
        
        mUnity {
            $method($value) 
        }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("mUnity")

        then:
        result.wasExecuted("mUnity")
        result.standardOutput.contains("Starting process 'command '")
        result.standardOutput.contains(" $expectedCommandlineSwitch")

        where:
        property      | useSetter | value                             | expectedCommandlineSwitch
        "quit"        | false     | 'true'                            | BatchModeFlags.QUIT
        "quit"        | true      | 'true'                            | BatchModeFlags.QUIT
        "batchMode"   | false     | 'true'                            | BatchModeFlags.BATCH_MODE
        "batchMode"   | true      | 'true'                            | BatchModeFlags.BATCH_MODE
        "noGraphics"  | false     | 'true'                            | BatchModeFlags.NO_GRAPHICS
        "noGraphics"  | true      | 'true'                            | BatchModeFlags.NO_GRAPHICS
        "buildTarget" | false     | '"android"'                       | BatchModeFlags.BUILD_TARGET
        "buildTarget" | true      | '"android"'                       | BatchModeFlags.BUILD_TARGET
        "args"        | false     | '"-customFlag1", "-customFlag2"'  | '-customFlag1 -customFlag2'
        "args"        | true      | '["-customFlag1","-customFlag2"]' | '-customFlag1 -customFlag2'
        "args"        | false     | '["-customFlag1","-customFlag2"]' | '-customFlag1 -customFlag2'

        method = (useSetter) ? "set${property.capitalize()}" : property
    }

    @Unroll
    def "can set #property with #value and expect no commandline switch #expectedCommandlineSwitch"() {
        given:
        buildFile << """
        
        mUnity {
            $method($value) 
        }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("mUnity")

        then:
        result.wasExecuted("mUnity")
        result.standardOutput.contains("Starting process 'command '")
        !result.standardOutput.contains(" $expectedCommandlineSwitch")

        where:
        property     | useSetter | value   | expectedCommandlineSwitch
        "quit"       | false     | 'false' | BatchModeFlags.QUIT
        "quit"       | true      | 'false' | BatchModeFlags.QUIT
        "batchMode"  | false     | 'false' | BatchModeFlags.BATCH_MODE
        "batchMode"  | true      | 'false' | BatchModeFlags.BATCH_MODE
        "noGraphics" | false     | 'false' | BatchModeFlags.NO_GRAPHICS
        "noGraphics" | true      | 'false' | BatchModeFlags.NO_GRAPHICS

        method = (useSetter) ? "set${property.capitalize()}" : property
    }

    @Unroll
    def "can get #property from task"() {
        given:
        buildFile << """
        
        mUnity {
            $property = $initialValue 
        }
        
        task (mUnity2, type: wooga.gradle.unity.tasks.Unity) {
            $property = $value
        }

        mUnity.$property = mUnity2.$property
        
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("mUnity")

        then:
        result.wasExecuted("mUnity")
        result.standardOutput.contains("Starting process 'command '")
        result.standardOutput.contains(" $expectedCommandlineSwitch")

        where:
        property      | initialValue       | value              | expectedCommandlineSwitch
        "quit"        | 'false'            | 'true'             | BatchModeFlags.QUIT
        "batchMode"   | 'false'            | 'true'             | BatchModeFlags.BATCH_MODE
        "noGraphics"  | 'false'            | 'true'             | BatchModeFlags.NO_GRAPHICS
        "buildTarget" | '"ios"'            | '"android"'        | "$BatchModeFlags.BUILD_TARGET $BuildTarget.android"
        "args"        | '["-customFlag1"]' | '["-customFlag2"]' | '-customFlag2'
    }

}
