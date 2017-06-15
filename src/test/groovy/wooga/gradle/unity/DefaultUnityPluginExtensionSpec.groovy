package wooga.gradle.unity

import org.gradle.api.Project
import org.gradle.api.internal.file.FileResolver
import org.gradle.internal.reflect.Instantiator
import spock.lang.IgnoreIf
import spock.lang.Specification

class DefaultUnityPluginExtensionSpec extends Specification {

    def subject

    def setup() {
        subject = new DefaultUnityPluginExtension(Mock(Project), Mock(FileResolver), Mock(Instantiator))
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
