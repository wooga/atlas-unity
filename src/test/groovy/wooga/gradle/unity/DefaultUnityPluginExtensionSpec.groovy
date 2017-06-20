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
import spock.lang.IgnoreIf
import spock.lang.Specification

class DefaultUnityPluginExtensionSpec extends Specification {

    def subject

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
}
