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

package wooga.gradle.unity.utils.internal

import spock.lang.Specification
import spock.lang.Unroll

abstract class UnityAssetFileSpec extends Specification {

    abstract Class<UnityAssetFile> getClassImp()


    static String BASIC_CONTENT = """      
    %YAML 1.1
    %TAG !u! tag:unity3d.com,2011:
    --- !u!129 &1
    PlayerSettings:
        wiiUDrcBufferDisabled: 0
        wiiUProfilerLibPath: 
        playModeTestRunnerEnabled: 0
        actionOnDotNetUnhandledException: 1
    
    """.stripIndent()

    static String INVALID_CONTENT = """      
    0000 be6d 0000 c9e7 0000 0011 0000 be90
    0000 0000 3230 3137 2e31 2e30 6633 00fe
    ffff ff01 0100 0000 8100 0000 00ff ffa0
    fc36 4045 ea85 96ed 5478 598b 4e1b 87d8
    0500 00d9 3100 000c 0000 0000 0000 0037
    0000 80ff ffff ff00 0000 0000 8000 0001
    
    """.stripIndent()

    @Unroll
    def "initialize with #objectType"() {
        expect:
        def constructor = classImp.getConstructor(content.class)
        constructor.newInstance(content)

        where:
        objectType | content
        "String"   | BASIC_CONTENT
        "File"     | File.createTempFile("test", ".asset") << BASIC_CONTENT
    }

    def "parse invalid project settings file"() {
        when: "initialize projectSettings with binary content"
        def constructor = classImp.getConstructor(content.class)
        UnityAssetFile settings = constructor.newInstance(content) as UnityAssetFile

        then:
        !settings.isValid()

        where:
        content << [INVALID_CONTENT]
    }

    def "strip unity instructions from content string"() {
        given: "a test content"
        def content = """      
        %YAML 1.1
        %TAG !u! tag:unity3d.com,2011:
        --- !u!129 &1
        PlayerSettings:
            wiiUDrcBufferDisabled: 0
            wiiUProfilerLibPath: 
            playModeTestRunnerEnabled: 0
            actionOnDotNetUnhandledException: 1
    
        """.stripIndent()

        when:
        def result = UnityAssetFile.stripUnityInstructions(content)

        then:
        result.readLines().every { !it.matches(/%TAG !u! tag:unity3d.com,.*:/) }
        result.readLines().every { !it.matches(/(--- )!u!\d+( &\d+)/) }
    }
}
