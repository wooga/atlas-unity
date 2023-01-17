package wooga.gradle.unity.models

enum ResolutionStrategy {
    /**
     * Do not upgrade indirect dependencies. Instead, it uses exactly the requested version. This is the default mode.
     */
    lowest,
    /**
     * Upgrade to the highest version with the same Major and Minor components.
     * For example, with the requested version 1.2.3, this strategy selects the highest version in the range
     * [1.2.3, 1.3.0) (that is, >= 1.2.3 and < 1.3.0).
     */
    highestPatch,
    /**
     * Upgrade to the highest version with the same Major component. For example, with the requested version 1.2.3,
     * this strategy selects the highest version in the range [1.2.3, 2.0.0) (that is, >= 1.2.3 and < 2.0.0).
     */
    highestMinor,
    /**
     * Upgrade to the highest version. For example, with the requested version 1.2.3, this strategy selects the
     * highest version in the range [1.2.3,) (that is, >= 1.2.3 with no upper bound)
     */
    highest
}
