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

package wooga.gradle.unity.batchMode

import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.internal.file.PathToFileResolver
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll
import wooga.gradle.unity.UnityAuthentication
import wooga.gradle.unity.UnityPluginExtension

@Subject([DefaultActivationAction])
class DefaultActivationActionSpec extends Specification {

    static String DEFAULT_USER = "test-user"
    static String DEFAULT_PASSWORD = "test-password"
    static String DEFAULT_SERIAL = "test-serial"

    def extension = Mock(UnityPluginExtension)
    def fileResolver = Mock(PathToFileResolver)
    def authentication = new UnityAuthentication(DEFAULT_USER, DEFAULT_PASSWORD, DEFAULT_SERIAL)

    def activationAction = new DefaultActivationAction(extension, fileResolver, authentication)

    def setup() {
        activationAction.unityPath = File.createTempDir()
    }

    def "fails when unity path doesn't exist"() {
        given: "a faulty unity path"
        activationAction.setUnityPath(new File("/does/not/exist"))

        when:
        activationAction.activate()

        then:
        GradleException e = thrown()
        e.message == "Unity does not exist"
    }

    def "fails when authentication.username is not set"() {
        given: "unset authentication object"
        activationAction.authentication.username = null

        when:
        activationAction.activate()

        then:
        GradleException e = thrown()
        e.message == "Need a unity login username"
    }

    def "fails when authentication.password is not set"() {
        given: "unset authentication object"
        activationAction.authentication.password = null

        when:
        activationAction.activate()

        then:
        GradleException e = thrown()
        e.message == "Need a unity login password"
    }

    def "verify commandline"() {
        when:
        def commandline = activationAction.setupActivationCommandline().join(" ")

        then:
        commandline.contains("$BatchModeFlags.BATCH_MODE")
        commandline.contains("$BatchModeFlags.QUIT")
        commandline.contains("$BatchModeFlags.USER_NAME test-user")
        commandline.contains("$BatchModeFlags.PASSWORD test-password")
        commandline.contains("$BatchModeFlags.SERIAL test-serial")
    }

    @Unroll("verify configuration of authentication with closure and #name, #pass, #key")
    def "authentication can be configure with clojure"() {

        given: "action configured with default authentication"
        assertDefaultAuthentication()

        and: "authentication object configured with closure"
        activationAction.authentication {
            username = name
            password = pass
            serial = key
        }

        expect:
        assertAuthentication(name,pass,key)

        where:
        name            | pass                | key
        "new-test-user" | "new-user-password" | "new-serial"
        null            | "new-user-password" | "new-serial"
        null            | null                | "new-serial"
        null            | null                | null
        "new-test-user" | null                | "new-serial"
        "new-test-user" | null                | null
        "new-test-user" | "new-user-password" | null

    }

    @Unroll("verify configuration of authentication with action and #name, #pass, #key")
    def "authentication can be configure with action"() {

        given: "action configured with default authentication"
        assertDefaultAuthentication()

        and: "authentication object configured with action"
        def n = name
        def p = pass
        def s = key

        activationAction.authentication(new Action<UnityAuthentication>() {
            @Override
            void execute(UnityAuthentication authentication) {
                authentication.username = n
                authentication.password = p
                authentication.serial = s
            }
        })

        expect:
        assertAuthentication(name,pass,key)

        where:
        name            | pass                | key
        "new-test-user" | "new-user-password" | "new-serial"
        null            | "new-user-password" | "new-serial"
        null            | null                | "new-serial"
        null            | null                | null
        "new-test-user" | null                | "new-serial"
        "new-test-user" | null                | null
        "new-test-user" | "new-user-password" | null
    }

    @Unroll("verify configuration of authentication with auth and #name, #pass, #key")
    def "authentication can be configure with authentication object"()
    {
        given: "action configured with default authentication"
        assertDefaultAuthentication()
        def authentication = activationAction.authentication

        and: "a reconfigured auth object"
        activationAction.authentication = new UnityAuthentication(name,pass,key)

        expect:
        assertAuthentication(name,pass,key)
        activationAction.authentication == authentication

        where:
        name            | pass                | key
        "new-test-user" | "new-user-password" | "new-serial"
        null            | "new-user-password" | "new-serial"
        null            | null                | "new-serial"
        null            | null                | null
        "new-test-user" | null                | "new-serial"
        "new-test-user" | null                | null
        "new-test-user" | "new-user-password" | null
    }

    def "will not change auth object when value is null"() {
        given: "action configured with default authentication"
        assertDefaultAuthentication()
        def authentication = activationAction.authentication

        when:
        activationAction.authentication = null

        then:
        assertDefaultAuthentication()
    }

    void assertAuthentication(u,p,s) {
        assert activationAction.authentication.username == u
        assert activationAction.authentication.password == p
        assert activationAction.authentication.serial == s
    }

    void assertDefaultAuthentication() {
        assertAuthentication(DEFAULT_USER, DEFAULT_PASSWORD, DEFAULT_SERIAL)
    }
}
