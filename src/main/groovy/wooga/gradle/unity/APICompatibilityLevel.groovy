package wooga.gradle.unity

import org.gradle.internal.impldep.org.apache.commons.lang.NullArgumentException

import java.nio.InvalidMarkException
import java.security.InvalidKeyException

enum APICompatibilityLevel {
    net2_0(1),
    net2_0_subset(2),
    net4_6(3),
    net_web(4),
    net_micro(5),
    net_standard_2_0(6)

    private static Map map = new HashMap<>();

    static {
        for (APICompatibilityLevel apiLevel : APICompatibilityLevel.values()) {
            map.put(apiLevel.value, apiLevel);
        }
    }

    APICompatibilityLevel(Integer value) {
        this.value = value
    }

    private final Integer value
    Integer getValue() {
        value
    }

    static APICompatibilityLevel valueOfInt(Integer value) {
        if (!map.containsKey(value)) {
            throw new InvalidKeyException("There is no  API compatibility level for the value ${value}")
        }
        return (APICompatibilityLevel) map.get(value);
    }

    static final String unityProjectSettingsPropertyKey = "apiCompatibilityLevel"
    static final APICompatibilityLevel defaultLevel = APICompatibilityLevel.net2_0_subset
}
