package wooga.gradle.unity.utils


import spock.lang.Specification
import spock.lang.Unroll

class PropertyLookupTest extends Specification {

    @Unroll
    def "returns value #expected"() {

        given:
        if (props == _) {
            props = [:]
        }
        if (env == _) {
            env = [:]
        }

        when:
        def lookup = new PropertyLookup(envKeys, propKeys, defaultValue)


        then:
        def actual = lookup.getValue(props, env)
        actual == expected

        where:
        expected | props                    | env             | propKeys | envKeys | defaultValue
        "FOO"    | _                        | ["B": "FOO"]    | "A"      | "B"     | "FOOBAR"
        "FOOBAR" | _                        | _               | "A"      | "B"     | { "FOOBAR" }
        "FU"     | _                        | ["A": { "FU" }] | "B"      | "A"     | "FUBAR"
        "FU"     | ["D": { "FU" }]          | _               | "D"      | "D"     | "FUBAR"
        "FOO"    | ["A": "FOO"]             | ["A": "BAR"]    | "A"      | _       | "FOOBAR"
        "FOOBAR" | ["C": "FOO"]             | ["D": "BAR"]    | "A"      | _       | "FOOBAR"
        "FOOBAR" | ["C": "FOO"]             | ["D": "BAR"]    | "A"      | _       | "FOOBAR"
        "WAH"    | ["A": "FOO", "C": "WAH"] | ["A": "BAR"]    | "C"      | _       | "FOOBAR"
        7        | _                        | _               | "C"      | "B"     | 7
    }

    def "returns default value when lookups fail"() {

        given:
        def props = [:]
        def env = [:]
        def propsKey = "A"
        def envKey = "B"

        when:
        def lookup = new PropertyLookup(envKey, propsKey, defaultValue)

        then:
        def actual = lookup.getValue(props, env)
        actual == expected

        where:
        defaultValue = "FOO"
        expected = defaultValue
    }

    def "returns properties before environment value"() {

        when:
        def lookup = new PropertyLookup(envKey, propsKey, defaultValue)

        then:
        def actual = lookup.getValue(props, env)
        actual == expected

        where:
        defaultValue = "FOO"
        propsKey = "A"
        envKey = "B"
        props = ["A": "7"]
        env = ["B": "3"]

        expected = "7"
    }

}
