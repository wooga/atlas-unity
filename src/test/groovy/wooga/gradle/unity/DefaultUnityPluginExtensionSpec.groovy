package wooga.gradle.unity

import org.gradle.api.Project
import org.gradle.api.internal.file.FileResolver
import org.gradle.internal.reflect.Instantiator
import spock.lang.Specification

class DefaultUnityPluginExtensionSpec extends Specification {

    def subject

    def setup() {
        subject = new DefaultUnityPluginExtension(Mock(Project), Mock(FileResolver), Mock(Instantiator))
    }

    def "get unity path from env returns property value over env"() {
        given: "unity path set in properties"
        def props = ['unity.path': "/path/to/unity"]

        and: "unity path in environment"
        def env = ['UNITY_PATH': "/path/to/other/unity"]

        when: "calling getUnityPathFromEnv"
        def f = subject.getUnityPathFromEnv(props, env)

        then: "file path points to props path"
        f.path == "/path/to/unity"
    }

    def "get unity path from env returns env when prop not set"() {
        given: "empty properties"
        def props = [:]

        and: "unity path in environment"
        def env = ['UNITY_PATH': "/path/to/other/unity"]

        when: "calling getUnityPathFromEnv"
        def f = subject.getUnityPathFromEnv(props, env)

        then: "file path points to props path"
        f.path == "/path/to/other/unity"
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
