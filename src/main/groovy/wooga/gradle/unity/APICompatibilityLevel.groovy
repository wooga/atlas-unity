package wooga.gradle.unity

enum APICompatibilityLevel {
    net2_0(1),
    net2_0_subset(2),
    net4_6(3),
    net_web(4),
    net_micro(5),
    net_standard_2_0(6)

    private final Integer value

    APICompatibilityLevel(Integer value) {
        this.value = value
    }

    static APICompatibilityLevel valueOfInt(Integer value) {
        new APICompatibilityLevel(value)
    }
}
