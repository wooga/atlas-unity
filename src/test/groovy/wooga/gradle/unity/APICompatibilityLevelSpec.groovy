package wooga.gradle.unity

import org.gradle.internal.impldep.org.apache.commons.lang.NullArgumentException
import spock.lang.Specification
import spock.lang.Subject

import java.security.InvalidKeyException

@Subject(APICompatibilityLevel)
class APICompatibilityLevelSpec extends Specification {

    def "parses api compatibility level string"() {
        given:
        def apiCompatLevel = value as APICompatibilityLevel

        expect:
        expectedAPICompatLevel == apiCompatLevel

        where:
        value | expectedAPICompatLevel
        "net2_0" | APICompatibilityLevel.net2_0
        "net2_0_subset" | APICompatibilityLevel.net2_0_subset
        "net4_6" | APICompatibilityLevel.net4_6
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
        1 | APICompatibilityLevel.net2_0
        2 | APICompatibilityLevel.net2_0_subset
        3 | APICompatibilityLevel.net4_6
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
