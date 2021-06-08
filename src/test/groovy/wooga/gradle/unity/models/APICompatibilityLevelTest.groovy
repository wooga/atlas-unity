/*
 * Copyright 2021 Wooga GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package wooga.gradle.unity.models


import spock.lang.Specification
import spock.lang.Subject
import wooga.gradle.unity.models.APICompatibilityLevel

import java.security.InvalidKeyException

@Subject(APICompatibilityLevel)
class APICompatibilityLevelTest extends Specification {

    def "parses api compatibility level string"() {
        given:
        def apiCompatLevel = value as APICompatibilityLevel

        expect:
        expectedAPICompatLevel == apiCompatLevel

        where:
        value | expectedAPICompatLevel
        "net_2_0" | APICompatibilityLevel.net_2_0
        "net_2_0_subset" | APICompatibilityLevel.net_2_0_subset
        "net_4_6" | APICompatibilityLevel.net_4_6
        "net_web" | APICompatibilityLevel.net_web
        "net_micro" | APICompatibilityLevel.net_micro
        "net_standard_2_0" | APICompatibilityLevel.net_standard_2_0
    }

    def "parses api compatibility level from int"() {
        given:
        def apiCompatLevel = APICompatibilityLevel.valueOfInt(value)

        expect:
        expectedAPICompatLevel == apiCompatLevel

        where:
        value | expectedAPICompatLevel
        1 | APICompatibilityLevel.net_2_0
        2 | APICompatibilityLevel.net_2_0_subset
        3 | APICompatibilityLevel.net_4_6
        4 | APICompatibilityLevel.net_web
        5 | APICompatibilityLevel.net_micro
        6 | APICompatibilityLevel.net_standard_2_0
    }

    def "handles invalid values for api compatibility level from int"() {
        when:
        APICompatibilityLevel.valueOfInt(value)

        then:
        thrown(InvalidKeyException)

        where:
        value << [-1, 10]
    }
}
