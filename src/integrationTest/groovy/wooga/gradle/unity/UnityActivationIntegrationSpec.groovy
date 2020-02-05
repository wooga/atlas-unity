/*
 * Copyright 2018 Wooga GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package wooga.gradle.unity

import spock.lang.Unroll
import wooga.gradle.unity.batchMode.BatchModeFlags

/**
 * Integration spec for activation / return unity license
 */
class UnityActivationIntegrationSpec extends UnityIntegrationSpec {


    def setup() {
        buildFile << """
        unity{
             testBuildTargets = ["android"]
        }
        
        """.stripIndent()
    }


    def "skips activation when authentication is empty"() {
        given: "a build script with fake test unity location"
        buildFile << """
            task (mUnity, type: wooga.gradle.unity.tasks.Activate)
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("mUnity")

        then:
        result.wasSkipped("mUnity")
    }

    @Unroll("fails activation task when credentials part is missing (#name, #password, #serial")
    def "fails activation task when credentials part is missing"() {
        given: "a build script with fake test unity location"
        buildFile << """
            unity {
                authentication {
                    username = $name
                    password = $password
                    serial = $serial
                }
            }
        """.stripIndent()

        expect:
        runTasksWithFailure("activateUnityLicense")

        where:
        name  | password | serial
        "'a'" | "'b'"    | null
        "'a'" | null     | "'c'"
        "'a'" | null     | null
        null  | "'b'"    | "'c'"
        null  | "'b'"    | null
        null  | null     | "'c'"
        null  | null     | null
    }

    def "runs activation task with serial when set"() {
        given: "a build script with fake test unity location"
        buildFile << """
            unity {
                authentication {
                    username = "test@test.test"
                    password = "testtesttest"
                    serial = "abcdefg"
                }
            }

            task (mUnity, type: wooga.gradle.unity.tasks.Activate)
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("mUnity")

        then:
        !result.wasSkipped("mUnity")
        result.standardOutput.contains("${BatchModeFlags.USER_NAME} test@test.test")
        result.standardOutput.contains("${BatchModeFlags.PASSWORD} testtesttest")
        result.standardOutput.contains("${BatchModeFlags.SERIAL} abcdefg")
    }

