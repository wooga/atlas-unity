package wooga.gradle.unity.models

/**
 * The Unity test platform values.
 * <p>
 * Unity contains multiple test unit/integration test runner.
 * The @{code TestPlatform} value determines the which runner to invoke.
 */
enum TestPlatform {
    /**
     * Editmode tests are basic unit tests with no access or interaction with the Unity engine.
     */
    editmode,

    /**
     * Playmode tests will be executed within a running Unity engine.
     */
    playmode
}
