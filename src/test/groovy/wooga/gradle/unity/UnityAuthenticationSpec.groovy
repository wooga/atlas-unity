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

import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

@Subject(UnityAuthentication)
class UnityAuthenticationSpec extends Specification {

    @Unroll("validate getUsername with values from #source")
    def "returns username"() {
        given:
        def authentication = new UnityAuthentication(props, env)
        if (set) {
            authentication.username = set
        }

        expect:
        username == authentication.username

        where:
        username    | env                       | props                           | set        | source
        "test_env"  | ["UNITY_USR": "test_env"] | [:]                             | null       | "env"
        "test_prop" | [:]                       | ["unity.username": "test_prop"] | null       | "properties"
        "test_prop" | ["UNITY_USR": "test_env"] | ["unity.username": "test_prop"] | null       | "env/properties"
        "test_set"  | ["UNITY_USR": "test_env"] | ["unity.username": "test_prop"] | "test_set" | "setter"
        null        | [:]                       | [:]                             | null       | "null"
    }

    @Unroll("validate getPassword with values from #source")
    def "returns password"() {
        given:
        def authentication = new UnityAuthentication(props, env)
        if (set) {
            authentication.password = set
        }

        expect:
        password == authentication.password

        where:
        password    | env                       | props                           | set        | source
        "test_env"  | ["UNITY_PWD": "test_env"] | [:]                             | null       | "env"
        "test_prop" | [:]                       | ["unity.password": "test_prop"] | null       | "properties"
        "test_prop" | ["UNITY_PWD": "test_env"] | ["unity.password": "test_prop"] | null       | "env/properties"
        "test_set"  | ["UNITY_PWD": "test_env"] | ["unity.password": "test_prop"] | "test_set" | "setter"
        null        | [:]                       | [:]                             | null       | "null"
    }

    @Unroll("validate getSerial with values from #source")
    def "returns serial"() {
        given:
        def authentication = new UnityAuthentication(props, env)
        if (set) {
            authentication.serial = set
        }

        expect:
        serial == authentication.serial

        where:
        serial      | env                          | props                         | set        | source
        "test_env"  | ["UNITY_SERIAL": "test_env"] | [:]                           | null       | "env"
        "test_prop" | [:]                          | ["unity.serial": "test_prop"] | null       | "properties"
        "test_prop" | ["UNITY_SERIAL": "test_env"] | ["unity.serial": "test_prop"] | null       | "env/properties"
        "test_set"  | ["UNITY_SERIAL": "test_env"] | ["unity.serial": "test_prop"] | "test_set" | "setter"
        null        | [:]                          | [:]                           | null       | "null"
    }
}