    def "authentication can be overridden in task configuration"() {
        given: "a build script with fake test unity location"
        buildFile << """
            unity {
                authentication {
                    username = "test@test.test"
                    password = "testtesttest"
                    serial = "abcdefg"
                }
            }

            task (mUnity, type: wooga.gradle.unity.tasks.Activate) {
                authentication {
                    username = "beta@test.test"
                    password = "betatesttest"
                    serial = "zyxw"
                }
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("mUnity")

        then:
        !result.wasSkipped("mUnity")
        result.standardOutput.contains("${BatchModeFlags.USER_NAME} beta@test.test")
        result.standardOutput.contains("${BatchModeFlags.PASSWORD} betatesttest")
        result.standardOutput.contains("${BatchModeFlags.SERIAL} zyxw")
    }

    def "authentication can be set via setter"() {
        given: "a build script with fake test unity location"
        buildFile << """
            unity {
                authentication {
                    username = "test@test.test"
                    password = "testtesttest"
                    serial = "abcdefg"
                }
            }

            task (mUnity, type: wooga.gradle.unity.tasks.Activate) {
                authentication = new wooga.gradle.unity.internal.DefaultUnityAuthentication("beta@test.test", "betatesttest", "zyxw")
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("mUnity")

        then:
        !result.wasSkipped("mUnity")
        result.standardOutput.contains("${BatchModeFlags.USER_NAME} beta@test.test")
        result.standardOutput.contains("${BatchModeFlags.PASSWORD} betatesttest")
        result.standardOutput.contains("${BatchModeFlags.SERIAL} zyxw")
    }

    def "authentication can be set via setter in extension"() {
        given: "a build script with fake test unity location"
        buildFile << """
            unity {
                authentication = new wooga.gradle.unity.internal.DefaultUnityAuthentication("test@test.test", "testtesttest", "abcdefg")
            }

            task (mUnity, type: wooga.gradle.unity.tasks.Activate) {
                authentication = new wooga.gradle.unity.internal.DefaultUnityAuthentication("beta@test.test", "betatesttest", "zyxw")
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("mUnity")

        then:
        !result.wasSkipped("mUnity")
        result.standardOutput.contains("${BatchModeFlags.USER_NAME} beta@test.test")
        result.standardOutput.contains("${BatchModeFlags.PASSWORD} betatesttest")
        result.standardOutput.contains("${BatchModeFlags.SERIAL} zyxw")
    }

    def "authentication can be set via properties"() {
        given: "a build script with fake test unity location"
        def propertiesFile = createFile("gradle.properties")
        propertiesFile << """
            unity.username=delta@test.test
            unity.password=deltatesttest
            unity.serial=123456789
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("test")

        then:
        !result.wasSkipped("test")
        result.standardOutput.contains("${BatchModeFlags.USER_NAME} delta@test.test")
        result.standardOutput.contains("${BatchModeFlags.PASSWORD} deltatesttest")
        result.standardOutput.contains("${BatchModeFlags.SERIAL} 123456789")
    }

    def "activates with unity project path"() {
        given: "a build script with fake test unity authentication"
        buildFile << """
            unity {
                authentication {
                    username = "test@test.test"
                    password = "testtesttest"
                    serial = "abcdefg"
                }
            }

            task (mUnity, type: wooga.gradle.unity.tasks.Activate)
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("mUnity")

        then:
        !result.wasSkipped("mUnity")
        result.standardOutput.contains("${BatchModeFlags.PROJECT_PATH} ${projectDir}")
    }

    def "runs activation before a unity task when authentication is set once"() {
        given: "a build script with fake test unity location"
        buildFile << """
            unity {
                authentication {
                    username = "test@test.test"
                    password = "testtesttest"
                    serial = "abcdefg"
                }
            }
            
            task (mUnity, type: wooga.gradle.unity.tasks.Test)
            task (mUnity2, type: wooga.gradle.unity.tasks.Test)
            
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("test", "mUnity", "mUnity2")

        then:
        !result.wasSkipped("test")
        !result.wasSkipped("mUnity")
        !result.wasSkipped("mUnity2")
        result.wasExecuted("activateUnity")
    }

    def "return license runs always even when build fails"() {
        given: "a build script with fake test unity location"
        buildFile << """
            unity {
                authentication {
                    username = "test@test.test"
                    password = "testtesttest"
                    serial = "abcdefg"
                }
            }
            
            task (mUnity, type: wooga.gradle.unity.tasks.Test)
            
            task (fail) {
                doLast {
                    exec{
                        commandline "/does/not/exist"
                    }
                }
            }
                     
        """.stripIndent()

        when:
        def result = runTasksWithFailure("mUnity", "fail", "test")

        then:
        result.wasExecuted("activateUnity")
        result.wasExecuted("fail")
        result.wasExecuted("mUnity")
        result.wasExecuted("returnUnityLicense")
        !result.wasExecuted("test")
    }

    def "skips returnUnityLicense when license directory is empty"() {
        given: "a build script with fake test unity location"
        buildFile << """
            unity {
                authentication {
                    username = "test@test.test"
                    password = "testtesttest"
                    serial = "abcdefg"
                }
            }    
            
            returnUnityLicense {
                licenseDirectory = project.file("${File.createTempDir().path.replace('\\','\\\\')}")
            }               
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("test")

        then:
        result.wasExecuted("activateUnity")
        result.wasExecuted("returnUnityLicense")
        result.standardOutput.contains("returnUnityLicense NO-SOURCE") || result.standardOutput.contains("Skipping task ':returnUnityLicense' as it has no source files")
    }

    def "skips activateUnity and returnUnityLicense when authentication is not set"() {
        when:
        def result = runTasksSuccessfully("test")

        then:
        result.wasSkipped("activateUnity")
        result.wasSkipped("returnUnityLicense")
    }

    def "run returnUnityLicense from cli"() {
        when:
        def result = runTasksSuccessfully("returnUnityLicense")

        then:
        result.wasExecuted("returnUnityLicense")
        !result.wasExecuted("activateUnity")
    }

    def "run activateUnity from cli"() {
        when:
        def result = runTasksSuccessfully("activateUnity")

        then:
        result.wasSkipped("returnUnityLicense")
        result.wasExecuted("activateUnity")
    }

    def "skips activateUnity and returnUnityLicense when autoActivate is false"() {
        given: "a build script with fake test unity location"
        buildFile << """
            unity {
                authentication {
                    username = "test@test.test"
                    password = "testtesttest"
                    serial = "abcdefg"
                }
                autoActivateUnity = false
            }    
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("test")

        then:
        !result.wasExecuted("activateUnity")
        result.wasExecuted("test")
        !result.wasExecuted("returnUnityLicense")
    }

    def "skips returnUnityLicense when autoReturnLicense is false"() {
        given: "a build script with fake test unity location"
        buildFile << """
            unity {
                authentication {
                    username = "test@test.test"
                    password = "testtesttest"
                    serial = "abcdefg"
                }
                autoActivateUnity true
                autoReturnLicense false
            }    
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("test")

        then:
        result.wasExecuted("activateUnity")
        result.wasExecuted("test")
        !result.wasExecuted("returnUnityLicense")
    }

    def "run activateUnity in custom task as action"() {
        given: "a build script with fake test unity location"
        buildFile << """
            task (customTask) {
                doLast {
                    unity.activate {
                            authentication {
                            username = "custom@test.test"
                            password = "customtesttest"
                            serial = "customSerial"
                        }
                    }
                }
            }
            
        """.stripIndent()
        when:
        def result = runTasksSuccessfully("customTask")

        then:
        result.standardOutput.contains("${BatchModeFlags.USER_NAME} custom@test.test")
        result.standardOutput.contains("${BatchModeFlags.PASSWORD} customtesttest")
        result.standardOutput.contains("${BatchModeFlags.SERIAL} customSerial")
    }

    def "run returnLicense in custom task as action"() {
        given: "a build script with fake test unity location"
        buildFile << """
            task (customTask) {
                doLast {
                    unity.returnLicense {
                            authentication {
                            username = "custom@test.test"
                            password = "customtesttest"
                            serial = "customSerial"
                        }
                    }
                }
            }
            
        """.stripIndent()
        when:
        def result = runTasksSuccessfully("customTask")

        then:
        result.standardOutput.contains(BatchModeFlags.RETURN_LICENSE)
    }
}
