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
import org.junit.Rule
import org.junit.contrib.java.lang.system.EnvironmentVariables
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.environment.RestoreSystemProperties
import wooga.gradle.unity.batchMode.BuildTarget

class DefaultUnityPluginExtensionSpec extends Specification {

    UnityPluginExtension subject

    @Shared
    def projectProperties = [:]
    def projectMock

    def setup() {
        def projectMock = Mock(Project)
        projectMock.getProperties() >> projectProperties
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

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables()

    @RestoreSystemProperties
    @Unroll
    def "get default #property with #osName #osArch"() {
        given:
        environmentVariables.set("UNITY_PATH", null)
        System.setProperty("os.name", osName)
        System.setProperty("os.arch", osArch)

        expect:
        subject.invokeMethod(property, null) as File == expectedPath

        where:
        property                   | osName     | osArch | expectedPath
        "getUnityPath"             | "windows"  | "64"   | DefaultUnityPluginExtension.UNITY_PATH_WIN
        "getUnityPath"             | "windows"  | "32"   | DefaultUnityPluginExtension.UNITY_PATH_WIN_32
        "getUnityPath"             | "linux"    | "64"   | DefaultUnityPluginExtension.UNITY_PATH_LINUX
        "getUnityPath"             | "mac os x" | "64"   | DefaultUnityPluginExtension.UNITY_PATH_MAC_OS
        "getUnityPath"             | "mac os x" | "64"   | DefaultUnityPluginExtension.UNITY_PATH_MAC_OS
        "getUnityLicenseDirectory" | "windows"  | "64"   | DefaultUnityPluginExtension.UNITY_LICENSE_DIRECTORY_WIN
        "getUnityLicenseDirectory" | "mac os x" | "64"   | DefaultUnityPluginExtension.UNITY_LICENSE_DIRECTORY_MAC_OS
    }

    @Rule
    public final EnvironmentVariables env = new EnvironmentVariables()

    @Unroll
    def "default value for #property with env: #envValue and propertie #propertyValue returns #expectedValue"() {
        given:
        projectProperties[DefaultUnityPluginExtension.REDIRECT_STDOUT_OPTION] = propertyValue
        environmentVariables.set(DefaultUnityPluginExtension.REDIRECT_STDOUT_ENV_VAR, envValue)

        expect:
        subject.invokeMethod(property, null) as Boolean == expectedValue

        where:
        property            | envValue | propertyValue | expectedValue
        "getRedirectStdOut" | "0"      | "1"           | true
        "getRedirectStdOut" | "1"      | "0"           | false
        "getRedirectStdOut" | "0"      | "true"        | true
        "getRedirectStdOut" | "1"      | "false"       | false
        "getRedirectStdOut" | "0"      | "TRUE"        | true
        "getRedirectStdOut" | "1"      | "FALSE"       | false
        "getRedirectStdOut" | "0"      | "yes"         | true
        "getRedirectStdOut" | "1"      | "no"          | false
        "getRedirectStdOut" | "0"      | "YES"         | true
        "getRedirectStdOut" | "1"      | "NO"          | false
        "getRedirectStdOut" | "1"      | null          | true
        "getRedirectStdOut" | "0"      | null          | false
        "getRedirectStdOut" | "false"  | "true"        | true
        "getRedirectStdOut" | "true"   | "false"       | false
        "getRedirectStdOut" | "false"  | "TRUE"        | true
        "getRedirectStdOut" | "true"   | "FALSE"       | false
        "getRedirectStdOut" | "false"  | "1"           | true
        "getRedirectStdOut" | "true"   | "0"           | false
        "getRedirectStdOut" | "false"  | "yes"         | true
        "getRedirectStdOut" | "true"   | "no"          | false
        "getRedirectStdOut" | "false"  | "YES"         | true
        "getRedirectStdOut" | "true"   | "NO"          | false
        "getRedirectStdOut" | "true"   | null          | true
        "getRedirectStdOut" | "false"  | null          | false
        "getRedirectStdOut" | "FALSE"  | "TRUE"        | true
        "getRedirectStdOut" | "TRUE"   | "FALSE"       | false
        "getRedirectStdOut" | "FALSE"  | "true"        | true
        "getRedirectStdOut" | "TRUE"   | "false"       | false
        "getRedirectStdOut" | "FALSE"  | "1"           | true
        "getRedirectStdOut" | "TRUE"   | "0"           | false
        "getRedirectStdOut" | "FALSE"  | "yes"         | true
        "getRedirectStdOut" | "TRUE"   | "no"          | false
        "getRedirectStdOut" | "FALSE"  | "YES"         | true
        "getRedirectStdOut" | "TRUE"   | "NO"          | false
        "getRedirectStdOut" | "TRUE"   | null          | true
        "getRedirectStdOut" | "FALSE"  | null          | false
        "getRedirectStdOut" | "NO"     | "YES"         | true
        "getRedirectStdOut" | "YES"    | "NO"          | false
        "getRedirectStdOut" | "NO"     | "yes"         | true
        "getRedirectStdOut" | "YES"    | "no"          | false
        "getRedirectStdOut" | "NO"     | "true"        | true
        "getRedirectStdOut" | "YES"    | "false"       | false
        "getRedirectStdOut" | "NO"     | "TRUE"        | true
        "getRedirectStdOut" | "YES"    | "FALSE"       | false
        "getRedirectStdOut" | "NO"     | "1"           | true
        "getRedirectStdOut" | "YES"    | "0"           | false
        "getRedirectStdOut" | "YES"    | null          | true
        "getRedirectStdOut" | "NO"     | null          | false
        "getRedirectStdOut" | null     | null          | false
        "getRedirectStdOut" | null     | null          | false
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
        if (!expectedTestBuildTargets.empty) {
            expectedTestBuildTargets.each {
                subject.testBuildTargets.contains(it)
            }
        }

        where:
        timing    | defaultBuildTarget    | beforeTestBuildTargets             | afterTestBuildTargets   | expectedTestBuildTargets                                | useSetter
        "via"     | BuildTarget.ios       | []                                 | []                      | [BuildTarget.ios]                                       | true
        "via"     | BuildTarget.ios       | []                                 | []                      | [BuildTarget.ios]                                       | false
        "before"  | BuildTarget.android   | [BuildTarget.ios, BuildTarget.web] | []                      | [BuildTarget.ios, BuildTarget.web, BuildTarget.android] | true
        "before"  | BuildTarget.android   | [BuildTarget.ios, BuildTarget.web] | []                      | [BuildTarget.ios, BuildTarget.web, BuildTarget.android] | false
        "after"   | BuildTarget.android   | []                                 | [BuildTarget.ios]       | [BuildTarget.ios, BuildTarget.android]                  | true
        "after"   | BuildTarget.android   | []                                 | [BuildTarget.ios]       | [BuildTarget.ios, BuildTarget.android]                  | false
        "before"  | BuildTarget.android   | [BuildTarget.ios]                  | [BuildTarget.web]       | [BuildTarget.web, BuildTarget.android]                  | true
        "before"  | BuildTarget.android   | [BuildTarget.ios]                  | [BuildTarget.web]       | [BuildTarget.ios, BuildTarget.web, BuildTarget.android] | false
        "without" | BuildTarget.undefined | []                                 | [BuildTarget.ios]       | [BuildTarget.ios]                                       | true
        "without" | BuildTarget.undefined | []                                 | [BuildTarget.ios]       | [BuildTarget.ios]                                       | false
        "and"     | BuildTarget.ios       | [BuildTarget.ios]                  | [BuildTarget.ios]       | [BuildTarget.ios]                                       | true
        "and"     | BuildTarget.ios       | [BuildTarget.ios]                  | [BuildTarget.ios]       | [BuildTarget.ios]                                       | false
        "via"     | BuildTarget.undefined | []                                 | []                      | []                                                      | false
        "via"     | BuildTarget.android   | []                                 | [BuildTarget.undefined] | [BuildTarget.android]                                   | false
        "via"     | BuildTarget.android   | []                                 | [BuildTarget.undefined] | [BuildTarget.android]                                   | true


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

    @Unroll
    def "getTestBuildTargets when property unity.testBuildTargets is set with #source"() {
        given: "mock property in project"
        projectProperties["unity.testBuildTargets"] = source.join(',')

        when:
        def targets = subject.testBuildTargets

        then:
        targets.size() == expectedSize
        targets.every { BuildTarget.isInstance(it) }

        where:
        source << [["ios", "android"], ["ios"], ["android"]]
        expectedSize = source.size()
    }

    def "set logCategory with properties"(){
        given: "mock property in project"
        def testCategory = "helloworld"

        and:
        projectProperties[DefaultUnityPluginExtension.UNITY_LOG_CATEGORY_OPTION] = testCategory

        expect:
        subject.logCategory == testCategory
    }

    def "get logCategory from env returns property value over env"() {
        given: "logCategory set in properties"
        def category = "highPriority"
        def props = ['unity.logCategory': category]

        and: "unity path in environment"
        def newCategory = "lowPriority"
        def env = ['UNITY_LOG_CATEGORY': newCategory]

        when: "calling getUnityLogCategory"
        def result = subject.getUnityLogCategory(props, env)

        then: "file path points to props path"
        result == category
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
