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

import org.gradle.api.Project
import org.gradle.api.internal.file.FileResolver
import org.gradle.internal.reflect.Instantiator
import spock.lang.Specification
import spock.lang.Unroll
import wooga.gradle.unity.batchMode.BuildTarget

class DefaultUnityPluginExtensionSpec extends Specification {

    UnityPluginExtension subject

    def setup() {
        def projectMock = Mock(Project)
        projectMock.getProperties() >> [:]
        projectMock.getRootProject() >> projectMock
        subject = new DefaultUnityPluginExtension(projectMock, Mock(FileResolver), Mock(Instantiator))
    }


    def "get unity path from env returns property value over env"() {
        given: "unity path set in properties"
        def file = new File('/path/to/unity')
        def props = ['unity.path': file.path]


        and: "unity path in environment"
        def newPath = new File('/path/to/other/unity')
        def env = ['UNITY_PATH': newPath.path]

        when: "calling getUnityPathFromEnv"
        def f = subject.getUnityPathFromEnv(props, env)

        then: "file path points to props path"
        f.path == file.path
    }

    def "get unity path from env returns env when prop not set"() {
        given: "empty properties"
        def props = [:]

        and: "unity path in environment"
        def newPath = new File('/path/to/other/unity')
        def env = ['UNITY_PATH': newPath.path]

        when: "calling getUnityPathFromEnv"
        def f = subject.getUnityPathFromEnv(props, env)

        then: "file path points to props path"
        f.path == newPath.path
    }

    def "get unity path from env returns null when nothing is set"() {
        given: "empty properties"
        def props = [:]

        and: "empty environment"
        def env = [:]

        when: "calling getUnityPathFromEnv"
        def f = subject.getUnityPathFromEnv(props, env)

        then: "file path points to props path"
        f == null
    }

    @Unroll("use defaultBuildTarget with #source|#result")
    def "set defaultBuildTarget "() {
        given: "calling set defaultBuildTarget"
        subject.defaultBuildTarget = source

        when: "calling get defaultBuildTarget"
        def defaultBuildTarget = subject.defaultBuildTarget

        then: "file path points to props path"
        defaultBuildTarget == result

        where:
        source            | result
        BuildTarget.webgl | BuildTarget.webgl
        "ios"             | BuildTarget.ios
                { it -> BuildTarget.android } | BuildTarget.android
    }

    @Unroll
    def "set testBuildTargets #timing defaultBuildTarget with #method"() {
        given:
        if (!beforeTestBuildTargets.empty) {
            subject.testBuildTargets(beforeTestBuildTargets)
        }
        assert subject.getTestBuildTargets().size() == initialSize

        and: "extension with defaultBuildTarget"
        subject.defaultBuildTarget = defaultBuildTarget

        when:
        if (!afterTestBuildTargets.empty) {
            subject.invokeMethod(method, afterTestBuildTargets)
        }

        then:
        subject.getTestBuildTargets().size() == expectedSize
        if(!expectedTestBuildTargets.empty) {
            expectedTestBuildTargets.each {
              subject.testBuildTargets.contains(it)
            }
        }

        where:
        timing    | defaultBuildTarget    | beforeTestBuildTargets             | afterTestBuildTargets | expectedTestBuildTargets                                | useSetter
        "via"     | BuildTarget.ios       | []                                 | []                    | [BuildTarget.ios]                                       | true
        "via"     | BuildTarget.ios       | []                                 | []                    | [BuildTarget.ios]                                       | false
        "before"  | BuildTarget.android   | [BuildTarget.ios, BuildTarget.web] | []                    | [BuildTarget.ios, BuildTarget.web, BuildTarget.android] | true
        "before"  | BuildTarget.android   | [BuildTarget.ios, BuildTarget.web] | []                    | [BuildTarget.ios, BuildTarget.web, BuildTarget.android] | false
        "after"   | BuildTarget.android   | []                                 | [BuildTarget.ios]     | [BuildTarget.ios, BuildTarget.android]                  | true
        "after"   | BuildTarget.android   | []                                 | [BuildTarget.ios]     | [BuildTarget.ios, BuildTarget.android]                  | false
        "before"  | BuildTarget.android   | [BuildTarget.ios]                  | [BuildTarget.web]     | [BuildTarget.web, BuildTarget.android]                  | true
        "before"  | BuildTarget.android   | [BuildTarget.ios]                  | [BuildTarget.web]     | [BuildTarget.ios, BuildTarget.web, BuildTarget.android] | false
        "without" | BuildTarget.undefined | []                                 | [BuildTarget.ios]     | [BuildTarget.ios]                                       | true
        "without" | BuildTarget.undefined | []                                 | [BuildTarget.ios]     | [BuildTarget.ios]                                       | false
        "and"     | BuildTarget.ios       | [BuildTarget.ios]                  | [BuildTarget.ios]     | [BuildTarget.ios]                                       | true
        "and"     | BuildTarget.ios       | [BuildTarget.ios]                  | [BuildTarget.ios]     | [BuildTarget.ios]                                       | false
        "via"     | BuildTarget.undefined | []                                 | []                    | []                                                      | false

        expectedSize = expectedTestBuildTargets.size
        initialSize = beforeTestBuildTargets.size
        method = (useSetter) ? "setTestBuildTargets" : "testBuildTargets"
    }

    @Unroll
    def "testBuildTargets with List #source"() {
        given: "calling testBuildTargets"
        subject.testBuildTargets(source)

        expect:
        def targets = subject.testBuildTargets
        targets.size() == 2
        targets.every { BuildTarget.isInstance(it) }

        where:
        source << [[BuildTarget.android, BuildTarget.ios], ["ios", "android"], [new BuildTargetTestObject("ios"), new BuildTargetTestObject("android")]]
    }

    @Unroll
    def "testBuildTargets with variadic arguments #source"() {
        given: "calling testBuildTargets"
        subject.testBuildTargets(source[0], source[1])

        expect:
        def targets = subject.testBuildTargets
        targets.size() == 2
        targets.every { BuildTarget.isInstance(it) }

        where:
        source << [[BuildTarget.android, BuildTarget.ios], ["ios", "android"], [new BuildTargetTestObject("ios"), new BuildTargetTestObject("android")]]
    }

    @Unroll
    def "setTestBuildTargets assigns build targets"() {
        given: "calling testBuildTargets"
        subject.testBuildTargets = source

        when:
        def targets = subject.testBuildTargets

        then:
        targets.size() == 2
        targets.every { BuildTarget.isInstance(it) }

        when:
        subject.testBuildTargets = [override]
        targets = subject.testBuildTargets
        then:
        targets.size() == 1

        where:
        source << [[BuildTarget.android, BuildTarget.ios], ["ios", "android"], [new BuildTargetTestObject("ios"), new BuildTargetTestObject("android")]]
        override << [BuildTarget.web, "web", new BuildTargetTestObject("web")]
    }

    class BuildTargetTestObject {

        private def testValue

        BuildTargetTestObject(String value) {
            this.testValue = value
        }

        @Override
        String toString() {
            return testValue
        }
    }
}
