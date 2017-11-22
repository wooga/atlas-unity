package wooga.gradle.unity

import spock.lang.Unroll

class TestTaskExecutionSpec extends UnityIntegrationSpec {

    @Unroll
    def "Test tasks dependencies for :#task"() {

        given: "a build file with testBuildTargets"
        buildFile << """    
        unity.testBuildTargets = $testBuildTargets
        """.stripIndent()

        when:
        def result = runTasksSuccessfully(task)

        then:
        includedTasks.every { result.wasExecuted(it) }
        excludedTasks.every { !result.wasExecuted(it) }

        where:
        task              | testBuildTargets     | includedTasks                                                               | excludedTasks
        "test"            | '["ios", "android"]' | ["setup", "test", "testPlayMode", "testPlayModeAndroid", "testPlayModeIos"] | ["check"]
        "testPlayMode"    | '["android"]'        | ["setup", "testPlayMode", "testPlayModeAndroid"]                            | ["test", "testPlayModeIos"]
        "testPlayModeIos" | '["ios"]'            | ["setup", "testPlayModeIos"]                                                | ["test", "testPlayMode"]
    }

    @Unroll
    def "verify testBuildTargets fallback order with #message"() {
        given: "a build file"
        buildFile << """
        $buildFileTestBuildTargets
        $defaultBuildTarget
        """.stripIndent()

        and: "a properties file"
        createFile("gradle.properties") << """
        $propertyFileTestBuildTargets
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("check", commandlineTestBuildTargets)

        then:
        taskShouldRun.each { String task ->
            assert result.wasExecuted(task)
        }

        where:
        message                                    | buildFileTestBuildTargets                    | propertyFileTestBuildTargets         | commandlineTestBuildTargets         | defaultBuildTarget               | expectedTasksToRun
        "nothing"                                  | ""                                           | ""                                   | "-Pnothing=true"                    | ""                               | []
        "build.gradle"                             | "unity.testBuildTargets = ['ios']"           | ""                                   | "-Pnothing=true"                    | ""                               | ["testPlayModeIos", "testEditModeIos"]
        "multiple build.gradle"                    | "unity.testBuildTargets = ['ios','android']" | ""                                   | "-Pnothing=true"                    | ""                               | ["testPlayModeIos", "testEditModeIos", "testPlayModeAndroid", "testEditModeAndroid"]
        "gradle.properties"                        | ""                                           | "unity.testBuildTargets=webgl"       | "-Pnothing=true"                    | ""                               | ["testPlayModeWebgl", "testEditModeWebgl"]
        "multiple gradle.properties"               | ""                                           | "unity.testBuildTargets=webgl,linux" | "-Pnothing=true"                    | ""                               | ["testPlayModeWebgl", "testEditModeWebgl", "testPlayModeLinux", "testEditModeLinux"]
        "commandline"                              | ""                                           | ""                                   | "-Punity.testBuildTargets=ps4"      | ""                               | ["testPlayModePs4", "testEditModePs4"]
        "multiple commandline"                     | ""                                           | ""                                   | "-Punity.testBuildTargets=ps4,psp2" | ""                               | ["testPlayModePs4", "testEditModePs4", "testPlayModePsp2", "testEditModePsp2"]
        "defaultBuildTarget"                       | ""                                           | ""                                   | "-Pnothing=true"                    | "unity.defaultBuildTarget='web'" | ["testPlayModeWeb", "testEditModeWeb"]
        "build.gradle and gradle.properties"       | "unity.testBuildTargets = ['ios']"           | "unity.testBuildTargets=webgl"       | "-Pnothing=true"                    | ""                               | ["testPlayModeIos", "testEditModeIos"]
        "build.gradle and commandline"             | "unity.testBuildTargets = ['ios']"           | ""                                   | "-Punity.testBuildTargets=ps4,psp2" | ""                               | ["testPlayModeIos", "testEditModeIos"]
        "build.gradle and defaultBuildTarget"      | "unity.testBuildTargets = ['ios']"           | ""                                   | "-Pnothing=true"                    | "unity.defaultBuildTarget='web'" | ["testPlayModeIos", "testEditModeIos"]
        "commandline and defaultBuildTarget"       | ""                                           | ""                                   | "-Punity.testBuildTargets=ps4,psp2" | "unity.defaultBuildTarget='web'" | ["testPlayModePs4", "testEditModePs4", "testPlayModePsp2", "testEditModePsp2"]
        "commandline and gradle.properties "       | ""                                           | "unity.testBuildTargets=webgl,linux" | "-Punity.testBuildTargets=ps4,psp2" | ""                               | ["testPlayModePs4", "testEditModePs4", "testPlayModePsp2", "testEditModePsp2"]
        "gradle.properties and defaultBuildTarget" | ""                                           | "unity.testBuildTargets=webgl,linux" | "-Pnothing=true"                    | "unity.defaultBuildTarget='web'" | ["testPlayModeWebgl", "testEditModeWebgl", "testPlayModeLinux", "testEditModeLinux"]

        taskShouldRun = expectedTasksToRun << "test" << "testPlayMode" << "testEditMode"
    }
}
